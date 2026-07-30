package de.monticore.codeAdaption.matcher.annotMatcher;

import static de.monticore.codeAdaption.matcher.MatcherHelper.*;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.java.javadsl._ast.*;
import de.monticore.javalight._ast.ASTAnnotation;

import java.util.Optional;
import java.util.Set;

/***
 * match a TypeDeclaration(class, enum, interface) in the reference code to elements in
 *  the reference class Diagram form annotations.
 */
public class AnnotTypeMatcher implements TypeMatcher {
  protected ASTCDCompilationUnit cd;
  Set<ASTTypeDeclaration> typeDeclarationSet;

  public AnnotTypeMatcher(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public Set<ASTTypeDeclaration> getAllTypeDeclarations() {
    return typeDeclarationSet;
  }

  @Override
  public void setAllTypeDeclarations(Set<ASTTypeDeclaration> typeDeclarationSet) {
    this.typeDeclarationSet = typeDeclarationSet;
  }

  @Override
  public Optional<CodeMatching> getMatchedType(ASTTypeDeclaration type) {

    Optional<ASTAnnotation> annotation = Optional.empty();
    if (type instanceof ASTClassDeclaration) {
      annotation = getInfoJavaAnnot(((ASTClassDeclaration) type).getJavaModifierList());
    } else if (type instanceof ASTInterfaceDeclaration) {
      annotation = getInfoJavaAnnot(((ASTInterfaceDeclaration) type).getJavaModifierList());
    } else if (type instanceof ASTEnumDeclaration) {
      annotation = getInfoJavaAnnot(((ASTEnumDeclaration) type).getJavaModifierList());
    }
    return annotation.map(annot -> mkMatchingFromAnnotation(annot, cd));
  }
}
