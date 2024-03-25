/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.java2cd;

import de.monticore.cd.facade.*;
import de.monticore.cd.methodtemplates.CD4C;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4code._symboltable.CD4CodeSymbolTableCompleter;
import de.monticore.cd4codebasis._ast.ASTCDConstructor;
import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.*;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTConstDeclaration;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTSimpleInit;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.stream.Collectors;

import static de.monticore.cd.codegen.CD2JavaTemplates.EMPTY_BODY;
import static de.monticore.cd.facade.CDModifier.PUBLIC;
import static de.monticore.cd.facade.CDModifier.valueOf;

public class Java2CDVisitor implements JavaDSLVisitor2, JavaLightVisitor2 {

  protected ASTCDCompilationUnit cdCompilationUnit;
  protected ASTCDClass cdClass;
  protected ASTCDInterface cdInterface;
  protected ASTCDEnum cdEnum;
  protected ASTCDType currentType;

  protected final GlobalExtensionManagement glex;

  protected final CD4C cd4C;

  public Java2CDVisitor(GlobalExtensionManagement glex) {
    this.glex = glex;
    this.cd4C = CD4C.getInstance();

    ASTCDDefinition definition = CD4CodeMill.cDDefinitionBuilder()
        .setModifier(PUBLIC.build())
        .setName("Generated")
        .build();

    this.cdCompilationUnit = CD4CodeMill.cDCompilationUnitBuilder()
        .setCDDefinition(definition)
        .build();
  }

  @Override
  public void visit(ASTClassDeclaration ast) {
    // type parameters
    ASTCDClassBuilder classBuilder = CD4CodeMill.cDClassBuilder()
        .setModifier(PUBLIC.build()) // <- fix this
        .setName(ast.getName())
        .setCDInterfaceUsage(CDInterfaceUsageFacade.getInstance()
            .createCDInterfaceUsage(
                ast.getImplementedInterfaceList()
                    .stream()
                    .map(ASTMCType::printType)
                    .toArray(String[]::new)));

    if (ast.isPresentSuperClass()) {
      classBuilder = classBuilder
          .setCDExtendUsage(CDExtendUsageFacade.getInstance()
              .createCDExtendUsage(ast.getSuperClass().printType()));
    }

    cdClass = classBuilder.build();
    currentType = cdClass;
    cdCompilationUnit.getCDDefinition().addCDElement(cdClass);
  }

  @Override
  public void visit(ASTRecordDeclaration ast) {
    // type parameters
    cdClass = CD4CodeMill.cDClassBuilder()
        .setModifier(PUBLIC.build()) // <- fix this
        .setName(ast.getName())
        .setCDInterfaceUsage(CDInterfaceUsageFacade.getInstance()
            .createCDInterfaceUsage(
                ast.getImplementedInterfaceList()
                    .stream()
                    .map(ASTMCType::printType)
                    .toArray(String[]::new)))
        .build();

    currentType = cdClass;
    cdCompilationUnit.getCDDefinition().addCDElement(cdClass);
  }

  @Override
  public void visit(ASTRecordComponent ast) {
    currentType.addCDMember(CDAttributeFacade.getInstance().createAttribute(
        PUBLIC.build(), // <- fix this
        ast.getMCType(),
        ast.getName()));
  }

  @Override
  public void visit(ASTCompactConstructorDeclaration ast) {
    ASTCDConstructor constructor = CDConstructorFacade.getInstance()
        .createConstructor(PUBLIC.build(), ast.getName());

    StringBuilder methodBody = new StringBuilder();
    ast.getBody().getMCBlockStatementList().stream()
        .map(s -> CD4CodeMill.prettyPrint(s, true))
        .forEach(s -> methodBody.append(s).append("\n"));

    glex.replaceTemplate(EMPTY_BODY, constructor,
        new StringHookPoint(methodBody.toString()));
    currentType.addCDMember(constructor);
  }

  @Override
  public void visit(ASTInterfaceDeclaration ast) {
    //type parameters
    cdInterface = CD4CodeMill.cDInterfaceBuilder()
        .setModifier(PUBLIC.build()) // <- fix this
        .setName(ast.getName())
        .setCDExtendUsage(CDExtendUsageFacade.getInstance()
            .createCDExtendUsage(
                ast.getExtendedInterfaceList()
                    .stream()
                    .map(ASTMCType::printType)
                    .toArray(String[]::new)))
        .build();

    currentType = cdInterface;
    cdCompilationUnit.getCDDefinition().addCDElement(cdInterface);
  }

  @Override
  public void visit(ASTEnumDeclaration ast) {
    cdEnum = CD4CodeMill.cDEnumBuilder()
        .setModifier(PUBLIC.build()) // <- fix this
        .setName(ast.getName())
        .setCDInterfaceUsage(CDInterfaceUsageFacade.getInstance()
            .createCDInterfaceUsage(
                ast.getImplementedInterfaceList()
                    .stream()
                    .map(ASTMCType::printType)
                    .toArray(String[]::new)))
        .build();

    currentType = cdEnum;
    cdCompilationUnit.getCDDefinition().addCDElement(cdEnum);
  }

  @Override
  public void visit(ASTEnumConstantDeclaration ast) {
    cdEnum.addCDEnumConstant(
        CD4CodeMill.cDEnumConstantBuilder()
            .setName(ast.getName())
            .build()
    );
  }

  @Override
  public void visit(ASTFieldDeclaration ast) {
    ast.getVariableDeclaratorList().forEach(declarator ->
        currentType.addCDMember(CDAttributeFacade.getInstance().createAttribute(
            PUBLIC.build(), // <- fix this
            ast.getMCType(),
            declarator.getDeclarator().getName(),
            ((ASTSimpleInit) declarator.getVariableInit()).getExpression()
        )));
  }

  /*
  @Override
  public void visit(ASTPackageDeclaration ast) {
    cdCompilationUnit.setMCPackageDeclaration(
        CD4CodeMill.mCPackageDeclarationBuilder()
            .setMCQualifiedName(ast.getMCQualifiedName())
            .build());
  }

  @Override
  public void visit(ASTImportDeclaration ast) {
    cdCompilationUnit.addMCImportStatement(
        CD4CodeMill.mCImportStatementBuilder()
            .setMCQualifiedName(ast.getMCQualifiedName())
            .build());
  }
  */

  @Override
  public void visit(ASTMethodDeclaration ast) {
    ASTCDMethod method = CDMethodFacade.getInstance().createMethod(
        PUBLIC.build(), // <- fix this
        ast.getMCReturnType(),
        ast.getName());

    if (ast.getFormalParameters().isPresentFormalParameterListing()) {
      method = CDMethodFacade.getInstance().createMethodInternal(
          PUBLIC.build(), // <- fix this
          ast.getMCReturnType(),
          ast.getName(),
          false,
          ast.getFormalParameters()
              .getFormalParameterListing()
              .getFormalParameterList().stream()
              .map(p -> CDParameterFacade.getInstance()
                  .createParameter(
                      p.getMCType(), p.getDeclarator().getName()))
              .collect(Collectors.toList()),
          ast.getThrows().getMCQualifiedNameList());
    }

    StringBuilder methodBody = new StringBuilder();
    ast.getMCJavaBlock().getMCBlockStatementList().stream()
        .map(s -> CD4CodeMill.prettyPrint(s, true))
        .forEach(s -> methodBody.append(s).append("\n"));

    glex.replaceTemplate(EMPTY_BODY, method, new StringHookPoint(methodBody.toString()));
    currentType.addCDMember(method);
  }

  @Override
  public void visit(ASTConstructorDeclaration ast) {
    ASTCDConstructor method = CDConstructorFacade.getInstance().createConstructor(
        PUBLIC.build(), // <- fix this
        ast.getName(),
        ast.getFormalParameters()
            .getFormalParameterListing()
            .getFormalParameterList().stream()
            .map(p -> CDParameterFacade.getInstance()
                .createParameter(
                    p.getMCType(), p.getDeclarator().getName()))
            .toArray(ASTCDParameter[]::new));

    StringBuilder methodBody = new StringBuilder();
    ast.getMCJavaBlock().getMCBlockStatementList().stream()
        .map(s -> CD4CodeMill.prettyPrint(s, true))
        .forEach(s -> methodBody.append(s).append("\n"));

    glex.replaceTemplate(EMPTY_BODY, method, new StringHookPoint(methodBody.toString()));
    currentType.addCDMember(method);
  }

  public ASTCDCompilationUnit getCompilationUnit() {
    return cdCompilationUnit;
  }
}
