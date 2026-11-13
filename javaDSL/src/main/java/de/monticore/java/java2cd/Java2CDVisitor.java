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
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._prettyprint.JavaDSLFullPrettyPrinter;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.javalight._ast.ASTFormalParameterListing;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.statements.mccommonstatements._ast.*;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.types.MCTypeFacade;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mccollectiontypes._ast.ASTMCGenericType;
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
  
  private static final int M_PUBLIC    = 1;
  private static final int M_PROTECTED = 1 << 1;
  private static final int M_PRIVATE   = 1 << 2;
  private static final int M_ABSTRACT  = 1 << 3;
  private static final int M_FINAL     = 1 << 4;
  private static final int M_STATIC    = 1 << 5;

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
      List<String> interfaces = new ArrayList<>();
      for (ASTMCType i : ast.getImplementedInterfaceList()) {
        if (i instanceof ASTMCQualifiedType) {
          interfaces.add(((ASTMCQualifiedType) i).getMCQualifiedName().getQName());
        } else {
          interfaces.add(printJavaDSLASTMCType(i));
        }
      }
    }

    if (ast.isPresentSuperClass()) {
      classBuilder = classBuilder
          .setCDExtendUsage(CDExtendUsageFacade.getInstance()
              .createCDExtendUsage(printJavaDSLASTMCType(getMCType(ast.getSuperClass()))));
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
                  .map(this::printJavaDSLASTMCType)
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
                    .map(this::printJavaDSLASTMCType)
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
                    .map(this::printJavaDSLASTMCType)
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

      if (variable.isPresentVariableInit()) {
        String initial = new JavaDSLFullPrettyPrinter(new IndentPrinter()).prettyprint(variable.getVariableInit());
        glex.replaceTemplate(VALUE, attribute, new StringHookPoint(" = " + initial));
      }
      currentType.addCDMember(attribute);
    }
  }

  protected ASTMCType getMCType(ASTMCType mcType) {
    ASTMCType type;
    if (mcType instanceof ASTMCQualifiedType) {
      type = MCTypeFacade.getInstance()
          .createQualifiedType(
              ((ASTMCQualifiedType) mcType).getMCQualifiedName().getQName());
    } else if (mcType instanceof ASTMCArrayType arrayType) {
      type = MCTypeFacade.getInstance()
          .createArrayType(
              getMCType(arrayType.getMCType()), arrayType.getAnnotatedDimensionList().size());
    } else if (mcType instanceof ASTMCGenericType genericType) {

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
        getModifier(ast.getMCModifierList().stream().filter(m -> m instanceof ASTJavaModifier).map(m -> (ASTJavaModifier) m).collect(Collectors.toList())),
        ast.getName());

    if (ast.getMCReturnType().isPresentMCType()) {
      method.setMCReturnType(
          CD4CodeMill.mCReturnTypeBuilder()
              .setMCType(getMCType(ast.getMCReturnType().getMCType()))
              .build());
    }

    if (ast.getFormalParameters().isPresentFormalParameterListing()) {
      addParameters(ast.getFormalParameters().getFormalParameterListing(), method);
    }

    JavaDSLFullPrettyPrinter printer = new JavaDSLFullPrettyPrinter(new IndentPrinter());
    StringBuilder methodBody = new StringBuilder();
    ast.getMCJavaBlock().getMCBlockStatementList().stream()
        .map(printer::prettyprint)
        .forEach(methodBody::append);

    glex.replaceTemplate(EMPTY_BODY, method, new StringHookPoint(methodBody.toString()));
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
    StringBuilder methodBody = new StringBuilder();
    ast.getMCJavaBlock().getMCBlockStatementList().stream()
        .map(printer::prettyprint)
        .forEach(methodBody::append);

    glex.replaceTemplate(EMPTY_BODY, method, new StringHookPoint(methodBody.toString()));
    currentType.addCDMember(method);
  }

  protected ASTModifier getModifier(List<ASTJavaModifier> modifiers) {
    int mask = 0;
    for (ASTJavaModifier modifier : modifiers) {
      int bit = switch (modifier) {
        case ASTModifierPublic ignored -> M_PUBLIC;
        case ASTModifierProtected ignored -> M_PROTECTED;
        case ASTModifierPrivate ignored -> M_PRIVATE;
        case ASTModifierAbstract ignored -> M_ABSTRACT;
        case ASTModifierFinal ignored -> M_FINAL;
        case ASTModifierStatic ignored -> M_STATIC;
        default -> -1;
      };
      
      if (bit == -1 || (mask & bit) != 0) {
        continue;
      }
      
      mask |= bit;
    }
    
    return switch (mask) {
      case M_PUBLIC -> PUBLIC.build();
      case M_PROTECTED -> PROTECTED.build();
      case M_PRIVATE -> PRIVATE.build();
      case M_ABSTRACT -> PACKAGE_PRIVATE_ABSTRACT.build();
      case M_STATIC -> PACKAGE_PRIVATE_STATIC.build();
      case M_FINAL -> PACKAGE_PRIVATE_FINAL.build();
      case M_PUBLIC | M_ABSTRACT -> PUBLIC_ABSTRACT.build();
      case M_PUBLIC | M_FINAL -> PUBLIC_FINAL.build();
      case M_PUBLIC | M_STATIC -> PUBLIC_STATIC.build();
      
      case M_PROTECTED | M_ABSTRACT -> PROTECTED_ABSTRACT.build();
      case M_PROTECTED | M_FINAL -> PROTECTED_FINAL.build();
      case M_PROTECTED | M_STATIC -> PROTECTED_STATIC.build();
      
      case M_PRIVATE | M_FINAL -> PRIVATE_FINAL.build();
      case M_PRIVATE | M_STATIC -> PRIVATE_STATIC.build();
      
      case M_STATIC | M_FINAL -> PACKAGE_PRIVATE_STATIC_FINAL.build();
      
      case M_PUBLIC | M_STATIC | M_FINAL -> PUBLIC_STATIC_FINAL.build();
      
      case M_PROTECTED | M_STATIC | M_FINAL -> PROTECTED_STATIC_FINAL.build();
      
      case M_PRIVATE | M_STATIC | M_FINAL -> PRIVATE_STATIC_FINAL.build();
      default -> PACKAGE_PRIVATE.build();
    };
  }

  public ASTCDCompilationUnit getCompilationUnit() {
    return cdCompilationUnit;
  }
  
  public String printJavaDSLASTMCType(ASTMCType type) {
    JavaDSLMill.init();
    String printedType = type.printType();
    CD4CodeMill.init();
    return printedType;
  }
}
