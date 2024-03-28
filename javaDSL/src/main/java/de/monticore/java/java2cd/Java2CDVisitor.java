/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.java2cd;

import de.monticore.cd.facade.*;
import de.monticore.cd.methodtemplates.CD4C;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4codebasis._ast.ASTCDConstructor;
import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDMethodSignature;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.*;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.java.JavaDSLTool;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._prettyprint.JavaDSLFullPrettyPrinter;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.javalight._ast.ASTFormalParameterListing;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mccommonstatements._ast.ASTJavaModifier;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.types.MCTypeFacade;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mccollectiontypes._ast.ASTMCGenericType;
import de.monticore.types.mccollectiontypes._ast.ASTMCListType;
import de.monticore.types.mccollectiontypes._ast.ASTMCTypeArgument;
import de.monticore.umlmodifier._ast.ASTModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.monticore.cd.codegen.CD2JavaTemplates.EMPTY_BODY;
import static de.monticore.cd.codegen.CD2JavaTemplates.VALUE;
import static de.monticore.cd.facade.CDModifier.*;

public class Java2CDVisitor implements JavaDSLVisitor2, JavaLightVisitor2 {

  protected ASTCDCompilationUnit cdCompilationUnit;
  protected ASTCDPackage cdPackage;
  protected ASTCDType currentType;

  protected List<ASTMCImportStatement> imports;

  protected final GlobalExtensionManagement glex;

  protected final CD4C cd4C;

  public Java2CDVisitor(GlobalExtensionManagement glex) {
    this.glex = glex;
    this.cd4C = CD4C.getInstance();
    imports = new ArrayList<>();
  }

  @Override
  public void visit(ASTOrdinaryCompilationUnit ast) {
    ASTCDDefinition definition = CD4CodeMill.cDDefinitionBuilder()
        .setModifier(PUBLIC.build())
        .setName("Generated")
        .build();

    cdPackage = CD4CodeMill.cDPackageBuilder()
        .setMCQualifiedName(ast.isPresentPackageDeclaration()
            ? ast.getPackageDeclaration().getMCQualifiedName()
            : MCQualifiedNameFacade.createQualifiedName("generated"))
        .build();

    definition.addCDElement(cdPackage);

    this.cdCompilationUnit = CD4CodeMill.cDCompilationUnitBuilder()
        .setCDDefinition(definition)
        .build();
  }

  @Override
  public void visit(ASTImportDeclaration ast) {
    imports.add(CD4CodeMill.mCImportStatementBuilder()
        .setMCQualifiedName(ast.getMCQualifiedName())
        .build());
  }

  @Override
  public void visit(ASTClassDeclaration ast) {
    ASTCDClassBuilder classBuilder = CD4CodeMill.cDClassBuilder()
        .setModifier(getModifier(ast.getJavaModifierList()))
        .setName(ast.getName());

    if (!ast.isEmptyImplementedInterface()) {
      classBuilder.setCDInterfaceUsage(CDInterfaceUsageFacade.getInstance()
          .createCDInterfaceUsage(
              ast.getImplementedInterfaceList()
                  .stream()
                  .map(ASTMCType::printType)
                  .toArray(String[]::new)));
    }

    if (ast.isPresentSuperClass()) {
      classBuilder = classBuilder
          .setCDExtendUsage(CDExtendUsageFacade.getInstance()
              .createCDExtendUsage(ast.getSuperClass().printType()));
    }

    ASTCDClass cdClass = classBuilder.build();
    currentType = cdClass;
    cdPackage.addCDElement(cdClass);

    imports.forEach(i -> cd4C.addImport(currentType, i.getQName()));
  }

  @Override
  public void visit(ASTRecordDeclaration ast) {
    ASTCDClassBuilder classBuilder = CD4CodeMill.cDClassBuilder()
        .setModifier(getModifier(ast.getJavaModifierList()))
        .setName(ast.getName());

    if (!ast.isEmptyImplementedInterface()) {
      classBuilder.setCDInterfaceUsage(CDInterfaceUsageFacade.getInstance()
          .createCDInterfaceUsage(
              ast.getImplementedInterfaceList()
                  .stream()
                  .map(ASTMCType::printType)
                  .toArray(String[]::new)));
    }

    ASTCDClass cdClass = classBuilder.build();
    currentType = cdClass;
    cdPackage.addCDElement(cdClass);
  }

  @Override
  public void visit(ASTRecordComponent ast) {
    currentType.addCDMember(CDAttributeFacade.getInstance().createAttribute(
        getModifier(ast.getJavaModifierList()),
        ast.getMCType(),
        ast.getName()));
  }

  @Override
  public void visit(ASTCompactConstructorDeclaration ast) {
    ASTCDConstructor constructor = CDConstructorFacade.getInstance()
        .createConstructor(getModifier(ast.getJavaModifierList()), ast.getName());

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
    ASTCDInterface cdInterface = CD4CodeMill.cDInterfaceBuilder()
        .setModifier(getModifier(ast.getJavaModifierList()))
        .setName(ast.getName())
        .setCDExtendUsage(CDExtendUsageFacade.getInstance()
            .createCDExtendUsage(
                ast.getExtendedInterfaceList()
                    .stream()
                    .map(ASTMCType::printType)
                    .toArray(String[]::new)))
        .build();

    currentType = cdInterface;
    cdPackage.addCDElement(cdInterface);
  }

  @Override
  public void visit(ASTEnumDeclaration ast) {
    ASTCDEnum cdEnum = CD4CodeMill.cDEnumBuilder()
        .setModifier(getModifier(ast.getJavaModifierList()))
        .setName(ast.getName())
        .setCDInterfaceUsage(CDInterfaceUsageFacade.getInstance()
            .createCDInterfaceUsage(
                ast.getImplementedInterfaceList()
                    .stream()
                    .map(ASTMCType::printType)
                    .toArray(String[]::new)))
        .build();

    currentType = cdEnum;
    cdPackage.addCDElement(cdEnum);
  }

  @Override
  public void visit(ASTEnumConstantDeclaration ast) {
    ((ASTCDEnum) currentType).addCDEnumConstant(
        CD4CodeMill.cDEnumConstantBuilder()
            .setName(ast.getName())
            .build()
    );
  }

  @Override
  public void visit(ASTFieldDeclaration ast) {
    ASTMCType type = getMCType(ast.getMCType());

    for (ASTVariableDeclarator variable : ast.getVariableDeclaratorList()) {
      ASTCDAttribute attribute = CDAttributeFacade.getInstance()
          .createAttribute(
              getModifier(ast.getJavaModifierList()),
              type,
              variable.getDeclarator().getName());

      String initial = new JavaDSLFullPrettyPrinter(new IndentPrinter()).prettyprint(variable.getVariableInit());
      glex.replaceTemplate(VALUE, attribute, new StringHookPoint(" = " + initial));
      currentType.addCDMember(attribute);
    }
  }

  protected ASTMCType getMCType(ASTMCType mcType) {
    ASTMCType type;
    if (mcType instanceof ASTMCQualifiedType) {
      type = MCTypeFacade.getInstance()
          .createQualifiedType(
              ((ASTMCQualifiedType) mcType).getMCQualifiedName().getQName());
    } else if (mcType instanceof ASTMCArrayType) {
      ASTMCArrayType arrayType = (ASTMCArrayType) mcType;
      type = MCTypeFacade.getInstance()
          .createArrayType(
              arrayType.getMCType(), arrayType.getAnnotatedDimensionList().size());
    } else if (mcType instanceof ASTMCGenericType) {
      ASTMCGenericType genericType = (ASTMCGenericType) mcType;

      List<ASTMCTypeArgument> typeArguments = genericType.getMCTypeArgumentList().stream()
          .map(ASTMCTypeArgument::getMCTypeOpt)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(this::getMCType)
          .map(t -> CD4CodeMill.mCBasicTypeArgumentBuilder()
              .setMCQualifiedType((de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType) t)
              .build())
          .collect(Collectors.toList());

      type = MCTypeFacade.getInstance()
          .createBasicGenericTypeOf(
              genericType.getNameList(), typeArguments);
    } else {
      type = mcType.deepClone();
    }
    return type;
  }

  @Override
  public void visit(ASTMethodDeclaration ast) {
    ASTCDMethod method = CDMethodFacade.getInstance().createMethod(
        getModifier(ast.getMCModifierList().stream().map(m -> (ASTJavaModifier) m).collect(Collectors.toList())),
        ast.getMCReturnType(),
        ast.getName());

    if (ast.getFormalParameters().isPresentFormalParameterListing()) {
      addParameters(ast.getFormalParameters().getFormalParameterListing(), method);
    }

    JavaDSLFullPrettyPrinter printer = new JavaDSLFullPrettyPrinter(new IndentPrinter());
    String methodBody = printer.prettyprint(ast.getMCJavaBlock());

    glex.replaceTemplate(EMPTY_BODY, method, new StringHookPoint(methodBody));
    currentType.addCDMember(method);
  }

  protected void addParameters(ASTFormalParameterListing ast, ASTCDMethodSignature method) {
    List<ASTCDParameter> list = new ArrayList<>();
    for (ASTFormalParameter p : ast.getFormalParameterList()) {
      ASTMCType type = getMCType(p.getMCType());
      ASTCDParameter parameter = CDParameterFacade.getInstance()
          .createParameter(
              type, p.getDeclarator().getName());
      list.add(parameter);
    }

    method.addAllCDParameters(list);
  }

  @Override
  public void visit(ASTConstructorDeclaration ast) {
    ASTCDConstructor method = CDConstructorFacade.getInstance().createConstructor(
        getModifier(ast.getMCModifierList().stream().map(m -> (ASTJavaModifier) m).collect(Collectors.toList())),
        ast.getName());

    if (ast.getFormalParameters().isPresentFormalParameterListing()) {
      addParameters(ast.getFormalParameters().getFormalParameterListing(), method);
    }

    JavaDSLFullPrettyPrinter printer = new JavaDSLFullPrettyPrinter(new IndentPrinter());
    String methodBody = printer.prettyprint(ast.getMCJavaBlock());

    glex.replaceTemplate(EMPTY_BODY, method, new

        StringHookPoint(methodBody));
    currentType.addCDMember(method);
  }

  protected ASTModifier getModifier(List<ASTJavaModifier> modifiers) {
    int modifier = 0;
    if (modifiers.size() == 1) {
      modifier = modifiers.get(0).getModifier();
    } else if (modifiers.size() == 2) {
      modifier = modifiers.get(0).getModifier() * 10 + modifiers.get(1).getModifier();
    } else if (modifiers.size() == 3) {
      modifier = modifiers.get(0).getModifier() * 100 + modifiers.get(1).getModifier() * 10 + modifiers.get(2).getModifier();
    }

    switch (modifier) {
      case 0:
        return PACKAGE_PRIVATE.build();
      case 1:
        return PACKAGE_PRIVATE_ABSTRACT.build();
      case 3:
        return PACKAGE_PRIVATE_FINAL.build();
      case 6:
        return PRIVATE.build();
      case 7:
        return PROTECTED.build();
      case 9:
        return PACKAGE_PRIVATE_STATIC.build();
      case 38:
      case 83:
        return PUBLIC_FINAL.build();
      case 18:
      case 81:
        return PUBLIC_ABSTRACT.build();
      case 98:
      case 89:
        return PUBLIC_STATIC.build();
      case 37:
      case 73:
        return PROTECTED_FINAL.build();
      case 17:
      case 71:
        return PROTECTED_ABSTRACT.build();
      case 97:
      case 79:
        return PROTECTED_STATIC.build();
      case 63:
      case 36:
        return PRIVATE_FINAL.build();
      case 96:
      case 69:
        return PRIVATE_STATIC.build();
      case 93:
      case 39:
        return PACKAGE_PRIVATE_STATIC_FINAL.build();
      case 893:
      case 839:
      case 983:
      case 938:
      case 389:
      case 398:
        return PUBLIC_STATIC_FINAL.build();
      case 793:
      case 739:
      case 973:
      case 937:
      case 379:
      case 397:
        return PROTECTED_STATIC_FINAL.build();
      case 693:
      case 639:
      case 963:
      case 936:
      case 369:
      case 396:
        return PRIVATE_STATIC_FINAL.build();
      default:
        return PUBLIC.build();
    }
  }

  public ASTCDCompilationUnit getCompilationUnit() {
    return cdCompilationUnit;
  }
}
