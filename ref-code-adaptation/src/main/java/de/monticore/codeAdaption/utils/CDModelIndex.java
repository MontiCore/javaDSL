package de.monticore.codeAdaption.utils;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable lookup structure for class-diagram elements and inheritance relationships.
 *
 * <p>Named lookups are intentionally based on simple CD type names. Supplying {@code
 * shop.Payment} therefore looks up {@code Payment}; this index does not distinguish two types that
 * differ only by package. Member-owner lookups use the owning type's simple name, while {@link
 * #ownerOf(ASTCDAttribute)} and {@link #ownerOf(ASTCDMethod)} use AST object identity and therefore
 * require nodes from this indexed CD.
 */
public final class CDModelIndex {
  private final ASTCDCompilationUnit cd;
  private final List<ASTCDType> types;
  private final List<ASTCDClass> classes;
  private final List<ASTCDInterface> interfaces;
  private final List<ASTCDEnum> enums;
  private final List<ASTCDAssociation> associations;
  private final Map<String, ASTCDType> typesBySimpleName;
  private final Map<String, List<ASTCDAttribute>> attributesByOwner;
  private final Map<String, List<ASTCDMethod>> methodsByOwner;
  private final Map<ASTCDAttribute, ASTCDType> attributeOwners;
  private final Map<ASTCDMethod, ASTCDType> methodOwners;
  private final Map<String, Set<String>> directParentsByType;
  private final Map<String, Set<String>> childrenByParent;
  private final Map<String, List<ASTCDType>> implementersByInterface;

  private CDModelIndex() {
    this.cd = null;
    this.types = List.of();
    this.classes = List.of();
    this.interfaces = List.of();
    this.enums = List.of();
    this.associations = List.of();
    this.typesBySimpleName = Map.of();
    this.attributesByOwner = Map.of();
    this.methodsByOwner = Map.of();
    this.attributeOwners = Map.of();
    this.methodOwners = Map.of();
    this.directParentsByType = Map.of();
    this.childrenByParent = Map.of();
    this.implementersByInterface = Map.of();
  }

  private CDModelIndex(ASTCDCompilationUnit cd) {
    this.cd = cd;
    this.classes = List.copyOf(cd.getCDDefinition().getCDClassesList());
    this.interfaces = List.copyOf(cd.getCDDefinition().getCDInterfacesList());
    this.enums = List.copyOf(cd.getCDDefinition().getCDEnumsList());
    this.associations = List.copyOf(cd.getCDDefinition().getCDAssociationsList());

    List<ASTCDType> collectedTypes = new ArrayList<>();
    collectedTypes.addAll(classes);
    collectedTypes.addAll(interfaces);
    collectedTypes.addAll(enums);
    this.types = List.copyOf(collectedTypes);

    Map<String, ASTCDType> typeMap = new LinkedHashMap<>();
    Map<String, List<ASTCDAttribute>> attributes = new LinkedHashMap<>();
    Map<String, List<ASTCDMethod>> methods = new LinkedHashMap<>();
    Map<ASTCDAttribute, ASTCDType> attributeOwnerMap = new IdentityHashMap<>();
    Map<ASTCDMethod, ASTCDType> methodOwnerMap = new IdentityHashMap<>();
    Map<String, Set<String>> parents = new LinkedHashMap<>();
    Map<String, Set<String>> children = new LinkedHashMap<>();
    Map<String, List<ASTCDType>> implementers = new LinkedHashMap<>();

    for (ASTCDType type : types) {
      String typeName = type.getName();
      typeMap.put(typeName, type);
      attributes.put(typeName, List.copyOf(type.getCDAttributeList()));
      methods.put(typeName, List.copyOf(type.getCDMethodList()));

      for (ASTCDAttribute attribute : type.getCDAttributeList()) {
        attributeOwnerMap.put(attribute, type);
      }
      for (ASTCDMethod method : type.getCDMethodList()) {
        methodOwnerMap.put(method, type);
      }

      Set<String> directParents = new LinkedHashSet<>();
      CDTypeRelations.firstSuperclassName(type)
          .map(JavaSourceNames::simpleName)
          .filter(name -> !name.isEmpty())
          .ifPresent(directParents::add);
      for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
        String simpleName = JavaSourceNames.simpleName(interfaceName);
        if (!simpleName.isEmpty()) {
          directParents.add(simpleName);
          implementers.computeIfAbsent(simpleName, ignored -> new ArrayList<>()).add(type);
        }
      }
      parents.put(typeName, Collections.unmodifiableSet(directParents));
      for (String parent : directParents) {
        children.computeIfAbsent(parent, ignored -> new LinkedHashSet<>()).add(typeName);
      }
    }

    this.typesBySimpleName = Map.copyOf(typeMap);
    this.attributesByOwner = copyListMap(attributes);
    this.methodsByOwner = copyListMap(methods);
    this.attributeOwners = Collections.unmodifiableMap(new IdentityHashMap<>(attributeOwnerMap));
    this.methodOwners = Collections.unmodifiableMap(new IdentityHashMap<>(methodOwnerMap));
    this.directParentsByType = copySetMap(parents);
    this.childrenByParent = copySetMap(children);
    this.implementersByInterface = copyListMap(implementers);
  }

  /** Creates an index, or an empty index when {@code cd} is {@code null}. */
  public static CDModelIndex of(ASTCDCompilationUnit cd) {
    if (cd == null) {
      return new CDModelIndex();
    }
    return new CDModelIndex(cd);
  }

  public ASTCDCompilationUnit cd() {
    return cd;
  }

  public List<ASTCDType> types() {
    return types;
  }

  public List<ASTCDClass> classes() {
    return classes;
  }

  public List<ASTCDInterface> interfaces() {
    return interfaces;
  }

  public List<ASTCDEnum> enums() {
    return enums;
  }

  public List<ASTCDAssociation> associations() {
    return associations;
  }

  /** Resolves a type after reducing the supplied name to its simple name. */
  public Optional<ASTCDType> type(String name) {
    return Optional.ofNullable(typesBySimpleName.get(JavaSourceNames.simpleName(name)));
  }

  public boolean hasType(String name) {
    return type(name).isPresent();
  }

  /** Returns attributes declared directly by the named owner; inherited attributes are excluded. */
  public List<ASTCDAttribute> attributes(String ownerName) {
    return attributesByOwner.getOrDefault(JavaSourceNames.simpleName(ownerName), List.of());
  }

  /** Resolves one directly declared attribute by owner and simple attribute name. */
  public Optional<ASTCDAttribute> attribute(String ownerName, String attributeName) {
    String simpleAttribute = JavaSourceNames.simpleName(attributeName);
    return attributes(ownerName).stream()
        .filter(attribute -> attribute.getName().equals(simpleAttribute))
        .findFirst();
  }

  /** Returns methods declared directly by the named owner; inherited methods are excluded. */
  public List<ASTCDMethod> methods(String ownerName) {
    return methodsByOwner.getOrDefault(JavaSourceNames.simpleName(ownerName), List.of());
  }

  /** Returns directly declared overloads having the supplied simple method name. */
  public List<ASTCDMethod> methods(String ownerName, String methodName) {
    String simpleMethod = JavaSourceNames.simpleName(methodName);
    return methods(ownerName).stream()
        .filter(method -> method.getName().equals(simpleMethod))
        .toList();
  }

  /**
   * Resolves one directly declared method by its normalized {@code name(type,...)} signature.
   *
   * <p>For example, {@code method("Order", "update(java.time.Instant)")} does not match a
   * zero-argument {@code update()} overload.
   */
  public Optional<ASTCDMethod> method(String ownerName, String signature) {
    String normalizedSignature = signature == null ? "" : signature.trim();
    return methods(ownerName).stream()
        .filter(method -> JavaSourceNames.methodSignature(method).equals(normalizedSignature))
        .findFirst();
  }

  public Optional<ASTCDType> ownerOf(ASTCDAttribute attribute) {
    return Optional.ofNullable(attributeOwners.get(attribute));
  }

  public Optional<ASTCDType> ownerOf(ASTCDMethod method) {
    return Optional.ofNullable(methodOwners.get(method));
  }

  /** Returns direct superclass and interface names; the supplied type itself is not included. */
  public Set<String> directParentNames(String typeName) {
    return directParentsByType.getOrDefault(JavaSourceNames.simpleName(typeName), Set.of());
  }

  /** Returns every reachable superclass and interface name, excluding the supplied type itself. */
  public Set<String> transitiveParentNames(String typeName) {
    Set<String> result = new LinkedHashSet<>();
    ArrayDeque<String> queue = new ArrayDeque<>(directParentNames(typeName));
    while (!queue.isEmpty()) {
      String parent = queue.removeFirst();
      if (!result.add(parent)) {
        continue;
      }
      queue.addAll(directParentNames(parent));
    }
    return Collections.unmodifiableSet(result);
  }

  /**
   * Returns whether {@code typeName} has {@code parentName} as a direct or transitive parent.
   * Equal type names are not considered a subtype relationship by this method.
   */
  public boolean isSubtypeOf(String typeName, String parentName) {
    String simpleParent = JavaSourceNames.simpleName(parentName);
    return directParentNames(typeName).contains(simpleParent)
        || transitiveParentNames(typeName).contains(simpleParent);
  }

  /** Returns names of types that directly declare the supplied parent. */
  public Set<String> childNames(String parentName) {
    return childrenByParent.getOrDefault(JavaSourceNames.simpleName(parentName), Set.of());
  }

  /**
   * Returns types that directly list the supplied interface in their declaration.
   *
   * <p>This is a declaration index, not a transitive subtype query. Use {@link #isSubtypeOf} when
   * indirect interface implementation must count.
   */
  public List<ASTCDType> implementersOf(String interfaceName) {
    return implementersByInterface.getOrDefault(JavaSourceNames.simpleName(interfaceName), List.of());
  }

  /**
   * Finds the unique type that directly declares an attribute or method with this simple name.
   * Returns empty both when no owner exists and when several types declare that member name.
   */
  public Optional<ASTCDType> ownerOfMemberNamed(String memberName) {
    String simpleMember = JavaSourceNames.simpleName(memberName);
    ASTCDType owner = null;
    for (ASTCDType type : types) {
      boolean matches =
          attributes(type.getName()).stream().anyMatch(attribute -> attribute.getName().equals(simpleMember))
              || methods(type.getName()).stream().anyMatch(method -> method.getName().equals(simpleMember));
      if (!matches) {
        continue;
      }
      if (owner != null && owner != type) {
        return Optional.empty();
      }
      owner = type;
    }
    return Optional.ofNullable(owner);
  }

  public Set<String> typeNames() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(typesBySimpleName.keySet()));
  }

  private static <T> Map<String, List<T>> copyListMap(Map<String, List<T>> source) {
    Map<String, List<T>> copy = new LinkedHashMap<>();
    for (Map.Entry<String, List<T>> entry : source.entrySet()) {
      copy.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return Map.copyOf(copy);
  }

  private static Map<String, Set<String>> copySetMap(Map<String, Set<String>> source) {
    Map<String, Set<String>> copy = new LinkedHashMap<>();
    for (Map.Entry<String, Set<String>> entry : source.entrySet()) {
      copy.put(entry.getKey(), Set.copyOf(entry.getValue()));
    }
    return Map.copyOf(copy);
  }
}
