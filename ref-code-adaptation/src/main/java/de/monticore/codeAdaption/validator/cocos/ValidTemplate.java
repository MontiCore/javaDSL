package de.monticore.codeAdaption.validator.cocos;

import static de.monticore.codeAdaption.matcher.MatcherHelper.*;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.java.javadsl._cocos.JavaDSLASTLocalVariableDeclarationCoCo;
import de.monticore.java.javadsl._cocos.JavaDSLASTTypeDeclarationCoCo;
import de.monticore.javalight._ast.ASTAnnotation;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._cocos.JavaLightASTMethodDeclarationCoCo;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mccommonstatements._ast.ASTJavaModifier;
import de.monticore.statements.mccommonstatements._cocos.MCCommonStatementsASTFormalParameterCoCo;
import de.monticore.statements.mcstatementsbasis._ast.ASTMCModifier;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValidTemplate
    implements JavaDSLASTFieldDeclarationCoCo,
        JavaDSLASTTypeDeclarationCoCo,
        JavaLightASTMethodDeclarationCoCo,
        MCCommonStatementsASTFormalParameterCoCo,
        JavaDSLASTLocalVariableDeclarationCoCo {
  protected ASTCDCompilationUnit cd;

  public ValidTemplate(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  protected String message =
      "0xRC003 %s invalid annotation template: filling template [%s] with the references "
          + " produce [%s]. But expected [%s].\n"
          + "allowed templates placeholder are {${},${cap_first} and ${uncap_first} }";

  @Override
  public void check(ASTMethodDeclaration node) {
    Optional<ASTAnnotation> annotation = getInfoAnnotation(node.getMCModifierList());
    annotation.ifPresent(astJavaAnnotation -> checkTemplate(astJavaAnnotation, node.getName()));
  }

  @Override
  public void check(ASTFieldDeclaration node) {
    String srcName = node.getVariableDeclarator(0).getDeclarator().getName();
    Optional<ASTAnnotation> annotation = getInfoJavaAnnot(node.getJavaModifierList());
    annotation.ifPresent(astJavaAnnotation -> checkTemplate(astJavaAnnotation, srcName));
  }

  @Override
  public void check(ASTTypeDeclaration node) {
    Optional<ASTAnnotation> annotation = Optional.empty();
    if (node instanceof ASTClassDeclaration) {
      annotation = getInfoJavaAnnot(((ASTClassDeclaration) node).getJavaModifierList());
    } else if (node instanceof ASTInterfaceDeclaration) {
      annotation = getInfoJavaAnnot(((ASTInterfaceDeclaration) node).getJavaModifierList());
    } else if (node instanceof ASTEnumDeclaration) {
      annotation = getInfoJavaAnnot(((ASTEnumDeclaration) node).getJavaModifierList());
    }

    annotation.ifPresent(astJavaAnnotation -> checkTemplate(astJavaAnnotation, node.getName()));
  }

  @Override
  public void check(ASTFormalParameter node) {
    Optional<ASTAnnotation> annotation = getInfoAnnotation(node.getMCModifierList());
    annotation.ifPresent(
        astJavaAnnotation -> checkTemplate(astJavaAnnotation, node.getDeclarator().getName()));
  }

  @Override
  public void check(ASTLocalVariableDeclaration node) {
    String srcName = node.getVariableDeclarator(0).getDeclarator().getName();
    Optional<ASTAnnotation> annotation = getInfoAnnotation(node.getMCModifierList());
    annotation.ifPresent(astJavaAnnotation -> checkTemplate(astJavaAnnotation, srcName));
  }

  public void checkTemplate(ASTMCModifier annotation, String sourceNAme) {
    String pos = AdapterUtils.getPosition(annotation.get_SourcePositionStart());
    CodeMatching matching = mkMatchingFromAnnotation(annotation, cd);
    if (matching.getTemplate() != null) {
      String computedName =
          MatcherHelper.fillTemplate(matching.getTemplate(), matching.getReferences());
      if (!computedName.equals(sourceNAme)) {
        Log.error(String.format(message, pos, matching.getTemplate(), computedName, sourceNAme));
      }
    }
  }
}
