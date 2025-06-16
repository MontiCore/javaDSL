package de.monticore.codeAdaption.utils;

import static de.monticore.codeAdaption.utils.JavaLoader.print;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdapterUtils {

  public static Set<ASTCDType> getAllCDTypes(ASTCDCompilationUnit cd) {
    Set<ASTCDType> res = new HashSet<>();
    res.addAll(cd.getCDDefinition().getCDClassesList());
    res.addAll(cd.getCDDefinition().getCDInterfacesList());
    res.addAll(cd.getCDDefinition().getCDEnumsList());
    return res;
  }

  public static Optional<ISymbol> resolveCDSymbol(String name, ASTCDCompilationUnit refCD) {
    List<FieldSymbol> symbol = refCD.getEnclosingScope().resolveFieldMany(name);
    if (!symbol.isEmpty()) {
      return Optional.of(symbol.iterator().next());
    }
    return refCD.getEnclosingScope().resolveCDType(name).map(s -> (ISymbol) s).stream().findAny();
  }

  public static String getFileName(ASTOrdinaryCompilationUnit ast) {
    return ast.get_SourcePositionStart().getFileName().orElse("");
  }

  public static String getPosition(SourcePosition pos) {
    if (pos.getFileName().isPresent()) {
      String fileName = new File(pos.getFileName().get()).getName();
      return fileName + " <" + pos.getLine() + "," + pos.getColumn() + ">";
    }
    return "";
  }
  
  /**
   * Reads a Java file and removes multi-line and single-line comments.
   *
   * @param file The Java file from which comments will be removed.
   */
  public static void removeComments(File file) {

    // remove single line comments
    String code = JavaLoader.readFileContent(file).replaceAll("//.*", "");

    // remove multilines comments
    Pattern pattern = Pattern.compile("/\\*(?s).*?\\*/");
    Matcher matcher = pattern.matcher(code);
    code = matcher.replaceAll("");

    // print code
    JavaLoader.writeFile(file.toPath(), code);
  }

  public static ASTOrdinaryCompilationUnit mergeAsts(
      ASTOrdinaryCompilationUnit leftAST, ASTOrdinaryCompilationUnit rightAST) {

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

  private static ASTTypeDeclaration mergeTypeDeclaration(
      JavaAstElemCollector lCollector,
      ASTTypeDeclaration lefType,
      JavaAstElemCollector rCollector,
      ASTTypeDeclaration rightType) {

    // merge methods
    for (ASTMethodDeclaration rMeth : rCollector.getAllMethodDeclarations(rightType)) {
      Optional<ASTMethodDeclaration> lMeth =
          lCollector.getAllMethodDeclarations(lefType).stream()
              .filter(m -> compare(m, rMeth))
              .findAny();

      if (lMeth.isEmpty()) {
        if (lefType instanceof ASTClassDeclaration) {
          ((ASTClassDeclaration) lefType).getClassBody().addClassBodyDeclaration(rMeth);
        } else if (rightType instanceof ASTInterfaceDeclaration) {
          ((ASTInterfaceDeclaration) lefType).getInterfaceBody().addInterfaceBodyDeclaration(rMeth);
        }
      }
    }

    // merge fields
    for (ASTFieldDeclaration rField : rCollector.getAllFieldDeclarations(rightType)) {

      Optional<ASTFieldDeclaration> lField =
          lCollector.getAllFieldDeclarations(lefType).stream()
              .filter(
                  f ->
                      f.getVariableDeclarator(0)
                          .getDeclarator()
                          .getName()
                          .equals(rField.getVariableDeclarator(0).getDeclarator().getName()))
              .findAny();

      if (lField.isEmpty()) {
        if (lefType instanceof ASTClassDeclaration) {
          ((ASTClassDeclaration) lefType).getClassBody().addClassBodyDeclaration(rField);
        }
      }
    }

    return lefType;
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
