package de.monticore.codeAdaption.matcher.errorMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TMemberMatcher;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;

/***
 *throws an error when the previous strategies didn't find machining for attributes or a methods.
 */
public class ErrorTMemberMatcher implements TMemberMatcher {
  protected ASTCDCompilationUnit cd;
  protected TypeMatcher typeMatcher;

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public void setTypeMatcher(TypeMatcher matcher) {
    this.typeMatcher = matcher;
  }

  @Override
  public TypeMatcher getTypeMatcher() {
    return typeMatcher;
  }

  @Override
  public Optional<CodeMatching> getMatchedMethod(
      ASTTypeDeclaration type, ASTMethodDeclaration method) {

    String pos = AdapterUtils.getPosition(method.get_SourcePositionStart());
    Log.error(pos + " No Match found for the Method " + method.getName());
    assert false;
    return Optional.empty();
  }

  @Override
  public Optional<CodeMatching> getMatchedField(
      ASTTypeDeclaration type, ASTFieldDeclaration field) {

    String name = field.getVariableDeclarator(0).getDeclarator().getName();
    String pos = AdapterUtils.getPosition(field.get_SourcePositionStart());
    Log.error(pos + " No Match found for the Field " + name);
    assert false;
    return Optional.empty();
  }

  @Override
  public Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype) {
    String name = JavaLoader.print(supertype);

    for (ASTTypeDeclaration typeDeclaration : typeMatcher.getAllTypeDeclarations()) {
      if (typeDeclaration.getName().equals(name)) {
        return Optional.of(new CodeMatching(true));
      }
    }

    String pos = AdapterUtils.getPosition(supertype.get_SourcePositionStart());
    Log.error(pos + " No Match found for the Type " + name);
    return Optional.empty();
  }
}
