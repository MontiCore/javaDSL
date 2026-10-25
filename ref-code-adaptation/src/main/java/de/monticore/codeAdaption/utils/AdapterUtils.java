package de.monticore.codeAdaption.utils;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.se_rwth.commons.logging.Log;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.utils.visitors.AnnotationRemover;
import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.SourcePosition;
import java.io.File;
import java.util.*;

public class AdapterUtils {

  public static Set<ASTCDType> getAllCDTypes(ASTCDCompilationUnit cd) {
    return new LinkedHashSet<>(CDModelIndex.of(cd).types());
  }

  public static Optional<ISymbol> resolveCDSymbol(String name, ASTCDCompilationUnit refCD) {
    Optional<AdaptReference> parsed = AdaptReference.parse(name);
    if (parsed.isEmpty() || refCD == null) {
      return Optional.empty();
    }
    AdaptReference reference = parsed.get();
    String n = reference.memberName();
    CDModelIndex index = CDModelIndex.of(refCD);
    Log.debug("AdapterUtils.resolveCDSymbol: resolving '" + name + "'", "AdapterUtils");

    if (reference.owner().isPresent()) {
      String typeName = reference.owner().get();
      String memberName = reference.memberName();
      Log.debug("AdapterUtils.resolveCDSymbol: qualified ref type='" + typeName + "' member='" + memberName + "'", "AdapterUtils");

      if (reference.isMethod()) {
        return index.method(typeName, reference.methodSignature().orElseThrow())
                .map(ASTCDMethod::getSymbol)
                .map(symbol -> (ISymbol) symbol);
      }
      Optional<ISymbol> attribute =
          index.attribute(typeName, memberName)
              .map(ASTCDAttribute::getSymbol)
              .map(symbol -> (ISymbol) symbol);
      if (attribute.isPresent()) {
        Log.debug("AdapterUtils.resolveCDSymbol: resolved to attribute '" + memberName + "' in type '" + typeName + "'", "AdapterUtils");
        return attribute;
      }
      List<ASTCDMethod> methods = index.methods(typeName, memberName);
      Optional<ISymbol> method =
          methods.size() == 1
              ? Optional.of(methods.get(0).getSymbol())
              : Optional.empty();
      if (method.isPresent()) {
        Log.debug("AdapterUtils.resolveCDSymbol: resolved to method '" + memberName + "' in type '" + typeName + "'", "AdapterUtils");
        return method;
      }
      if (methods.size() > 1) {
        Log.warn(
            "Adapter reference '"
                + name
                + "' is ambiguous because method '"
                + typeName
                + "."
                + memberName
                + "' is overloaded; include a signature-aware mapping");
        return Optional.empty();
      }
    } else if (reference.isMethod()) {
      List<ASTCDMethod> methods =
          index.types().stream()
              .map(type -> index.method(type.getName(), reference.methodSignature().orElseThrow()))
              .flatMap(Optional::stream)
              .toList();
      if (methods.size() == 1) {
        return Optional.of(methods.get(0).getSymbol());
      }
      if (methods.size() > 1) {
        Log.warn("Adapter reference '" + name + "' resolves to multiple methods");
      }
      return Optional.empty();
    }

    // try resolve as a field in the enclosing scope
    List<FieldSymbol> fields = refCD.getEnclosingScope().resolveFieldMany(n);
    if (fields.size() == 1) {
      Log.debug("AdapterUtils.resolveCDSymbol: resolved to field '" + n + "' in enclosing scope", "AdapterUtils");
      return Optional.of(fields.iterator().next());
    }
    if (fields.size() > 1) {
      Log.warn("Adapter reference '" + name + "' resolves to multiple fields");
      return Optional.empty();
    }

    // try resolving a top-level CD type by name
    Optional<ISymbol> tSym = refCD.getEnclosingScope().resolveCDType(n).map(s -> (ISymbol) s).stream().findAny();
    if (tSym.isPresent()) {
      Log.debug("AdapterUtils.resolveCDSymbol: resolved to top-level type '" + n + "'", "AdapterUtils");
    } else {
      Log.debug("AdapterUtils.resolveCDSymbol: could not resolve '" + name + "' (normalized='" + n + "')", "AdapterUtils");
    }
    return tSym;
  }

  public static String getFileName(ASTOrdinaryCompilationUnit ast) {
    return ast.get_SourcePositionStart().getFileName().orElse("");
  }

  public static String getSimpleFileName(ASTOrdinaryCompilationUnit ast) {
    String fullPath = ast.get_SourcePositionStart().getFileName().orElse("");
    if (fullPath.isEmpty()) {
      return "";
    }
    return new java.io.File(fullPath).getName();
  }

  public static String getPosition(SourcePosition pos) {
    if (pos.getFileName().isPresent()) {
      String fileName = new File(pos.getFileName().get()).getName();
      return fileName + " <" + pos.getLine() + "," + pos.getColumn() + ">";
    }
    return "";
  }

  public static ASTOrdinaryCompilationUnit mergeAsts(
      ASTOrdinaryCompilationUnit leftAST, ASTOrdinaryCompilationUnit rightAST) {
    return mergeAsts(leftAST, rightAST, false);
  }

  /** Merges adapted members into authoritative concrete handwritten code. */
  public static ASTOrdinaryCompilationUnit mergeAstsPreferringLeft(
      ASTOrdinaryCompilationUnit leftAST, ASTOrdinaryCompilationUnit rightAST) {
    return mergeAsts(leftAST, rightAST, true);
  }

  private static ASTOrdinaryCompilationUnit mergeAsts(
      ASTOrdinaryCompilationUnit leftAST,
      ASTOrdinaryCompilationUnit rightAST,
      boolean preferLeftOnConflict) {

    String leftPackage = packageName(leftAST);
    String rightPackage = packageName(rightAST);
    if (!leftPackage.equals(rightPackage)) {
      throw new IllegalArgumentException(
          "Cannot merge Java compilation units from different packages: '"
              + leftPackage
              + "' ("
              + getFileName(leftAST)
              + ") and '"
              + rightPackage
              + "' ("
              + getFileName(rightAST)
              + ")");
    }

    TypeIdentityScope leftTypes = TypeIdentityScope.from(leftAST);
    TypeIdentityScope rightTypes = TypeIdentityScope.from(rightAST);
    mergeImports(leftAST, rightAST);

    JavaAstElemCollector lCollector = new JavaAstElemCollector();
    JavaDSLTraverser leftTraverser = JavaDSLMill.traverser();
    leftTraverser.add4JavaDSL(lCollector);
    leftAST.accept(leftTraverser);

    JavaAstElemCollector rCollector = new JavaAstElemCollector();
    JavaDSLTraverser rightTraverser = JavaDSLMill.traverser();
    rightTraverser.add4JavaDSL(rCollector);
    rightAST.accept(rightTraverser);

    // Only compilation-unit declarations participate here. Nested types belong to their enclosing
    // declaration and must never be promoted to top-level declarations during a merge.
    for (ASTTypeDeclaration right : rightAST.getTypeDeclarationList()) {
      Optional<ASTTypeDeclaration> lType =
          leftAST.getTypeDeclarationList().stream()
              .filter(t -> t.getName().equals(right.getName()))
              .findFirst();
      if (lType.isEmpty()) {
        leftAST.addTypeDeclaration(right.deepClone());
      } else {
        mergeTypeDeclaration(
            lCollector,
            lType.get(),
            rCollector,
            right,
            leftTypes,
            rightTypes,
            getFileName(leftAST),
            getFileName(rightAST),
            preferLeftOnConflict);
      }
    }

    return leftAST;
  }

  private static String packageName(ASTOrdinaryCompilationUnit unit) {
    return unit.isPresentPackageDeclaration()
        ? unit.getPackageDeclaration().getMCQualifiedName().getQName()
        : "";
  }

  private static void mergeImports(
      ASTOrdinaryCompilationUnit leftAST, ASTOrdinaryCompilationUnit rightAST) {
    Set<ImportIdentity> existingImports = new LinkedHashSet<>();
    Map<ImportSimpleName, String> explicitImportsBySimpleName = new LinkedHashMap<>();
    for (ASTImportDeclaration leftImport : leftAST.getImportDeclarationList()) {
      registerImport(leftImport, existingImports, explicitImportsBySimpleName);
    }

    for (ASTImportDeclaration rightImport : rightAST.getImportDeclarationList()) {
      if (registerImport(rightImport, existingImports, explicitImportsBySimpleName)) {
        leftAST.addImportDeclaration(rightImport.deepClone());
      }
    }
  }

  private static boolean registerImport(
      ASTImportDeclaration importDeclaration,
      Set<ImportIdentity> existingImports,
      Map<ImportSimpleName, String> explicitImportsBySimpleName) {
    String qualifiedName = importDeclaration.getMCQualifiedName().getQName();
    ImportIdentity identity =
        new ImportIdentity(qualifiedName, importDeclaration.isStatic(), importDeclaration.isSTAR());
    if (!existingImports.add(identity)) {
      return false;
    }
    if (identity.star()) {
      return true;
    }

    String simpleName = JavaSourceNames.simpleName(qualifiedName);
    ImportSimpleName key = new ImportSimpleName(simpleName, identity.isStatic());
    String conflict = explicitImportsBySimpleName.putIfAbsent(key, qualifiedName);
    if (conflict != null && !conflict.equals(qualifiedName)) {
      throw new IllegalStateException(
          "Cannot merge imports '"
              + conflict
              + "' and '"
              + qualifiedName
              + "' because both use the "
              + (identity.isStatic() ? "static member" : "type")
              + " name '"
              + simpleName
              + "'");
    }
    return true;
  }

  private record ImportIdentity(String qualifiedName, boolean isStatic, boolean star) {}

  private record ImportSimpleName(String name, boolean isStatic) {}

  private static ASTTypeDeclaration mergeTypeDeclaration(
      JavaAstElemCollector lCollector,
      ASTTypeDeclaration lefType,
      JavaAstElemCollector rCollector,
      ASTTypeDeclaration rightType,
      TypeIdentityScope leftTypes,
      TypeIdentityScope rightTypes,
      String leftSource,
      String rightSource,
      boolean preferLeftOnConflict) {

    // Get ordered lists of fields and methods from both sides
    List<ASTFieldDeclaration> lFields = lCollector.getAllFieldDeclarations(lefType);
    List<ASTFieldDeclaration> rFields = rCollector.getAllFieldDeclarations(rightType);
    List<ASTMethodDeclaration> lMethods = lCollector.getAllMethodDeclarations(lefType);
    List<ASTMethodDeclaration> rMethods = rCollector.getAllMethodDeclarations(rightType);

    String typeName = lefType.getName();

    if (!lefType.getClass().equals(rightType.getClass())) {
      if (preferLeftOnConflict) {
        return lefType;
      }
      throw new IllegalStateException(
          String.format(
              "Cannot merge type '%s': declaration kinds differ in '%s' and '%s'",
              typeName, leftSource, rightSource));
    }

    mergeTypeRelationships(
        lefType,
        rightType,
        leftTypes,
        rightTypes,
        leftSource,
        rightSource,
        preferLeftOnConflict);

    // Java overload identity is the method name plus normalized parameter types. A same-name
    // method with different parameters is a valid overload and must be retained.
    List<MethodEntry> leftMethodsBySignature = new ArrayList<>();
    for (ASTMethodDeclaration method : lMethods) {
      leftMethodsBySignature.add(new MethodEntry(methodIdentity(method, leftTypes), method));
    }
    for (ASTMethodDeclaration rMeth : rMethods) {
      MethodIdentity rightIdentity = methodIdentity(rMeth, rightTypes);
      List<MethodEntry> signatureMatches =
          leftMethodsBySignature.stream()
              .filter(entry -> entry.identity().collidesWith(rightIdentity))
              .toList();
      if (signatureMatches.size() > 1) {
        throw new IllegalStateException(
            "Ambiguous method identity '" + rightIdentity + "' in type '" + typeName + "'");
      }
      ASTMethodDeclaration existingMethod =
          signatureMatches.isEmpty() ? null : signatureMatches.get(0).method();

      if (existingMethod == null) {
        if (lefType instanceof ASTClassDeclaration) {
          ((ASTClassDeclaration) lefType)
              .getClassBody()
              .addClassBodyDeclaration(rMeth.deepClone());
        } else if (lefType instanceof ASTInterfaceDeclaration) {
          ((ASTInterfaceDeclaration) lefType)
              .getInterfaceBody()
              .addInterfaceBodyDeclaration(rMeth.deepClone());
        }
        leftMethodsBySignature.add(new MethodEntry(rightIdentity, rMeth));
      } else if (!preferLeftOnConflict
          && !equivalentWithoutAdaptationMetadata(existingMethod, rMeth)) {
        MergeOrigin existingOrigin = mergeOrigin(existingMethod);
        MergeOrigin incomingOrigin = mergeOrigin(rMeth);
        if (existingOrigin == incomingOrigin) {
          throw new IllegalStateException(
              String.format(
                  "Conflicting method '%s' in type '%s': declarations differ between '%s' and '%s'",
                  rightIdentity, typeName, leftSource, rightSource));
        }
        // A reference-derived declaration carries the Adapt marker that identifies it as the
        // authoritative template result. This is explicit provenance, not a name-based guess.
        if (incomingOrigin == MergeOrigin.REFERENCE_DERIVED) {
          ASTMethodDeclaration replacement =
              replaceMethodDeclaration(lefType, existingMethod, rMeth);
          int entryIndex = leftMethodsBySignature.indexOf(signatureMatches.get(0));
          leftMethodsBySignature.set(entryIndex, new MethodEntry(rightIdentity, replacement));
        }
      }
    }

    // A nested type remains a member of its enclosing type. Add missing direct nested types, but
    // never expose them through the compilation-unit collector.
    if (lefType instanceof ASTClassDeclaration leftClass
        && rightType instanceof ASTClassDeclaration rightClass) {
      Map<String, ASTTypeDeclaration> leftNestedTypes = new LinkedHashMap<>();
      for (var declaration : leftClass.getClassBody().getClassBodyDeclarationList()) {
        if (declaration instanceof ASTTypeDeclaration nested) {
          leftNestedTypes.put(nested.getName(), nested);
        }
      }
      for (var declaration : rightClass.getClassBody().getClassBodyDeclarationList()) {
        if (declaration instanceof ASTTypeDeclaration nested) {
          ASTTypeDeclaration existingNested = leftNestedTypes.get(nested.getName());
          if (existingNested == null) {
            leftNestedTypes.put(nested.getName(), nested);
            leftClass.getClassBody().addClassBodyDeclaration(declaration.deepClone());
          } else if (!preferLeftOnConflict
              && !equivalentWithoutAdaptationMetadata(existingNested, nested)) {
            throw new IllegalStateException(
                String.format(
                    "Conflicting nested type '%s.%s': declarations differ between '%s' and '%s'",
                    typeName, nested.getName(), leftSource, rightSource));
          }
        }
      }
    }

    // merge fields - compare by name for conflict detection / deduplication
    Map<String, ASTFieldDeclaration> leftFieldsByName = new LinkedHashMap<>();
    for (ASTFieldDeclaration lf : lFields) {
      String name = lf.getVariableDeclarator(0).getDeclarator().getName();
      leftFieldsByName.put(name, lf);
    }

    for (ASTFieldDeclaration rf : rFields) {
      String rFieldName = rf.getVariableDeclarator(0).getDeclarator().getName();
      if (!leftFieldsByName.containsKey(rFieldName)) {
        if (lefType instanceof ASTClassDeclaration) {
          ((ASTClassDeclaration) lefType)
              .getClassBody()
              .addClassBodyDeclaration(rf.deepClone());
        }
      } else if (!preferLeftOnConflict
          && !equivalentWithoutAdaptationMetadata(
              leftFieldsByName.get(rFieldName), rf)) {
        throw new IllegalStateException(
            String.format(
                "Conflicting field '%s' in type '%s': declarations differ between '%s' and '%s'",
                rFieldName, typeName, leftSource, rightSource));
      }
    }

    return lefType;
  }

  private static void mergeTypeRelationships(
      ASTTypeDeclaration leftType,
      ASTTypeDeclaration rightType,
      TypeIdentityScope leftTypes,
      TypeIdentityScope rightTypes,
      String leftSource,
      String rightSource,
      boolean preferLeftOnConflict) {
    if (leftType instanceof ASTClassDeclaration leftClass
        && rightType instanceof ASTClassDeclaration rightClass) {
      mergeCompletedAbstractModifier(leftClass, rightClass);
      if (leftClass.isPresentSuperClass()
          && rightClass.isPresentSuperClass()
          && !leftTypes.identity(leftClass.getSuperClass())
              .sameTypeAs(rightTypes.identity(rightClass.getSuperClass()))
          && !preferLeftOnConflict) {
        throw new IllegalStateException(
            String.format(
                "Conflicting superclass of type '%s': declarations differ between '%s' and '%s'",
                leftType.getName(), leftSource, rightSource));
      } else if (!leftClass.isPresentSuperClass() && rightClass.isPresentSuperClass()) {
        leftClass.setSuperClass(rightClass.getSuperClass().deepClone());
      }
      mergeTypes(
          leftClass.getImplementedInterfaceList(),
          rightClass.getImplementedInterfaceList(),
          leftTypes,
          rightTypes);
    } else if (leftType instanceof ASTInterfaceDeclaration leftInterface
        && rightType instanceof ASTInterfaceDeclaration rightInterface) {
      mergeTypes(
          leftInterface.getExtendedInterfaceList(),
          rightInterface.getExtendedInterfaceList(),
          leftTypes,
          rightTypes);
    } else if (leftType instanceof ASTEnumDeclaration leftEnum
        && rightType instanceof ASTEnumDeclaration rightEnum) {
      mergeTypes(
          leftEnum.getImplementedInterfaceList(),
          rightEnum.getImplementedInterfaceList(),
          leftTypes,
          rightTypes);
      Set<String> constants =
          leftEnum.getEnumConstantDeclarationList().stream()
              .map(constant -> constant.getName())
              .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
      for (int index = 0; index < rightEnum.getEnumConstantDeclarationList().size(); index++) {
        var constant = rightEnum.getEnumConstantDeclaration(index);
        if (constants.add(constant.getName())) {
          leftEnum
              .getEnumConstantDeclarationList()
              .add(Math.min(index, leftEnum.sizeEnumConstantDeclarations()), constant.deepClone());
        }
      }
    }
  }

  /**
   * The concrete source is authoritative for handwritten members, while the adapted declaration's
   * abstractness has already been normalized to the final target CD. Preserve that structural
   * modifier when the adapted declaration is merged into an existing concrete class.
   */
  private static void mergeCompletedAbstractModifier(
      ASTClassDeclaration concreteClass, ASTClassDeclaration adaptedClass) {
    boolean adaptedIsAbstract =
        adaptedClass.getJavaModifierList().stream()
            .anyMatch(modifier -> "abstract".equals(JavaLoader.print(modifier).trim()));
    boolean concreteIsAbstract =
        concreteClass.getJavaModifierList().stream()
            .anyMatch(modifier -> "abstract".equals(JavaLoader.print(modifier).trim()));
    if (!adaptedIsAbstract) {
      concreteClass
          .getJavaModifierList()
          .removeIf(modifier -> "abstract".equals(JavaLoader.print(modifier).trim()));
      return;
    }
    if (concreteIsAbstract) {
      return;
    }
    adaptedClass.getJavaModifierList().stream()
        .filter(modifier -> "abstract".equals(JavaLoader.print(modifier).trim()))
        .findFirst()
        .map(modifier -> modifier.deepClone())
        .ifPresent(concreteClass::addJavaModifier);
  }

  private static void mergeTypes(
      List<ASTMCType> left,
      List<ASTMCType> right,
      TypeIdentityScope leftTypes,
      TypeIdentityScope rightTypes) {
    List<TypeIdentity> existing =
        left.stream().map(leftTypes::identity).collect(java.util.stream.Collectors.toList());
    for (ASTMCType candidate : right) {
      TypeIdentity identity = rightTypes.identity(candidate);
      if (existing.stream().noneMatch(current -> current.sameTypeAs(identity))) {
        left.add(candidate.deepClone());
        existing.add(identity);
      }
    }
  }

  /**
   * Get a method signature string for comparison.
   * Format: "methodName(paramType1,paramType2)"
   * Uses method name + parameter types (not return type) for comparison.
   */
  private static MethodIdentity methodIdentity(
      ASTMethodDeclaration method, TypeIdentityScope types) {
    List<TypeIdentity> parameterTypes = new ArrayList<>();
    if (method.getFormalParameters().isPresentFormalParameterListing()) {
      method.getFormalParameters().getFormalParameterListing().getFormalParameterList()
          .forEach(parameter -> parameterTypes.add(types.identity(parameter.getMCType())));
    }
    return new MethodIdentity(method.getName(), List.copyOf(parameterTypes));
  }

  private static boolean equivalentWithoutAdaptationMetadata(
      ASTNode left, ASTNode right) {
    ASTNode cleanedLeft = left.deepClone();
    ASTNode cleanedRight = right.deepClone();
    removeAdaptationMetadata(cleanedLeft);
    removeAdaptationMetadata(cleanedRight);
    return cleanedLeft.deepEquals(cleanedRight, true);
  }

  private record MethodEntry(MethodIdentity identity, ASTMethodDeclaration method) {}

  private enum MergeOrigin {
    REFERENCE_DERIVED,
    GENERATED_OR_CONCRETE
  }

  private static MergeOrigin mergeOrigin(ASTMethodDeclaration method) {
    return MatcherHelper.getInfoAnnotation(method.getMCModifierList()).isPresent()
        ? MergeOrigin.REFERENCE_DERIVED
        : MergeOrigin.GENERATED_OR_CONCRETE;
  }

  private static ASTMethodDeclaration replaceMethodDeclaration(
      ASTTypeDeclaration owner,
      ASTMethodDeclaration existing,
      ASTMethodDeclaration replacement) {
    ASTMethodDeclaration replacementCopy = replacement.deepClone();
    List<? extends ASTNode> declarations;
    if (owner instanceof ASTClassDeclaration classDeclaration) {
      declarations = classDeclaration.getClassBody().getClassBodyDeclarationList();
      int index = declarations.indexOf(existing);
      if (index >= 0) {
        classDeclaration.getClassBody().getClassBodyDeclarationList().set(index, replacementCopy);
        return replacementCopy;
      }
    } else if (owner instanceof ASTInterfaceDeclaration interfaceDeclaration) {
      declarations = interfaceDeclaration.getInterfaceBody().getInterfaceBodyDeclarationList();
      int index = declarations.indexOf(existing);
      if (index >= 0) {
        interfaceDeclaration
            .getInterfaceBody()
            .getInterfaceBodyDeclarationList()
            .set(index, replacementCopy);
        return replacementCopy;
      }
    }
    throw new IllegalStateException(
        "Cannot replace method '"
            + methodIdentity(existing, TypeIdentityScope.empty())
            + "' because it is not a direct member of type '"
            + owner.getName()
            + "'");
  }

  private record MethodIdentity(String name, List<TypeIdentity> parameters) {
    private boolean collidesWith(MethodIdentity other) {
      if (!name.equals(other.name) || parameters.size() != other.parameters.size()) {
        return false;
      }
      for (int index = 0; index < parameters.size(); index++) {
        if (!parameters.get(index).sameErasureAs(other.parameters.get(index))) {
          return false;
        }
      }
      return true;
    }

    @Override
    public String toString() {
      return name
          + "("
          + parameters.stream()
              .map(TypeIdentity::canonical)
              .collect(java.util.stream.Collectors.joining(","))
          + ")";
    }
  }

  private record TypeIdentity(String canonical, String simpleName, boolean resolved) {
    private boolean sameTypeAs(TypeIdentity other) {
      return canonical.equals(other.canonical);
    }

    private boolean sameErasureAs(TypeIdentity other) {
      if (canonical.equals(other.canonical)) {
        return true;
      }
      if (resolved && other.resolved) {
        return false;
      }
      // An unresolved same-leaf type might denote the qualified type. Treat it as a collision so
      // the merger fails safely instead of emitting an illegal duplicate-erasure overload.
      return simpleName.equals(other.simpleName);
    }
  }

  /** Import-aware, parser-backed type identities for one original compilation unit. */
  private record TypeIdentityScope(
      Map<String, Set<String>> explicitImports, Set<String> wildcardPackages) {

    private static TypeIdentityScope empty() {
      return new TypeIdentityScope(Map.of(), Set.of());
    }

    private static TypeIdentityScope from(ASTOrdinaryCompilationUnit unit) {
      Map<String, Set<String>> explicit = new LinkedHashMap<>();
      Set<String> wildcards = new LinkedHashSet<>();
      for (ASTImportDeclaration declaration : unit.getImportDeclarationList()) {
        if (declaration.isStatic()) {
          continue;
        }
        String imported = declaration.getMCQualifiedName().getQName();
        if (declaration.isSTAR()) {
          wildcards.add(imported);
        } else {
          explicit
              .computeIfAbsent(JavaSourceNames.simpleName(imported), ignored -> new LinkedHashSet<>())
              .add(imported);
        }
      }
      return new TypeIdentityScope(
          explicit.entrySet().stream()
              .collect(
                  java.util.stream.Collectors.toUnmodifiableMap(
                      Map.Entry::getKey, entry -> Set.copyOf(entry.getValue()))),
          Set.copyOf(wildcards));
    }

    private TypeIdentity identity(ASTMCType type) {
      String resolvedSource =
          JavaSourceNames.replaceTypeNames(
              JavaSourceNames.printQualifiedType(type), this::resolveReference);
      JavaSourceNames.ErasedType erased =
          JavaSourceNames.erasedType(resolvedSource)
              .orElseThrow(
                  () -> new IllegalStateException("Cannot parse Java type '" + resolvedSource + "'"));
      String canonical =
          erased.name().replace('$', '.') + "[]".repeat(erased.arrayDimensions());
      boolean resolved =
          erased.name().contains(".") || isPrimitiveOrVoid(erased.name());
      return new TypeIdentity(
          canonical,
          JavaSourceNames.simpleName(erased.name()) + "[]".repeat(erased.arrayDimensions()),
          resolved);
    }

    private Optional<String> resolveReference(JavaSourceNames.TypeReferenceName reference) {
      if (reference.qualified() || isPrimitiveOrVoid(reference.originalName())) {
        return Optional.empty();
      }
      Set<String> explicit = explicitImports.get(reference.simpleName());
      if (explicit != null && !explicit.isEmpty()) {
        return uniqueBinding(reference.simpleName(), explicit);
      }
      LinkedHashSet<String> classpathBindings = new LinkedHashSet<>();
      String javaLang = "java.lang." + reference.simpleName();
      if (classpathTypeExists(javaLang)) {
        classpathBindings.add(javaLang);
      }
      for (String wildcardPackage : wildcardPackages) {
        String candidate = wildcardPackage + "." + reference.simpleName();
        if (classpathTypeExists(candidate)) {
          classpathBindings.add(candidate);
        }
      }
      return uniqueBinding(reference.simpleName(), classpathBindings);
    }

    private static Optional<String> uniqueBinding(String simpleName, Set<String> bindings) {
      if (bindings.isEmpty()) {
        return Optional.empty();
      }
      if (bindings.size() == 1) {
        return Optional.of(bindings.iterator().next());
      }
      throw new IllegalStateException(
          "Ambiguous Java type '" + simpleName + "' resolves to " + bindings);
    }

    private static boolean classpathTypeExists(String qualifiedName) {
      try {
        Class.forName(qualifiedName, false, AdapterUtils.class.getClassLoader());
        return true;
      } catch (ClassNotFoundException | LinkageError ignored) {
        return false;
      }
    }

    private static boolean isPrimitiveOrVoid(String name) {
      return Set.of(
              "boolean", "byte", "short", "int", "long", "float", "double", "char", "void")
          .contains(name);
    }
  }

  private static void removeAdaptationMetadata(ASTNode node) {
    AnnotationRemover remover = new AnnotationRemover();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(remover);
    traverser.add4JavaLight(remover);
    traverser.add4MCCommonStatements(remover);
    traverser.add4MCVarDeclarationStatements(remover);
    node.accept(traverser);
  }

}
