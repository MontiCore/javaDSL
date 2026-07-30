package de.monticore.codeAdaption.utils.visitors;

import static de.monticore.codeAdaption.utils.Constants.ANNOT_PACKAGE;

import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mccommonstatements._ast.ASTJavaModifier;
import de.monticore.statements.mccommonstatements._visitor.MCCommonStatementsVisitor2;
import de.monticore.statements.mcstatementsbasis._ast.ASTMCModifier;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.statements.mcvardeclarationstatements._visitor.MCVarDeclarationStatementsVisitor2;
import java.util.List;
import java.util.Optional;

/***
 * this Visitor remove info-Annotation form the reference code.
 */
public class AnnotationRemover
    implements JavaDSLVisitor2,
        JavaLightVisitor2,
        MCCommonStatementsVisitor2,
        MCVarDeclarationStatementsVisitor2 {

  @Override
  public void visit(ASTOrdinaryCompilationUnit node) {
    Optional<ASTImportDeclaration> importDec =
        node.getImportDeclarationList().stream()
            .filter(i -> i.getMCQualifiedName().getQName().equals(ANNOT_PACKAGE))
            .findFirst();
    importDec.map(node.getImportDeclarationList()::remove);
  }

  @Override
  public void visit(ASTClassDeclaration node) {
    this.removeJavaInfoAnnot(node.getJavaModifierList());
  }

  @Override
  public void visit(ASTInterfaceDeclaration node) {
    this.removeJavaInfoAnnot(node.getJavaModifierList());
  }

  @Override
  public void visit(ASTEnumDeclaration node) {
    this.removeJavaInfoAnnot(node.getJavaModifierList());
  }

  @Override
  public void visit(ASTMethodDeclaration node) {
    removeInfoAnnot(node.getMCModifierList());
  }

  @Override
  public void visit(ASTFieldDeclaration node) {
    this.removeJavaInfoAnnot(node.getJavaModifierList());
  }

  @Override
  public void visit(ASTFormalParameter param) {
    removeInfoAnnot(param.getMCModifierList());
  }

  @Override
  public void visit(ASTLocalVariableDeclaration node) {
    removeInfoAnnot(node.getMCModifierList());
  }

  private void removeJavaInfoAnnot(List<ASTJavaModifier> modifiers) {
    MatcherHelper.getInfoJavaAnnot(modifiers).map(modifiers::remove);
  }

  private void removeInfoAnnot(List<ASTMCModifier> modifiers) {
    MatcherHelper.getInfoAnnotation(modifiers).map(modifiers::remove);
  }
}
