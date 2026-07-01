package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Shared helper API for adaptation conflict checks. */
public final class ConflictDetectionContext {

  private final Set<String> mappings;
  private final Map<String, IncarnationContext> contexts;
  private final Set<CDConfParameter> confParams;
  private final boolean useCommonParentForMultipleIncarnations;
  private final CDModelIndex referenceIndex;
  private final CDModelIndex concreteIndex;
  private final Map<String, ASTCDType> referenceTypes = new LinkedHashMap<>();
  private final Map<String, ASTCDType> concreteTypes = new LinkedHashMap<>();

  public ConflictDetectionContext(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams,
      boolean useCommonParentForMultipleIncarnations) {
    this.mappings = mappings;
    this.contexts = contexts;
    this.confParams = confParams;
    this.useCommonParentForMultipleIncarnations = useCommonParentForMultipleIncarnations;
    this.referenceIndex = CDModelIndex.of(referenceCD);
    this.concreteIndex = CDModelIndex.of(concreteCD);
    referenceIndex.types().forEach(type -> referenceTypes.put(type.getName(), type));
    concreteIndex.types().forEach(type -> concreteTypes.put(type.getName(), type));
  }

  public Set<String> mappings() {
    return mappings;
  }

  public Map<String, IncarnationContext> contexts() {
    return contexts;
  }

  public Set<CDConfParameter> confParams() {
    return confParams;
  }

  public boolean useCommonParentForMultipleIncarnations() {
    return useCommonParentForMultipleIncarnations;
  }

  public CDModelIndex referenceIndex() {
    return referenceIndex;
  }

  public CDModelIndex concreteIndex() {
    return concreteIndex;
  }

  public Map<String, ASTCDType> referenceTypes() {
    return referenceTypes;
  }

  public Map<String, ASTCDType> concreteTypes() {
    return concreteTypes;
  }

  public Set<String> concreteTypeNamesFor(IncarnationContext context, String referenceTypeName) {
    Set<String> result = new LinkedHashSet<>();
    for (var incarnation : context.getIncarnations(StableElementKey.type(referenceTypeName))) {
      result.add(incarnation.getKey().getName());
    }
    if (result.isEmpty() && concreteTypes.containsKey(referenceTypeName)) {
      result.add(referenceTypeName);
    }
    return result;
  }

  public Set<String> concreteParentNames(ASTCDType concrete) {
    Set<String> parents = new LinkedHashSet<>();
    CDTypeRelations.firstSuperclassName(concrete).map(this::simpleName).ifPresent(parents::add);
    for (String interfaceName : CDTypeRelations.interfaceNames(concrete)) {
      parents.add(simpleName(interfaceName));
    }
    parents.removeIf(parent -> !concreteIndex.hasType(parent));
    return parents;
  }

  public Optional<String> stereotypeValue(ASTCDType cdType, String mapping) {
    if (cdType.getModifier() == null || !cdType.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var stereotype : cdType.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(stereotype.getName())) {
        try {
          return Optional.ofNullable(stereotype.getValue());
        } catch (Exception ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> stereotypeValue(ASTCDMethod cdMethod, String mapping) {
    if (cdMethod.getModifier() == null || !cdMethod.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var stereotype : cdMethod.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(stereotype.getName())) {
        try {
          return Optional.ofNullable(stereotype.getValue());
        } catch (Exception ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> stereotypeValue(ASTCDAttribute cdAttribute, String mapping) {
    if (cdAttribute.getModifier() == null || !cdAttribute.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var stereotype : cdAttribute.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(stereotype.getName())) {
        try {
          return Optional.ofNullable(stereotype.getValue());
        } catch (Exception ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> forEachValue(Object element) {
    if (element instanceof ASTCDType type) {
      return stereotypeValue(type, "forEach");
    }
    if (element instanceof ASTCDAttribute attribute) {
      return stereotypeValue(attribute, "forEach");
    }
    if (element instanceof ASTCDMethod method) {
      return stereotypeValue(method, "forEach");
    }
    return Optional.empty();
  }

  public String typeKind(ASTCDType type) {
    if (type instanceof ASTCDClass) {
      return "class";
    }
    if (type instanceof ASTCDInterface) {
      return "interface";
    }
    if (type instanceof ASTCDEnum) {
      return "enum";
    }
    return type.getClass().getSimpleName();
  }

  public String printType(ASTCDAttribute attribute) {
    return JavaSourceNames.printNormalizedFieldType(attribute);
  }

  public String methodSignature(ASTCDMethod method) {
    return JavaSourceNames.methodSignature(method);
  }

  public String returnType(ASTCDMethod method) {
    return JavaSourceNames.printNormalizedReturnType(method);
  }

  public boolean isAny(String type) {
    return "any".equals(type == null ? "" : type.trim());
  }

  public String printedType(Object astType) {
    if (astType instanceof de.monticore.types.mcbasictypes._ast.ASTMCType type) {
      return JavaLoader.print(type);
    }
    return astType == null ? "" : astType.toString();
  }

  public String associationName(ASTCDAssociation association) {
    if (association.isPresentName()) {
      return association.getName();
    }
    return association.getLeftQualifiedName().getQName()
        + "--"
        + association.getRightQualifiedName().getQName();
  }

  public String simpleName(String name) {
    return JavaSourceNames.simpleName(name);
  }

  public List<RoleFieldCandidate> roleFieldCandidates(ASTCDAssociation association) {
    List<RoleFieldCandidate> result = new ArrayList<>();
    String left = simpleName(association.getLeftQualifiedName().getQName());
    String right = simpleName(association.getRightQualifiedName().getQName());
    if (association.getLeft().isPresentCDRole()) {
      result.add(new RoleFieldCandidate(right, association.getLeft().getCDRole().getName(), left));
    }
    if (association.getRight().isPresentCDRole()) {
      result.add(new RoleFieldCandidate(left, association.getRight().getCDRole().getName(), right));
    }
    return result;
  }

  public record RoleFieldCandidate(String owner, String role, String target) {}
}
