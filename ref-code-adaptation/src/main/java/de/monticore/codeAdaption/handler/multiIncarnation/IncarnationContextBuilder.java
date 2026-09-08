package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Builds incarnation contexts from checker results, optionally overlaid with stereotypes. */
public class IncarnationContextBuilder {
  private final CDConformanceChecker conformanceChecker;
  private final IncarnationMappingSupport support;

  /**
   * Creates a builder backed by one completed conformance check and indexes of the same model pair.
   *
   * @param conformanceChecker checker whose incarnation mapping supplies inferred relationships
   * @param referenceIndex index of the reference CD
   * @param concreteIndex index of the concrete CD checked against the reference CD
   */
  public IncarnationContextBuilder(
      CDConformanceChecker conformanceChecker,
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex) {
    this.conformanceChecker = conformanceChecker;
    this.support = new IncarnationMappingSupport(referenceIndex, concreteIndex);
  }

  /**
   * Builds a context from checker results and fills checker gaps from explicit mapping
   * stereotypes.
   *
   * @param mapping stereotype name whose explicit mappings should be overlaid
   * @return immutable context for the requested mapping
   */
  public IncarnationContext buildContextForMapping(String mapping) {
    return buildContextForMapping(mapping, true);
  }

  /**
   * Builds a context from inferred checker mappings and, when requested, explicit stereotypes.
   * Explicit mappings are merged with rather than substituted for checker results; duplicate
   * concrete targets are removed by {@link IncarnationMappingSupport}.
   *
   * <p>For example, if the checker maps {@code Payment -> CreditCard} and stereotype {@code
   * <<shop="Payment">>} maps {@code Invoice}, enabling the overlay yields {@code Payment ->
   * [CreditCard, Invoice]}; disabling it retains only {@code CreditCard}.
   *
   * @param mapping stereotype name represented by the resulting context
   * @param overlayStereotypeMappings whether explicit stereotypes should fill or extend checker
   *     data
   * @return immutable incarnation and grouping context
   */
  public IncarnationContext buildContextForMapping(
      String mapping, boolean overlayStereotypeMappings) {
    Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings = support.newMapping();
    if (conformanceChecker.getIncarnationMapping() != null) {
      extractCheckerMappings(mappings);
    }
    if (overlayStereotypeMappings) {
      Log.info(
          "Overlaying stereotype context for mapping '" + mapping + "'",
          "IncarnationContextBuilder");
      support.mergeMappings(mappings, extractStereotypeMappings(mapping));
    }
    return support.assembleContext(mapping, mappings);
  }

  /**
   * Copies inferred type, field, and method incarnations from the checker into stable keys that
   * remain usable after AST cloning.
   */
  private void extractCheckerMappings(
      Map<StableElementKey, List<IncarnationContext.MappedElement>> result) {
    for (ASTCDType referenceType : support.referenceIndex().types()) {
      var typeIncarnations =
          conformanceChecker.getIncarnationMapping().getIncarnations(referenceType);
      if (typeIncarnations != null) {
        for (var incarnation : typeIncarnations) {
          support.addMapping(result, StableElementKey.type(referenceType), incarnation.getSymbol());
        }
      }
      for (ASTCDAttribute referenceField : referenceType.getCDAttributeList()) {
        var fieldIncarnations =
            conformanceChecker.getIncarnationMapping().getIncarnations(referenceField);
        if (fieldIncarnations == null) {
          continue;
        }
        for (var incarnation : fieldIncarnations) {
          support.addMapping(
              result, StableElementKey.field(referenceType, referenceField), incarnation.getSymbol());
        }
      }
      for (ASTCDMethod referenceMethod : referenceType.getCDMethodList()) {
        var methodIncarnations =
            conformanceChecker.getIncarnationMapping().getIncarnations(referenceMethod);
        if (methodIncarnations == null) {
          continue;
        }
        for (var incarnation : methodIncarnations) {
          support.addMapping(
              result, StableElementKey.method(referenceType, referenceMethod), incarnation.getSymbol());
        }
      }
    }
  }

  /**
   * Extracts explicit mappings from stereotypes on concrete types and their members. Member
   * references are resolved only within the reference owners mapped for the concrete type.
   */
  private Map<StableElementKey, List<IncarnationContext.MappedElement>> extractStereotypeMappings(
      String mapping) {
    Map<StableElementKey, List<IncarnationContext.MappedElement>> result = support.newMapping();
    for (ASTCDType concreteType : support.concreteIndex().types()) {
      Optional<String> referenceTypeName = support.stereotypeValue(concreteType, mapping);
      Optional<StableElementKey> referenceType =
          referenceTypeName.flatMap(support::findReferenceType);
      referenceType.ifPresent(
          key -> support.addMapping(result, key, concreteType.getSymbol()));

      List<ASTCDType> referenceOwners = referenceOwners(concreteType, mapping, referenceTypeName);
      for (ASTCDAttribute concreteField : concreteType.getCDAttributeList()) {
        support
            .stereotypeValue(concreteField, mapping)
            .flatMap(name -> support.findReferenceField(referenceOwners, name))
            .ifPresent(
                key -> support.addMapping(result, key, concreteField.getSymbol()));
      }
      for (ASTCDMethod concreteMethod : concreteType.getCDMethodList()) {
        support
            .stereotypeValue(concreteMethod, mapping)
            .flatMap(name -> support.findReferenceMethod(referenceOwners, name))
            .ifPresent(
                key -> support.addMapping(result, key, concreteMethod.getSymbol()));
      }
    }
    return result;
  }

  /**
   * Finds the reference owners in which member stereotype names must be resolved, preferring the
   * directly mapped reference type and then a mapped reference interface.
   */
  private List<ASTCDType> referenceOwners(
      ASTCDType concreteType, String mapping, Optional<String> referenceTypeName) {
    List<ASTCDType> owners = new ArrayList<>();
    referenceTypeName
        .flatMap(support.referenceIndex()::type)
        .ifPresent(owners::add);
    if (owners.isEmpty()) {
      support
          .mappedInterfaceReference(concreteType, mapping)
          .flatMap(support.referenceIndex()::type)
          .ifPresent(owners::add);
    }
    return owners;
  }
}
