package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builds contexts from explicit stereotypes and deterministic name matches, without invoking
 * cdconcretization or the conformance checker.
 */
public class ManualIncarnationContextBuilder {
  private final Set<CDConfParameter> confParams;
  private final IncarnationContextSupport support;

  public ManualIncarnationContextBuilder(
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex,
      Set<CDConfParameter> confParams) {
    this.confParams = confParams;
    this.support = new IncarnationContextSupport(referenceIndex, concreteIndex);
  }

  public IncarnationContext buildContextForMapping(String mapping) {
    Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings = support.newMapping();
    collectTypeMappings(mapping, mappings);
    collectMemberMappings(mapping, mappings);
    collectForEachMappings(mappings);
    return support.assembleContext(mapping, mappings);
  }

  private void collectTypeMappings(
      String mapping, Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings) {
    for (ASTCDType concreteType : support.concreteIndex().types()) {
      Optional<String> explicit = support.stereotypeValue(concreteType, mapping);
      if (explicit.isPresent()) {
        support
            .findReferenceType(explicit.get())
            .ifPresent(reference -> support.addMapping(mappings, reference, concreteType.getSymbol()));
      } else if (confParams.contains(CDConfParameter.NAME_MAPPING)) {
        support
            .findReferenceType(concreteType.getName())
            .ifPresent(reference -> support.addMapping(mappings, reference, concreteType.getSymbol()));
      }
    }
  }

  private void collectMemberMappings(
      String mapping, Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings) {
    for (ASTCDType concreteType : support.concreteIndex().types()) {
      List<ASTCDType> referenceOwners = support.mappedReferenceOwners(mappings, concreteType);
      for (ASTCDAttribute concreteField : concreteType.getCDAttributeList()) {
        Optional<String> explicit = support.stereotypeValue(concreteField, mapping);
        if (explicit.isPresent()) {
          support
              .findReferenceField(referenceOwners, explicit.get())
              .ifPresent(reference -> support.addMapping(mappings, reference, concreteField.getSymbol()));
        } else if (confParams.contains(CDConfParameter.NAME_MAPPING)) {
          support
              .findSameNameField(referenceOwners, concreteField)
              .ifPresent(reference -> support.addMapping(mappings, reference, concreteField.getSymbol()));
        }
      }
      for (ASTCDMethod concreteMethod : concreteType.getCDMethodList()) {
        Optional<String> explicit = support.stereotypeValue(concreteMethod, mapping);
        if (explicit.isPresent()) {
          support
              .findReferenceMethod(referenceOwners, explicit.get())
              .ifPresent(reference -> support.addMapping(mappings, reference, concreteMethod.getSymbol()));
        } else if (confParams.contains(CDConfParameter.NAME_MAPPING)) {
          support
              .findSameSignatureMethod(referenceOwners, concreteMethod)
              .ifPresent(reference -> support.addMapping(mappings, reference, concreteMethod.getSymbol()));
        }
      }
    }
  }

  /** Expands manual {@code <<forEach="...">>} mappings from already-known incarnations. */
  private void collectForEachMappings(
      Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings) {
    for (ASTCDType referenceType : support.referenceIndex().types()) {
      support
          .stereotypeValue(referenceType, "forEach")
          .flatMap(support::findReferenceType)
          .ifPresent(
              target -> copyIncarnations(mappings, target, StableElementKey.type(referenceType)));

      for (ASTCDAttribute referenceField : referenceType.getCDAttributeList()) {
        support.stereotypeValue(referenceField, "forEach").ifPresent(
            name -> {
              Optional<StableElementKey> target =
                  support.findReferenceField(List.of(referenceType), name);
              if (target.isEmpty()) {
                target = support.findReferenceMethod(List.of(referenceType), name);
              }
              target.ifPresent(
                  key -> copyIncarnations(
                      mappings, key, StableElementKey.field(referenceType, referenceField)));
            });
      }
      for (ASTCDMethod referenceMethod : referenceType.getCDMethodList()) {
        Optional<String> targetName = support.stereotypeValue(referenceMethod, "forEach");
        if (targetName.isEmpty()) {
          continue;
        }
        Optional<StableElementKey> targetMethod =
            support.findReferenceMethod(List.of(referenceType), targetName.get());
        if (targetMethod.isPresent()) {
          copyIncarnations(
              mappings,
              targetMethod.get(),
              StableElementKey.method(referenceType, referenceMethod));
          continue;
        }
        support
            .findReferenceField(List.of(referenceType), targetName.get())
            .ifPresent(
                target -> collectForEachMethods(mappings, target, referenceType, referenceMethod));
      }
    }
  }

  private void collectForEachMethods(
      Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings,
      StableElementKey targetField,
      ASTCDType referenceOwner,
      ASTCDMethod referenceMethod) {
    List<IncarnationContext.MappedElement> targetIncarnations = mappings.get(targetField);
    if (targetIncarnations == null) {
      return;
    }
    for (IncarnationContext.MappedElement incarnation : targetIncarnations) {
      if (!(incarnation.symbol().getAstNode() instanceof ASTCDAttribute concreteField)) {
        continue;
      }
      Optional<ASTCDType> owner = support.concreteIndex().ownerOf(concreteField);
      if (owner.isEmpty()) {
        continue;
      }
      Set<String> names =
          forEachMethodNames(
              referenceMethod.getName(), targetField.getName(), concreteField.getName());
      for (ASTCDMethod concreteMethod : owner.get().getCDMethodList()) {
        if (names.contains(concreteMethod.getName())
            && concreteMethod.getCDParameterList().size()
                == referenceMethod.getCDParameterList().size()) {
          support.addMapping(
              mappings,
              StableElementKey.method(referenceOwner, referenceMethod),
              concreteMethod.getSymbol());
        }
      }
    }
  }

  private void copyIncarnations(
      Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings,
      StableElementKey target,
      StableElementKey reference) {
    List<IncarnationContext.MappedElement> incarnations = mappings.get(target);
    if (incarnations == null) {
      return;
    }
    for (IncarnationContext.MappedElement incarnation : incarnations) {
      support.addMapping(mappings, reference, incarnation.symbol());
    }
  }

  private Set<String> forEachMethodNames(
      String methodName, String referenceTarget, String concreteTarget) {
    Set<String> result = new LinkedHashSet<>();
    if (methodName.equals(referenceTarget)) {
      result.add(concreteTarget);
    }
    if (methodName.equals(JavaSourceNames.capitalize(referenceTarget))) {
      result.add(JavaSourceNames.capitalize(concreteTarget));
    }
    replaceNameSegment(methodName, referenceTarget, concreteTarget).ifPresent(result::add);
    replaceNameSegment(
            methodName,
            JavaSourceNames.capitalize(referenceTarget),
            JavaSourceNames.capitalize(concreteTarget))
        .ifPresent(result::add);
    replaceNameSegment(
            methodName,
            JavaSourceNames.uncapitalize(referenceTarget),
            JavaSourceNames.uncapitalize(concreteTarget))
        .ifPresent(result::add);
    return result;
  }

  private static Optional<String> replaceNameSegment(
      String name, String referenceSegment, String concreteSegment) {
    if (name == null
        || referenceSegment == null
        || referenceSegment.isEmpty()
        || concreteSegment == null
        || concreteSegment.isEmpty()) {
      return Optional.empty();
    }
    List<String> segments = splitNameSegments(name);
    StringBuilder rewritten = new StringBuilder(name.length() + concreteSegment.length());
    boolean changed = false;
    for (String segment : segments) {
      if (segment.equals(referenceSegment)) {
        rewritten.append(concreteSegment);
        changed = true;
      } else {
        rewritten.append(segment);
      }
    }
    return changed ? Optional.of(rewritten.toString()) : Optional.empty();
  }

  private static List<String> splitNameSegments(String name) {
    List<String> result = new ArrayList<>();
    int start = 0;
    for (int i = 1; i < name.length(); i++) {
      char previous = name.charAt(i - 1);
      char current = name.charAt(i);
      if (Character.isUpperCase(current)
          && (Character.isLowerCase(previous)
              || (i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1))))) {
        result.add(name.substring(start, i));
        start = i;
      }
    }
    result.add(name.substring(start));
    return result;
  }
}
