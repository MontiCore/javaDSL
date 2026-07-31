package de.monticore.java.javadsl._symboltable;

import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTAnnotation;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.statements.mcarraystatements._ast.ASTArrayDeclaratorId;
import de.monticore.statements.mccommonstatements._ast.*;
import de.monticore.statements.mcstatementsbasis._ast.ASTMCModifier;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfObject;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaDSLScopesGenitorP2 implements JavaDSLVisitor2, JavaLightVisitor2 {
  
  @Override
  public void endVisit(ASTEnumDeclaration node) {
    TypeDeclarationSymbol symbol = node.getSymbol();
    
    List<SymTypeExpression> supertypes =
        node.getImplementedInterfaceList().stream().map(TypeCheck3::symTypeFromAST)
            .collect(Collectors.toList());
    symbol.setSuperTypesList(supertypes);
  }
  
  @Override
  public void endVisit(ASTRecordDeclaration node) {
    TypeDeclarationSymbol symbol = node.getSymbol();
    
    List<SymTypeExpression> supertypes =
        node.getImplementedInterfaceList().stream().map(TypeCheck3::symTypeFromAST)
            .collect(Collectors.toList());
    symbol.setSuperTypesList(supertypes);
  }
  
  @Override
  public void endVisit(ASTEnumConstantDeclaration node) {
    EnumConstantDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsStatic(true);
    symbol.setIsFinal(true);
    symbol.setIsReadOnly(true);
    symbol.setIsPublic(true);
    symbol.setIsEnumConstant(true);
    
    final String enumName = node.getEnclosingScope().getName();
    final Optional<TypeDeclarationSymbol> enumDeclaration =
        node.getEnclosingScope().getEnclosingScope().resolveTypeDeclarationLocally(enumName);
    if (enumDeclaration.isPresent()) {
      final SymTypeOfObject typeObject =
          SymTypeExpressionFactory.createTypeObject(enumDeclaration.get());
      symbol.setType(typeObject);
    }
    else {
      Log.error("0x0x7A003: Could not resolve enclosing enum declaration",
          node.get_SourcePositionStart());
    }
  }
  
  // Temporary fix for missing scope property inheritance
  // TODO: Remove when monticore#4729 is fixed
  @Override
  public void visit(ASTMethodDeclaration node) {
    node.getSpannedScope().setShadowing(true);
    node.getSpannedScope().setExportingSymbols(false);
    node.getSpannedScope().setOrdered(true);
  }
  
  @Override
  public void endVisit(ASTConstructorDeclaration node) {
    JavaMethodSymbol symbol = node.getSymbol();
    IJavaDSLScope enclosingScope =
        JavaDSLMill.typeDispatcher().asJavaDSLIJavaDSLScope(node.getEnclosingScope());
    ASTNode enclosingScopeNode = enclosingScope.getAstNode();
    if (JavaDSLMill.typeDispatcher().isJavaDSLASTClassDeclaration(enclosingScopeNode)) {
      ASTClassDeclaration enclosingClass =
          JavaDSLMill.typeDispatcher().asJavaDSLASTClassDeclaration(enclosingScopeNode);
      symbol.setType(SymTypeExpressionFactory.createFromSymbol(enclosingClass.getSymbol()));
    }
    else if (JavaDSLMill.typeDispatcher().isJavaDSLASTEnumDeclaration(enclosingScopeNode)) {
      ASTEnumDeclaration enclosingEnum =
          JavaDSLMill.typeDispatcher().asJavaDSLASTEnumDeclaration(enclosingScopeNode);
      symbol.setType(SymTypeExpressionFactory.createFromSymbol(enclosingEnum.getSymbol()));
    }
    else {
      Log.error(
          "0x7A004: Could not set ASTConstructorDeclaration type as it is not a direct child of a ASTClassDeclaration");
    }
  }
  
  @Override
  public void endVisit(ASTClassDeclaration node) {
    TypeDeclarationSymbol symbol = node.getSymbol();
    
    List<SymTypeExpression> supertypes = new ArrayList<>();
    if (node.isPresentSuperClass()) {
      supertypes.add(TypeCheck3.symTypeFromAST(node.getSuperClass()));
    }
    supertypes.addAll(node.getImplementedInterfaceList().stream().map(TypeCheck3::symTypeFromAST)
        .toList());
    symbol.setSuperTypesList(supertypes);
  }
  
  @Override
  public void endVisit(ASTFieldDeclaration node) {
    for (ASTVariableDeclarator v : node.getVariableDeclaratorList()) {
      SymTypeExpression declaratorType = TypeCheck3.symTypeFromAST(node.getMCType());
      if (JavaDSLMill.typeDispatcher().isMCArrayStatementsASTArrayDeclaratorId(v.getDeclarator())) {
        ASTArrayDeclaratorId arrayDeclaratorId =
            JavaDSLMill.typeDispatcher().asMCArrayStatementsASTArrayDeclaratorId(v.getDeclarator());
        declaratorType = SymTypeRelations.normalize(
            SymTypeExpressionFactory.createTypeArray(declaratorType, arrayDeclaratorId.sizeDim()));
      }
      v.getDeclarator().getSymbol().setType(declaratorType);
      addModifiersToField(v.getDeclarator().getSymbol(), node.getJavaModifierList());
    }
  }
  
  @Override
  public void endVisit(ASTAnnotationMethod node) {
    // adapted from endVisit(ASTMethodDeclaration) in JavaLightSTCompleteTypes
    JavaMethodSymbol symbol = node.getSymbol();
    addModifiersToMethOrConstr(symbol, node.getJavaModifierList());
    symbol.setType(TypeCheck3.symTypeFromAST(node.getMCType()));
  }
  
  @Override
  public void endVisit(ASTTryLocalVariableDeclaration node) {
    if (!node.isVar()) {
      FieldSymbol symbol = node.getDeclaratorId().getSymbol();
      symbol.setType(TypeCheck3.symTypeFromAST(node.getMCType()));
    }
  }
  
  protected void addModifiersToField(FieldSymbol fieldSymbol,
      Iterable<? extends ASTMCModifier> astModifierList) {
    for (ASTMCModifier modifier : astModifierList) {
      switch (modifier) {
        case ASTAnnotation annotation -> {
          //fieldSymbol.addAnnotations(TypeCheck3.symTypeFromAST(annotation.getAnnotationName()));
          // TODO: FieldSymbols do not support annotations yet -> annotations are lost
        }
        case ASTJavaModifier javaModifier -> {
          switch (javaModifier) {
            case ASTModifierPublic m -> fieldSymbol.setIsPublic(true);
            case ASTModifierProtected m -> fieldSymbol.setIsProtected(true);
            case ASTModifierPrivate m -> fieldSymbol.setIsPrivate(true);
            case ASTModifierStatic m -> fieldSymbol.setIsStatic(true);
            case ASTModifierFinal m -> fieldSymbol.setIsFinal(true);
            default -> {
            } // ignore
          }
        }
        default -> {
        } // ignore
      }
    }
  }
  
  protected void addModifiersToMethOrConstr(JavaMethodSymbol javaMethodSymbol,
      Iterable<? extends ASTMCModifier> astModifierList) {
    for (ASTMCModifier modifier : astModifierList) {
      switch (modifier) {
        case ASTAnnotation annotation -> javaMethodSymbol.addAnnotations(
            TypeCheck3.symTypeFromAST(annotation.getAnnotationName()));
        case ASTJavaModifier javaModifier -> {
          switch (javaModifier) {
            case ASTModifierPublic m -> javaMethodSymbol.setIsPublic(true);
            case ASTModifierProtected m -> javaMethodSymbol.setIsProtected(true);
            case ASTModifierPrivate m -> javaMethodSymbol.setIsPrivate(true);
            case ASTModifierAbstract m -> javaMethodSymbol.setIsAbstract(true);
            case ASTModifierStatic m -> javaMethodSymbol.setIsStatic(true);
            case ASTModifierFinal m -> javaMethodSymbol.setIsFinal(true);
            case ASTModifierNative m -> javaMethodSymbol.setIsNative(true);
            case ASTModifierStrictFp m -> javaMethodSymbol.setIsStrictfp(true);
            case ASTModifierSynchronized m -> javaMethodSymbol.setIsSynchronized(true);
            case ASTModifierDefault m -> javaMethodSymbol.setIsDefault(true);
            default -> {
            } // ignore
          }
        }
        default -> {
        } // ignore
      }
    }
  }
}
