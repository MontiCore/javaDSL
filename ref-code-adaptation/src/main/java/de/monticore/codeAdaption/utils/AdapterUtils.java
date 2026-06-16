package de.monticore.codeAdaption.utils;

import static de.monticore.codeAdaption.utils.JavaLoader.print;

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
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.SourcePosition;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import spoon.Launcher;
import spoon.reflect.code.CtComment;
import spoon.reflect.visitor.filter.TypeFilter;

public class AdapterUtils {

  public static Set<ASTCDType> getAllCDTypes(ASTCDCompilationUnit cd) {
    Set<ASTCDType> res = new LinkedHashSet<>();
    res.addAll(cd.getCDDefinition().getCDClassesList());
    res.addAll(cd.getCDDefinition().getCDInterfacesList());
    res.addAll(cd.getCDDefinition().getCDEnumsList());
    return res;
  }

  public static Optional<ISymbol> resolveCDSymbol(String name, ASTCDCompilationUnit refCD) {
    if (name == null || name.isEmpty()) {
      return Optional.empty();
    }

    // TODO: Rework inm next iteration, prone to errors
    // normalize - strip trailing parentheses used in some annotations like "update()"
    String n = name.trim().replaceAll("\\(\\)$", "");
    Log.debug("AdapterUtils.resolveCDSymbol: resolving '" + name + "' -> '" + n + "'", "AdapterUtils");

    // handle qualified references like "Type.member" -> resolve member symbol from type AST
    if (n.contains(".")) {
      String[] parts = n.split("\\.", 2);
      String typeName = parts[0].trim();
      String memberName = parts[1].trim();
      Log.debug("AdapterUtils.resolveCDSymbol: qualified ref type='" + typeName + "' member='" + memberName + "'", "AdapterUtils");

      // Try to find the referenced type in the reference CD AST
      for (ASTCDType t : getAllCDTypes(refCD)) {
        if (t.getName().equals(typeName)) {
          // search attributes
          for (ASTCDAttribute attr : t.getCDAttributeList()) {
            if (attr.getName().equals(memberName) && attr.getSymbol() != null) {
              Log.debug("AdapterUtils.resolveCDSymbol: resolved to attribute '" + memberName + "' in type '" + typeName + "'", "AdapterUtils");
              return Optional.of(attr.getSymbol());
            }
          }
          // search methods
          for (ASTCDMethod m : t.getCDMethodList()) {
            if (m.getName().equals(memberName) && m.getSymbol() != null) {
              Log.debug("AdapterUtils.resolveCDSymbol: resolved to method '" + memberName + "' in type '" + typeName + "'", "AdapterUtils");
              return Optional.of(m.getSymbol());
            }
          }
        }
      }
      // not found as qualified member -> fallthrough to global lookup below
      n = memberName; // try resolving member name globally as a fallback
    }

    // try resolve as a field in the enclosing scope
    List<FieldSymbol> fields = refCD.getEnclosingScope().resolveFieldMany(n);
    if (!fields.isEmpty()) {
      Log.debug("AdapterUtils.resolveCDSymbol: resolved to field '" + n + "' in enclosing scope", "AdapterUtils");
      return Optional.of(fields.iterator().next());
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

  /**
   * Removes comments from a Java source file through Spoon's model instead of source regexes.
   *
   * @param file The Java file from which comments will be removed.
   */
  public static void removeComments(File file) {
    Path tempDir = null;
    try {
      tempDir = Files.createTempDirectory("ref-code-adaptation-comments");
      Launcher launcher = new Launcher();
      launcher.getEnvironment().setNoClasspath(true);
      launcher.addInputResource(file.getAbsolutePath());
      launcher.buildModel();
      List<CtComment> comments =
          new ArrayList<>(launcher.getModel().getElements(new TypeFilter<>(CtComment.class)));
      for (CtComment comment : comments) {
        comment.delete();
      }
      launcher.setSourceOutputDirectory(tempDir.toFile());
      launcher.prettyprint();

      Optional<Path> generated;
      try (var paths = Files.walk(tempDir)) {
        generated =
            paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(file.getName()))
                .findFirst();
      }
      if (generated.isPresent()) {
        Files.copy(generated.get(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } else {
        Log.warn("Spoon did not produce a rewritten file for " + file.getAbsolutePath());
      }
    } catch (Exception e) {
      Log.error("It was not possible to remove comments from " + file.getAbsolutePath(), e);
    } finally {
      if (tempDir != null) {
        try (var paths = Files.walk(tempDir)) {
          for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
            Files.deleteIfExists(path);
          }
        } catch (IOException e) {
          Log.warn("Could not delete temporary comment-cleanup directory " + tempDir);
        }
      }
    }
  }

  public static ASTOrdinaryCompilationUnit mergeAsts(
      ASTOrdinaryCompilationUnit leftAST, ASTOrdinaryCompilationUnit rightAST) {

    mergeImports(leftAST, rightAST);

    JavaAstElemCollector lCollector = new JavaAstElemCollector();
    JavaDSLTraverser leftTraverser = JavaDSLMill.traverser();
    leftTraverser.add4JavaDSL(lCollector);
    leftAST.accept(leftTraverser);

    JavaAstElemCollector rCollector = new JavaAstElemCollector();
    JavaDSLTraverser rightTraverser = JavaDSLMill.traverser();
    rightTraverser.add4JavaDSL(rCollector);
    rightAST.accept(rightTraverser);

    for (ASTTypeDeclaration right : rCollector.getAllTypeDeclarations()) {
      Optional<ASTTypeDeclaration> lType =
          lCollector.getAllTypeDeclarations().stream()
              .filter(t -> t.getName().equals(right.getName()))
              .findAny();
      if (lType.isEmpty()) {
        leftAST.addTypeDeclaration(right);
      } else {
        leftAST.removeTypeDeclaration(lType.get());
        leftAST.addTypeDeclaration(
            mergeTypeDeclaration(lCollector, lType.get(), rCollector, right));
      }
    }

    return leftAST;
  }

  private static void mergeImports(
      ASTOrdinaryCompilationUnit leftAST, ASTOrdinaryCompilationUnit rightAST) {
    Set<String> existingImports = new LinkedHashSet<>();
    for (ASTImportDeclaration leftImport : leftAST.getImportDeclarationList()) {
      existingImports.add(leftImport.getMCQualifiedName().getQName());
    }

    for (ASTImportDeclaration rightImport : rightAST.getImportDeclarationList()) {
      String importName = rightImport.getMCQualifiedName().getQName();
      if (existingImports.add(importName)) {
        leftAST.addImportDeclaration(rightImport.deepClone());
      }
    }
  }

  private static ASTTypeDeclaration mergeTypeDeclaration(
      JavaAstElemCollector lCollector,
      ASTTypeDeclaration lefType,
      JavaAstElemCollector rCollector,
      ASTTypeDeclaration rightType) {

    // Get ordered lists of fields and methods from both sides
    List<ASTFieldDeclaration> lFields = lCollector.getAllFieldDeclarations(lefType);
    List<ASTFieldDeclaration> rFields = rCollector.getAllFieldDeclarations(rightType);
    List<ASTMethodDeclaration> lMethods = lCollector.getAllMethodDeclarations(lefType);
    List<ASTMethodDeclaration> rMethods = rCollector.getAllMethodDeclarations(rightType);

    String typeName = lefType.getName();

    // merge methods - compare by signature for conflict detection
    // Same signature = same method (possibly renamed differently by different patterns)
    for (ASTMethodDeclaration rMeth : rMethods) {
      String rSignature = getMethodSignature(rMeth);
      String rName = rMeth.getName();

      Optional<ASTMethodDeclaration> existingMethod = lMethods.stream()
          .filter(m -> getMethodSignature(m).equals(rSignature))
          .findAny();

      if (existingMethod.isEmpty()) {
        boolean concreteMethodWithSameNameExists =
            lMethods.stream().anyMatch(m -> m.getName().equals(rName));
        if (concreteMethodWithSameNameExists) {
          continue;
        }
        // No conflict - add method from right
        if (lefType instanceof ASTClassDeclaration) {
          ((ASTClassDeclaration) lefType).getClassBody().addClassBodyDeclaration(rMeth);
        } else if (rightType instanceof ASTInterfaceDeclaration) {
          ((ASTInterfaceDeclaration) lefType).getInterfaceBody().addInterfaceBodyDeclaration(rMeth);
        }
      } else {
        // Conflict detected: same method signature but possibly different names
        String lName = existingMethod.get().getName();
        if (!lName.equals(rName)) {
          throw new IllegalStateException(
              String.format("Conflict detected in type '%s': method with signature '%s' " +
                  "was renamed to '%s' by first mapping and '%s' by second mapping. " +
                  "Conflicting renames are not allowed.",
                  typeName, rSignature, lName, rName));
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
        // Field does not exist in left - add it
        if (lefType instanceof ASTClassDeclaration) {
          ((ASTClassDeclaration) lefType).getClassBody().addClassBodyDeclaration(rf);
        }
      }
    }

    return lefType;
  }

  /**
   * Get a method signature string for comparison.
   * Format: "methodName(paramType1,paramType2)"
   * Uses method name + parameter types (not return type) for comparison.
   */
  private static String getMethodSignature(ASTMethodDeclaration method) {
    StringBuilder sig = new StringBuilder();
    sig.append(method.getName()).append("(");
    if (method.getFormalParameters().isPresentFormalParameterListing()) {
      method.getFormalParameters().getFormalParameterListing().getFormalParameterList()
          .forEach(p -> sig.append(JavaLoader.print(p.getMCType())).append(","));
    }
    sig.append(")");
    return sig.toString();
  }

  /***
   * Compare two method (names and types) and return true when the method are the same.
   * @param leftMethod the left method.
   * @param rightMethod the right method.
   * @return true if both method are the same.
   */
  protected static boolean compare(
      ASTMethodDeclaration leftMethod, ASTMethodDeclaration rightMethod) {
    // compare names
    if (!leftMethod.getName().equals(rightMethod.getName())) {
      return false;
    }

    // is present parameters ?
    if (!leftMethod.getFormalParameters().isPresentFormalParameterListing()) {
      return !rightMethod.getFormalParameters().isPresentFormalParameterListing();
    }

    List<ASTFormalParameter> leftParams =
        leftMethod.getFormalParameters().getFormalParameterListing().getFormalParameterList();
    List<ASTFormalParameter> rightParams =
        rightMethod.getFormalParameters().getFormalParameterListing().getFormalParameterList();

    // same number of parameters ?
    if (leftParams.size() != rightParams.size()) {
      return false;
    }

    // parameters have the same type ?
    for (int i = 0; i < leftParams.size(); i++) {
      if (!print(leftParams.get(i).getMCType()).equals(print(rightParams.get(i).getMCType()))) {
        return false;
      }
    }

    return true;
  }
}
