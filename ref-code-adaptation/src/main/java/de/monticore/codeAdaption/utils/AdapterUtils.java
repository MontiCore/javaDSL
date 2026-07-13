package de.monticore.codeAdaption.utils;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.se_rwth.commons.logging.Log;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
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
    if (name == null || name.isEmpty()) {
      return Optional.empty();
    }

    // normalize - strip trailing parentheses used in some annotations like "update()"
    String n = name.trim();
    if (n.endsWith("()")) {
      n = n.substring(0, n.length() - 2);
    }
    Log.debug("AdapterUtils.resolveCDSymbol: resolving '" + name + "' -> '" + n + "'", "AdapterUtils");

    // handle qualified references like "Type.member" -> resolve member symbol from type AST
    if (n.contains(".")) {
      String[] parts = n.split("\\.", 2);
      String typeName = parts[0].trim();
      String memberName = parts[1].trim();
      Log.debug("AdapterUtils.resolveCDSymbol: qualified ref type='" + typeName + "' member='" + memberName + "'", "AdapterUtils");

      // Try to find the referenced type in the reference CD AST
      CDModelIndex index = CDModelIndex.of(refCD);
      if (memberName.contains("(")) {
        Optional<ISymbol> exactMethod =
            index.method(typeName, JavaSourceNames.normalizeMethodSignature(memberName))
                .map(ASTCDMethod::getSymbol)
                .map(symbol -> (ISymbol) symbol);
        if (exactMethod.isPresent()) {
          return exactMethod;
        }
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
      // not found as qualified member -> fallthrough to global lookup below
      n = memberName; // try resolving member name globally as a fallback
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

    mergeTypeRelationships(lefType, rightType);

    // Java overload identity is the method name plus normalized parameter types. A same-name
    // method with different parameters is a valid overload and must be retained.
    Map<String, ASTMethodDeclaration> leftMethodsBySignature = new LinkedHashMap<>();
    for (ASTMethodDeclaration method : lMethods) {
      leftMethodsBySignature.put(getMethodSignature(method), method);
    }
    for (ASTMethodDeclaration rMeth : rMethods) {
      String rSignature = getMethodSignature(rMeth);
      ASTMethodDeclaration existingMethod = leftMethodsBySignature.get(rSignature);

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
        leftMethodsBySignature.put(rSignature, rMeth);
      } else if (!normalizedReturnType(existingMethod).equals(normalizedReturnType(rMeth))
          && !preferLeftOnConflict) {
        throw new IllegalStateException(
            String.format(
                "Conflicting method '%s' in type '%s': return types differ between '%s' and '%s'",
                rSignature, typeName, leftSource, rightSource));
      }
    }

    // A nested type remains a member of its enclosing type. Add missing direct nested types, but
    // never expose them through the compilation-unit collector.
    if (lefType instanceof ASTClassDeclaration leftClass
        && rightType instanceof ASTClassDeclaration rightClass) {
      Set<String> leftNestedNames = new LinkedHashSet<>();
      for (var declaration : leftClass.getClassBody().getClassBodyDeclarationList()) {
        if (declaration instanceof ASTTypeDeclaration nested) {
          leftNestedNames.add(nested.getName());
        }
      }
      for (var declaration : rightClass.getClassBody().getClassBodyDeclarationList()) {
        if (declaration instanceof ASTTypeDeclaration nested
            && leftNestedNames.add(nested.getName())) {
          leftClass.getClassBody().addClassBodyDeclaration(declaration.deepClone());
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
      } else if (!normalizedFieldType(leftFieldsByName.get(rFieldName))
              .equals(normalizedFieldType(rf))
          && !preferLeftOnConflict) {
        throw new IllegalStateException(
            String.format(
                "Conflicting field '%s' in type '%s': types differ between '%s' and '%s'",
                rFieldName, typeName, leftSource, rightSource));
      }
    }

    return lefType;
  }

  private static void mergeTypeRelationships(
      ASTTypeDeclaration leftType, ASTTypeDeclaration rightType) {
    if (leftType instanceof ASTClassDeclaration leftClass
        && rightType instanceof ASTClassDeclaration rightClass) {
      if (!leftClass.isPresentSuperClass() && rightClass.isPresentSuperClass()) {
        leftClass.setSuperClass(rightClass.getSuperClass().deepClone());
      }
      mergeTypes(
          leftClass.getImplementedInterfaceList(), rightClass.getImplementedInterfaceList());
    } else if (leftType instanceof ASTInterfaceDeclaration leftInterface
        && rightType instanceof ASTInterfaceDeclaration rightInterface) {
      mergeTypes(
          leftInterface.getExtendedInterfaceList(), rightInterface.getExtendedInterfaceList());
    } else if (leftType instanceof ASTEnumDeclaration leftEnum
        && rightType instanceof ASTEnumDeclaration rightEnum) {
      mergeTypes(leftEnum.getImplementedInterfaceList(), rightEnum.getImplementedInterfaceList());
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

  private static void mergeTypes(List<ASTMCType> left, List<ASTMCType> right) {
    Set<String> existing =
        left.stream()
            .map(JavaSourceNames::printNormalizedType)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    right.stream()
        .filter(type -> existing.add(JavaSourceNames.printNormalizedType(type)))
        .map(ASTMCType::deepClone)
        .forEach(left::add);
  }

  /**
   * Get a method signature string for comparison.
   * Format: "methodName(paramType1,paramType2)"
   * Uses method name + parameter types (not return type) for comparison.
   */
  private static String getMethodSignature(ASTMethodDeclaration method) {
    List<String> parameterTypes = new ArrayList<>();
    if (method.getFormalParameters().isPresentFormalParameterListing()) {
      method.getFormalParameters().getFormalParameterListing().getFormalParameterList()
          .forEach(p -> parameterTypes.add(JavaSourceNames.printNormalizedType(p.getMCType())));
    }
    return method.getName() + "(" + String.join(",", parameterTypes) + ")";
  }

  private static String normalizedReturnType(ASTMethodDeclaration method) {
    return JavaSourceNames.normalizeType(JavaLoader.print(method.getMCReturnType()));
  }

  private static String normalizedFieldType(ASTFieldDeclaration field) {
    return JavaSourceNames.printNormalizedType(field.getMCType());
  }

}
