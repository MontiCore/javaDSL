/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.java.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;

import de.monticore.java.javadsl._ast.ASTAnonymousClass;
import de.monticore.java.javadsl._ast.ASTArrayCreator;
import de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression;
import de.monticore.java.javadsl._ast.ASTArrayDimensionByInitializer;
import de.monticore.java.javadsl._ast.ASTCatchType;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTConstantExpressionSwitchLabel;
import de.monticore.java.javadsl._ast.ASTConstantsJavaDSL;
import de.monticore.java.javadsl._ast.ASTCreatedName;
import de.monticore.java.javadsl._ast.ASTDeclaratorId;
import de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration;
import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._ast.ASTExpressionStatement;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTIdentifierAndTypeArgument;
import de.monticore.java.javadsl._ast.ASTIfStatement;
import de.monticore.java.javadsl._ast.ASTInnerCreator;
import de.monticore.java.javadsl._ast.ASTInnerCreatorExpression;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTJavaBlock;
import de.monticore.java.javadsl._ast.ASTLastFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclarationStatement;
import de.monticore.java.javadsl._ast.ASTMethodBody;
import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTMethodSignature;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._ast.ASTReturnStatement;
import de.monticore.java.javadsl._ast.ASTSwitchStatement;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTVariableInitializer;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.expressions.mcexpressions._ast.ASTAddExpression;
import de.monticore.expressions.mcexpressions._ast.ASTArrayExpression;
import de.monticore.expressions.mcexpressions._ast.ASTAssignmentExpression;
import de.monticore.expressions.mcexpressions._ast.ASTBinaryAndOpExpression;
import de.monticore.expressions.mcexpressions._ast.ASTBinaryOrOpExpression;
import de.monticore.expressions.mcexpressions._ast.ASTBinaryXorOpExpression;
import de.monticore.expressions.mcexpressions._ast.ASTBooleanAndOpExpression;
import de.monticore.expressions.mcexpressions._ast.ASTBooleanNotExpression;
import de.monticore.expressions.mcexpressions._ast.ASTBooleanOrOpExpression;
import de.monticore.expressions.mcexpressions._ast.ASTBracketExpression;
import de.monticore.expressions.mcexpressions._ast.ASTCallExpression;
import de.monticore.expressions.mcexpressions._ast.ASTClassExpression;
import de.monticore.expressions.mcexpressions._ast.ASTComparisonExpression;
import de.monticore.expressions.mcexpressions._ast.ASTConditionalExpression;
import de.monticore.expressions.mcexpressions._ast.ASTExpression;
import de.monticore.expressions.mcexpressions._ast.ASTGenericInvocationExpression;
import de.monticore.expressions.mcexpressions._ast.ASTGenericInvocationSuffix;
import de.monticore.expressions.mcexpressions._ast.ASTIdentityExpression;
import de.monticore.expressions.mcexpressions._ast.ASTInstanceofExpression;
import de.monticore.expressions.mcexpressions._ast.ASTLiteralExpression;
import de.monticore.expressions.mcexpressions._ast.ASTLogicalNotExpression;
import de.monticore.expressions.mcexpressions._ast.ASTMultExpression;
import de.monticore.expressions.mcexpressions._ast.ASTNameExpression;
import de.monticore.expressions.mcexpressions._ast.ASTPrefixExpression;
import de.monticore.expressions.mcexpressions._ast.ASTPrimaryGenericInvocationExpression;
import de.monticore.expressions.mcexpressions._ast.ASTPrimarySuperExpression;
import de.monticore.expressions.mcexpressions._ast.ASTPrimaryThisExpression;
import de.monticore.expressions.mcexpressions._ast.ASTQualifiedNameExpression;
import de.monticore.expressions.mcexpressions._ast.ASTShiftExpression;
import de.monticore.expressions.mcexpressions._ast.ASTSuffixExpression;
import de.monticore.expressions.mcexpressions._ast.ASTSuperExpression;
import de.monticore.expressions.mcexpressions._ast.ASTTypeCastExpression;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.literals.literals._ast.ASTBooleanLiteral;
import de.monticore.literals.literals._ast.ASTCharLiteral;
import de.monticore.literals.literals._ast.ASTDoubleLiteral;
import de.monticore.literals.literals._ast.ASTFloatLiteral;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.monticore.literals.literals._ast.ASTLongLiteral;
import de.monticore.literals.literals._ast.ASTNullLiteral;
import de.monticore.literals.literals._ast.ASTSignedDoubleLiteral;
import de.monticore.literals.literals._ast.ASTSignedFloatLiteral;
import de.monticore.literals.literals._ast.ASTSignedIntLiteral;
import de.monticore.literals.literals._ast.ASTSignedLongLiteral;
import de.monticore.literals.literals._ast.ASTStringLiteral;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.types.types._ast.ASTComplexArrayType;
import de.monticore.types.types._ast.ASTComplexReferenceType;
import de.monticore.types.types._ast.ASTPrimitiveArrayType;
import de.monticore.types.types._ast.ASTPrimitiveType;
import de.monticore.types.types._ast.ASTQualifiedName;
import de.monticore.types.types._ast.ASTSimpleReferenceType;
import de.monticore.types.types._ast.ASTTypeArgument;
import de.monticore.types.types._ast.ASTTypeVariableDeclaration;
import de.monticore.types.types._ast.ASTVoidType;
import de.monticore.types.types._ast.ASTWildcardType;

/**
 * TODO
 *
 * @author (last commit) $$Author$$
 * @since TODO
 */

public class HCJavaDSLTypeResolver extends GenericTypeResolver<JavaTypeSymbolReference>
    implements JavaDSLVisitor {
  
  protected Stack<List<JavaTypeSymbolReference>> parameterStack = new Stack<>();
  
  public JavaDSLVisitor getRealThis() {
    return this;
  }
  
  public void handle(ASTFieldDeclaration node) {
    node.getType().accept(this);
  }
  
  public void handle(ASTLocalVariableDeclaration node) {
    node.getType().accept(this);
  }
  
  public void handle(ASTComplexReferenceType type) {
    handle(type.getSimpleReferenceTypes().get(0));
  }
  
  public void handle(ASTComplexArrayType type) {
    type.getComponentType().accept(this);
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference typeSymbolReference = this.getResult().get();
      typeSymbolReference.setDimension(type.getDimensions());
      this.setResult(typeSymbolReference);
    }
  }
  
  public void handle(ASTSimpleReferenceType type) {
    JavaTypeSymbolReference typeSymbolReference = new JavaTypeSymbolReference(
        String.join(".", type.getNames()), type.getEnclosingScope().get(), 0);
    String completeName = JavaDSLHelper.getCompleteName(typeSymbolReference);
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
        completeName, type.getEnclosingScope().get(), 0);
    if (type.typeArgumentsIsPresent()) {
      List<ActualTypeArgument> actualTypeArgumentList = new ArrayList<>();
      for (ASTTypeArgument typeArgument : type.getTypeArguments().get().getTypeArguments()) {
        typeArgument.accept(this);
        ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false,
            this.getResult().get());
        actualTypeArgumentList.add(actualTypeArgument);
      }
      finalType.setActualTypeArguments(actualTypeArgumentList);
    }
    this.setResult(finalType);
  }
  
  public void handle(ASTPrimitiveArrayType type) {
    type.getComponentType().accept(this);
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference typeSymbolReference = this.getResult().get();
      typeSymbolReference.setDimension(type.getDimensions());
      this.setResult(typeSymbolReference);
    }
  }
  
  public void handle(ASTWildcardType type) {
    JavaTypeSymbolReference wildType = new JavaTypeSymbolReference("ASTWildcardType",
        type.getEnclosingScope().get(), 0);
    List<ActualTypeArgument> typeArguments = new ArrayList<>();
    if (type.lowerBoundIsPresent()) {
      type.getLowerBound().get().accept(this);
      if (this.getResult().isPresent()) {
        ActualTypeArgument typeArgument = new ActualTypeArgument(true, false,
            this.getResult().get());
        typeArguments.add(typeArgument);
      }
    }
    if (type.upperBoundIsPresent()) {
      type.getUpperBound().get().accept(this);
      if (this.getResult().isPresent()) {
        ActualTypeArgument typeArgument = new ActualTypeArgument(false, true,
            this.getResult().get());
        typeArguments.add(typeArgument);
      }
    }
    wildType.setActualTypeArguments(typeArguments);
    this.setResult(wildType);
  }
  
  public void handle(ASTPrimitiveType type) {
    this.setResult(new JavaTypeSymbolReference(type.toString(), type.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTVoidType type) {
    this.setResult(new JavaTypeSymbolReference("void", type.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTMethodDeclaration node) {
    handle(node.getMethodSignature());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference methodType = this.getResult().get();
      this.setResult(methodType);
    }
  }
  
  public void handle(ASTMethodSignature node) {
    node.getReturnType().accept(this);
  }
  
  public void handle(ASTFormalParameter node) {
    node.getType().accept(this);
  }
  
  public void handle(ASTVariableDeclarator node) {
    if (node.getVariableInititializerOrExpression().isPresent()) {
      handle(node.getVariableInititializerOrExpression().get());
    }
  }
  
  public void handle(ASTVariableInitializer node) {
    node.accept(this);
  }
  
  public void handle(ASTPrefixExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (!JavaDSLHelper.isNumericType(type)) {
        this.setResult(null);
      }
    }
  }
  
  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTSuperExpression)
   */
  @Override
  public void handle(ASTSuperExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      Scope scope = node.getEnclosingScope().get();
      JavaTypeSymbolReference refType = this.getResult().get();
      JavaTypeSymbol type = node.getEnclosingScope().get()
          .<JavaTypeSymbol> resolve(refType.getName(), JavaTypeSymbol.KIND).get();
      Collection<JavaTypeSymbolReference> superTypes = new HashSet(type.getSuperTypes());
      if (type.getSuperClass().isPresent()) {
        superTypes.add(type.getSuperClass().get());
      }
      if (node.getSuperSuffix().argumentsIsPresent()) {
        List<JavaTypeSymbolReference> paramTypes = new ArrayList<>();
        for (ASTExpression paramExpression : node.getSuperSuffix().getArguments().get()
            .getExpressions()) {
          this.handle(paramExpression);
          if (this.getResult().isPresent()) {
            paramTypes.add(this.getResult().get());
          }
        }
        if (node.getSuperSuffix().getName().isPresent()) {
          
          for (JavaTypeSymbolReference superType : superTypes) {
            JavaTypeSymbol currentSymbol = scope.<JavaTypeSymbol>resolve(superType.getName(), JavaTypeSymbol.KIND).get();
            HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methods = JavaDSLHelper
                .resolveManyInSuperType(node.getSuperSuffix().getName().get(), false,
                    superType, currentSymbol, new ArrayList<JavaTypeSymbolReference>(),
                    paramTypes);
            if (methods.size()== 1) {
              Entry<JavaMethodSymbol, JavaTypeSymbolReference> m = methods.entrySet().iterator().next();
              this.setResult(m.getKey().getReturnType());
              return;
            }
          }
        }
      }
    }
  }
  
  public void handle(ASTArrayExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference arrayType = this.getResult().get();
      if (arrayType.getDimension() > 0) {
        if (JavaDSLHelper.isReifiableType(arrayType)) {
          handle(node.getIndexExpression());
          if (this.getResult().isPresent()) {
            if (JavaDSLHelper
                .isIntegralType(
                    JavaDSLHelper.getUnaryNumericPromotionType(this.getResult().get()))) {
              arrayType.setDimension(arrayType.getDimension() - 1);
              this.setResult(arrayType);
            }
            else {
              this.setResult(null);
            }
          }
        }
        else {
          this.setResult(null);
        }
      }
    }
  }
  
  public void handle(ASTAddExpression node) {
    JavaTypeSymbolReference leftType;
    JavaTypeSymbolReference rightType;
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper
                .resolveTypeForExpressions(leftType, rightType, node.getAdditiveOp().get())
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTMultExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "multiplicativeOp")
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTShiftExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper
                .resolveTypeForExpressions(leftType, rightType, node.getShiftOp().get())
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTComparisonExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "comparison")
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTIdentityExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();        
        this.setResult(
            JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "identityTest")
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTBinaryAndOpExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "bitwiseOp")
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTBinaryOrOpExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "bitwiseOp")
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTBinaryXorOpExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "bitwiseOp")
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTBooleanAndOpExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "booleanOp")
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTBooleanOrOpExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "booleanOp")
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTSuffixExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (!JavaDSLHelper.isNumericType(type)) {
        this.setResult(null);
      }
    }
  }
  
  public void handle(ASTAssignmentExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        if (JavaDSLHelper.assignmentConversionAvailable(rightType, leftType)) {
          this.setResult(leftType);
        }
        else {
          this.setResult(null);
        }
      }
    }
  }
  
  public void handle(ASTGenericInvocationExpression node) {
    List<JavaTypeSymbolReference> actualArguments = new ArrayList<>();
    List<JavaTypeSymbolReference> typeArguments = new ArrayList<>();
    String methodName = "";
    ASTPrimaryGenericInvocationExpression genericInvocation = node.getPrimaryGenericInvocationExpression();
    for (ASTTypeArgument typeArgument : genericInvocation.getTypeArguments()
        .getTypeArguments()) {
      typeArgument.accept(this);
      if (this.getResult().isPresent()) {
        typeArguments.add(this.getResult().get());
      }
      else {
        this.setResult(null);
      }
    }
    if (genericInvocation.getTypeArguments().getTypeArguments().size() == typeArguments.size()) {
      for (ASTExpression expression : genericInvocation.getGenericInvocationSuffix()
          .getArguments().get().getExpressions()) {
        expression.accept(this);
        if (this.getResult().isPresent()) {
          actualArguments.add(this.getResult().get());
        }
        else {
          this.setResult(null);
        }
      }
      if (genericInvocation.getGenericInvocationSuffix().getArguments().get()
          .getExpressions().size() == actualArguments.size()) {
        if (genericInvocation.getGenericInvocationSuffix().getName().isPresent()) {
          methodName = genericInvocation.getGenericInvocationSuffix().getName().get();
        }
        
        this.handle(node.getExpression());
        if (this.getResult().isPresent()) {
          JavaTypeSymbolReference expType = this.getResult().get();
          // String completeName = JavaDSLHelper.getCompleteName(expType);
          if (node.getEnclosingScope().get().resolve(expType.getName(), JavaTypeSymbol.KIND)
              .isPresent()) {
            JavaTypeSymbol expSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                .resolve(expType.getName(), JavaTypeSymbol.KIND).get();
            HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                .resolveManyInSuperType(methodName, false, expType, expSymbol,
                    typeArguments, actualArguments);
            if (methodSymbols.size() == 1) {
              this.setResult(methodSymbols.values().iterator().next());
            }
            else {
              this.setResult(null);
            }
          }
          else {
            this.setResult(null);
          }
        }
      }
    }
  }
  
  public void handle(ASTCallExpression node) {
    List<JavaTypeSymbolReference> paramTypes = new ArrayList<>();
    for (ASTExpression paramExpression : node.getArguments().getExpressions()) {
      this.handle(paramExpression);
      if (this.getResult().isPresent()) {
        paramTypes.add(this.getResult().get());
      }
    }
    if (paramTypes.size() == node.getArguments().getExpressions().size()) {
      parameterStack.push(paramTypes);
      ASTExpression expr = node.getExpression();
      expr.accept(getRealThis());
      parameterStack.pop();
    }
  }
  
  public void handle(ASTQualifiedNameExpression node) {
    this.result = null;
    handle(node.getExpression());
    if (!this.getResult().isPresent()) {
      return;
    }
    JavaTypeSymbolReference expType = this.getResult().get();
    if ("class".equals(node.getName())) {
      JavaTypeSymbolReference classType = new JavaTypeSymbolReference("java.lang.Class",
          node.getEnclosingScope().get(), 0);
      List<ActualTypeArgument> arg = new ArrayList<>();
      ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false, expType);
      arg.add(actualTypeArgument);
      classType.setActualTypeArguments(arg);
      this.setResult(classType);
    }
    else if (this.getResult().get().getDimension() > 0
        && "length".equals(node.getName())) {
      this.setResult(new JavaTypeSymbolReference("int", expType.getEnclosingScope(), 0));
    }
    else if (JavaDSLHelper
        .visibleTypeSymbolFound(node.getEnclosingScope().get(),
            this.getResult().get().getName())
        .isPresent()) {
      JavaTypeSymbolReference type = JavaDSLHelper
          .visibleTypeSymbolFound(node.getEnclosingScope().get(),
              this.getResult().get().getName())
          .get();
      String completeName = JavaDSLHelper.getCompleteName(type);
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
          .resolve(completeName, JavaTypeSymbol.KIND).get();
      if ("super".equals(node.getName())) {
        if (typeSymbol.getSuperClass().isPresent()) {
          JavaTypeSymbolReference classType = typeSymbol.getSuperClass().get();
          JavaTypeSymbolReference superType = new JavaTypeSymbolReference(classType.getName(),
              node.getEnclosingScope().get(), classType.getDimension());
          superType.setActualTypeArguments(classType.getActualTypeArguments());
          this.setResult(superType);
        }
      }
      else if (typeSymbol.isEnum()) {
        if (typeSymbol.getSpannedScope()
            .resolveMany(node.getName(), JavaTypeSymbol.KIND)
            .size() == 1) {
          this.setResult(
              new JavaTypeSymbolReference(typeSymbol.getName(),
                  typeSymbol.getEnclosingScope(),
                  0));
        }
      }
      else {
        Collection<JavaFieldSymbol> resolvedFields = typeSymbol.getSpannedScope()
            .<JavaFieldSymbol> resolveMany(node.getName(), JavaFieldSymbol.KIND);
        if (resolvedFields.size() == 1) {
          JavaTypeSymbolReference fieldType = resolvedFields.iterator().next()
              .getType();
          String completeTypeName = JavaDSLHelper.getCompleteName(fieldType);
          JavaTypeSymbolReference newType = new JavaTypeSymbolReference(completeTypeName,
              fieldType.getEnclosingScope(), fieldType.getDimension());
          newType.setActualTypeArguments(fieldType.getActualTypeArguments());
          this.setResult(newType);
          return;
        }
        List<JavaTypeSymbolReference> paramTypes = new ArrayList<>();
        if (!parameterStack.isEmpty()) {
          paramTypes = parameterStack.peek();
        }
        HashMap<JavaMethodSymbol, JavaTypeSymbolReference> resolvedMethods = JavaDSLHelper
            .resolveManyInSuperType(node.getName(), false, null, typeSymbol, null,
                paramTypes);
        if (resolvedMethods.size() == 1) {
          JavaMethodSymbol methodSymbol = resolvedMethods.entrySet().iterator().next().getKey();
          HashMap<String, JavaTypeSymbolReference> substituted = JavaDSLHelper
              .getSubstitutedTypes(typeSymbol, expType);
          JavaTypeSymbolReference sReturnType = JavaDSLHelper.applyTypeSubstitution(substituted,
              methodSymbol.getReturnType());
          //// TODO: 01.11.2016 complete name?
          this.setResult(sReturnType);
          return;
        }
        this.setResult(null);    
      }
    } else {
      String name = expType.getName() + "." + node.getName();
      JavaTypeSymbolReference pName = new JavaTypeSymbolReference(name,
          node.getEnclosingScope().get(), 0);
      pName = new JavaTypeSymbolReference(JavaDSLHelper.getCompleteName(pName),
          pName.getEnclosingScope(), pName.getDimension());
      this.setResult(pName);     
    }
    
  }
  
  public void handle(ASTInnerCreatorExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference expType = this.getResult().get();
      String completeName = JavaDSLHelper.getCompleteName(expType);
      handle(node.getInnerCreator());
      JavaTypeSymbolReference innerType = this.getResult().get();
      JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
          completeName + "." + innerType.getName(), innerType.getEnclosingScope(), 0);
      this.setResult(finalType);
    }
  }
  
  public void handle(ASTInstanceofExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference expType = this.getResult().get();
      handle(node.getType());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference instanceType = this.getResult()
            .get();
        JavaDSLHelper
            .resolveTypeForExpressions(expType, instanceType, "instanceOf").<Runnable> map(
                javaTypeSymbolReference -> () -> this.setResult(javaTypeSymbolReference))
            .orElse(() -> this.setResult(null)).run();
      }
    }
  }
  
  public void handle(ASTTypeCastExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference expressionType = new JavaTypeSymbolReference(
          JavaDSLHelper.getCompleteName(this.getResult().get()),
          this.getResult().get().getEnclosingScope(), this.getResult().get().getDimension());
      node.getType().accept(this);
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference castType = this.getResult().get();
        if (!JavaDSLHelper.castTypeConversionAvailable(expressionType, castType)) {
          this.setResult(null);
        }
      }
    }
  }
  
  public void handle(ASTBooleanNotExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference expressionType = this.getResult()
          .get();
      if (!JavaDSLHelper.isIntegralType(expressionType)) {
        this.setResult(null);
      }
    }
  }
  
  public void handle(ASTLogicalNotExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference expressionType = this.getResult()
          .get();
      if (!JavaDSLHelper.isBooleanType(expressionType)) {
        this.setResult(null);
      }
    }
  }

  public void handle(ASTConditionalExpression node) {
    handle(node.getCondition());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference conditionType = this.getResult().get();
      if (!JavaDSLHelper.isBooleanType(conditionType)) {
        this.setResult(null);
      }
      else {
        handle(node.getTrueExpression());
        if (this.getResult().isPresent()) {
          JavaTypeSymbolReference trueType = this.getResult().get();
          handle(node.getFalseExpression());
          if (this.getResult().isPresent()) {
            JavaTypeSymbolReference falseType = this.getResult().get();
            this.setResult(
                JavaDSLHelper.resolveTypeForExpressions(trueType, falseType, "condition")
                    .orElse(null));
          }
        }
      }
    }
  }
  
  public void handle(ASTBracketExpression node) {
    handle(node.getExpression());
  }
  
  public void handle(ASTNameExpression node) {
    if (node.getEnclosingScope().isPresent()) {
      // Try to resolve a field
      Optional<JavaFieldSymbol> optField = JavaDSLHelper
          .resolveFieldInSuperType(node.getEnclosingScope().get(), node.getName());
      if (optField.isPresent()) {
        JavaFieldSymbol fieldSymbol = optField.get();
        if (fieldSymbol.getType() != null) {
          this.setResult(JavaDSLHelper.convertToWildCard(fieldSymbol.getType()));
          return;
        }
      }
      
      // Try to resolve a type
      Optional<JavaTypeSymbol> optTypeSymbol = node.getEnclosingScope().get().<JavaTypeSymbol>resolve(node.getName(), JavaTypeSymbol.KIND);
      if (optTypeSymbol.isPresent()) {
        this.setResult(new JavaTypeSymbolReference(optTypeSymbol.get().getFullName(), node.getEnclosingScope().get(), 0));
        return;
      }
      
      // Try to resolve a method
      List<JavaTypeSymbolReference> paramTypes = new ArrayList<>();
      if (!parameterStack.isEmpty()) {
        paramTypes = parameterStack.peek();
      }     
      String enclosingType = JavaDSLHelper.getEnclosingTypeSymbolName(node.getEnclosingScope().get());
      if (enclosingType != null) {
        JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
            .resolve(enclosingType, JavaTypeSymbol.KIND).get();
        HashMap<JavaMethodSymbol, JavaTypeSymbolReference> resolvedMethods = JavaDSLHelper
            .resolveManyInSuperType(node.getName(), false, null, typeSymbol, null,
                paramTypes);
        if (resolvedMethods.size() == 1) {
          this.setResult(resolvedMethods.values().iterator().next());
          return;
        }
      }
      this.setResult(new JavaTypeSymbolReference(node.getName(), node.getEnclosingScope().get(), 0));
      return;
    }
  }
  
  public void handle(ASTLiteralExpression node) {
        node.getLiteral().accept(this);
  }
  
  public void handle(ASTPrimaryThisExpression node) {
    String className = JavaDSLHelper.getEnclosingTypeSymbolName(node);
    JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
        .resolve(className, JavaTypeSymbol.KIND).get();
    
    if (classSymbol.isClass()) {
      this.setResult(
          new JavaTypeSymbolReference(classSymbol.getFullName(),
              node.getEnclosingScope().get(), 0));
    }
    else {
      this.setResult(null);
    }   
  }
  
  public void handle(ASTPrimarySuperExpression node) {
    String name = JavaDSLHelper.getEnclosingTypeSymbolName(node);
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
        .resolve(name, JavaTypeSymbol.KIND).get();
    if (typeSymbol.isClass()) {
      if (typeSymbol.getSuperClass().isPresent()) {
        String completeName = JavaDSLHelper.getCompleteName(typeSymbol.getSuperClass().get());
        JavaTypeSymbolReference classType = new JavaTypeSymbolReference(completeName,
            node.getEnclosingScope().get(), 0);
        this.setResult(classType);
      }
      else {
        this.setResult(
            new JavaTypeSymbolReference("java.lang.Object", node.getEnclosingScope().get(), 0));
      }
    }
  }
  
  public void handle(ASTClassExpression node) { 
    JavaTypeSymbolReference classType = new JavaTypeSymbolReference("java.lang.Class",
        node.getEnclosingScope().get(), 0);
    List<ActualTypeArgument> arg = new ArrayList<>();
    node.getReturnType().accept(getRealThis());
    if (getResult().isPresent()) {
      ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false, getResult().get());
      arg.add(actualTypeArgument);
      classType.setActualTypeArguments(arg);
      this.setResult(classType);
    }     
  }
  
  public void handle(ASTSignedIntLiteral node) {
    this.setResult(new JavaTypeSymbolReference("int", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTIntLiteral node) {
    this.setResult(new JavaTypeSymbolReference("int", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTNullLiteral node) {
    this.setResult(new JavaTypeSymbolReference("null", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTCharLiteral node) {
    this.setResult(new JavaTypeSymbolReference("char", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTStringLiteral node) {
    this.setResult(
        new JavaTypeSymbolReference("java.lang.String", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTBooleanLiteral node) {
    this.setResult(new JavaTypeSymbolReference("boolean", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTSignedDoubleLiteral node) {
    this.setResult(new JavaTypeSymbolReference("double", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTDoubleLiteral node) {
    this.setResult(new JavaTypeSymbolReference("double", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTFloatLiteral node) {
    this.setResult(new JavaTypeSymbolReference("float", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTSignedFloatLiteral node) {
    this.setResult(new JavaTypeSymbolReference("float", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTLongLiteral node) {
    this.setResult(new JavaTypeSymbolReference("long", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTSignedLongLiteral node) {
    this.setResult(new JavaTypeSymbolReference("long", node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTAnonymousClass node) {
    handle(node.getCreatedName());
  }
  
  public void handle(ASTCreatedName node) {
    String finalName = "";
    List<ActualTypeArgument> list = new ArrayList<>();
    if (node.getPrimitiveType().isPresent()) {
      handle(node.getPrimitiveType().get());
      if (this.getResult().isPresent()) {
        finalName = this.getResult().get().getName();
      }
    } else {
      for (int i = 0; i < node.getIdentifierAndTypeArguments().size(); i++) {
        handle(node.getIdentifierAndTypeArguments().get(i));
        if (this.getResult().isPresent()) {
          if ("".equals(finalName)) {
            finalName = this.getResult().get().getName();
          }
          else {
            finalName = finalName + "." + this.getResult().get().getName();
          }
          if (i == node.getIdentifierAndTypeArguments().size() - 1) {
            list = new ArrayList<>(this.getResult().get().getActualTypeArguments());
          }
        }
        else {
          break;
        }
      }
    }
    JavaTypeSymbolReference type = new JavaTypeSymbolReference(finalName,
        node.getEnclosingScope().get(), 0);
    String completeName = JavaDSLHelper.getCompleteName(type);
    JavaTypeSymbolReference completeType = new JavaTypeSymbolReference(completeName,
        node.getEnclosingScope().get(), 0);
    completeType.setActualTypeArguments(list);
    this.setResult(completeType);
  }
  
  public void handle(ASTIdentifierAndTypeArgument node) {
    JavaTypeSymbolReference tempType = new JavaTypeSymbolReference(node.getName(),
        node.getEnclosingScope().get(), 0);
    String completeName = JavaDSLHelper.getCompleteName(tempType);
    List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();
    if (node.typeArgumentsIsPresent()) {
      if (!node.getTypeArguments().get().getTypeArguments().isEmpty()) {
        for (ASTTypeArgument typeArgument : node.getTypeArguments().get().getTypeArguments()) {
          typeArgument.accept(this);
          JavaTypeSymbolReference argType = this.getResult().get();
          ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false, argType);
          actualTypeArguments.add(actualTypeArgument);
        }
        tempType.setActualTypeArguments(actualTypeArguments);
      }
    }
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(completeName,
        node.getEnclosingScope().get(), 0);
    finalType.setActualTypeArguments(actualTypeArguments);
    this.setResult(finalType);
  }
  
  public void handle(ASTMethodBody node) {
    node.accept(this);
  }
  
  public void handle(ASTJavaBlock node) {
    node.getBlockStatements().forEach(astBlockStatement -> astBlockStatement.accept(this));
  }
  
  public void handle(ASTIfStatement node) {
    handle(node.getCondition());
  }
  
  public void handle(ASTReturnStatement node) {
    if (node.expressionIsPresent()) {
      handle(node.getExpression().get());
    }
  }
  
  public void handle(ASTArrayCreator node) {
    handle(node.getCreatedName());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (node.getArrayDimensionSpecifier() instanceof ASTArrayDimensionByInitializer) {
        ASTArrayDimensionByInitializer initializer = (ASTArrayDimensionByInitializer) node
            .getArrayDimensionSpecifier();
        type.setDimension(initializer.getDim().size());
      }
      if (node.getArrayDimensionSpecifier() instanceof ASTArrayDimensionByExpression) {
        ASTArrayDimensionByExpression arrayDimensionByExpression = (ASTArrayDimensionByExpression) node
            .getArrayDimensionSpecifier();
        type.setDimension(arrayDimensionByExpression.getExpressions().size());
      }
    }
  }
  
  public void handle(ASTArrayDimensionByInitializer node) {
    handle(node.getArrayInitializer());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference arrType = this.getResult().get();
      arrType.setDimension(node.getDim().size());
      this.setResult(arrType);
    }
  }
  
  public void handle(ASTArrayDimensionByExpression node) {
    JavaTypeSymbolReference arrayType = this.getResult().get();
    List<JavaTypeSymbolReference> listOfExpression = new ArrayList<>();
    for (ASTExpression astExpression : node.getExpressions()) {
      handle(astExpression);
      if (!this.getResult().isPresent()) {
        break;
      }
      else {
        listOfExpression.add(this.getResult().get());
      }
    }
    if (listOfExpression.size() == node.getExpressions().size()) {
      arrayType.setDimension(listOfExpression.size());
      this.setResult(arrayType);
    }
    else {
      this.setResult(null);
      
    }
  }
  
  public void handle(ASTTypeVariableDeclaration node) {
    this.setResult(new JavaTypeSymbolReference(
        node.getName(), node.getEnclosingScope().get(),
        0));
  }
  
  public void handle(ASTClassDeclaration node) {
    String completeName = JavaDSLHelper.getCompleteName(new JavaTypeSymbolReference(node.getName(),
        node.getEnclosingScope().get(), 0));
    JavaTypeSymbolReference classType = new JavaTypeSymbolReference(completeName,
        node.getEnclosingScope().get(), 0);
    List<ActualTypeArgument> classArgumentList = new ArrayList<>();
    if (node.getTypeParameters().isPresent()) {
      for (ASTTypeVariableDeclaration typeVariableDeclaration : node.getTypeParameters().get()
          .getTypeVariableDeclarations()) {
        List<ActualTypeArgument> argList = new ArrayList<>();
        handle(typeVariableDeclaration);
        JavaTypeSymbolReference varType = this.getResult().get();
        for (ASTComplexReferenceType type : typeVariableDeclaration.getUpperBounds()) {
          handle(type);
          ActualTypeArgument typeArgument = new ActualTypeArgument(false, true,
              this.getResult().get());
          argList.add(typeArgument);
          varType.setActualTypeArguments(argList);
        }
        classArgumentList.add(new ActualTypeArgument(false, false, varType));
        classType.setActualTypeArguments(classArgumentList);
      }
    }
    this.setResult(classType);
  }
  
  public void handle(ASTInterfaceDeclaration node) {
    String completeName = JavaDSLHelper.getCompleteName(new JavaTypeSymbolReference(node.getName(),
        node.getEnclosingScope().get(), 0));
    JavaTypeSymbolReference interfaceType = new JavaTypeSymbolReference(completeName,
        node.getEnclosingScope().get(), 0);
    List<ActualTypeArgument> classArgumentList = new ArrayList<>();
    if (node.getTypeParameters().isPresent()) {
      for (ASTTypeVariableDeclaration typeVariableDeclaration : node.getTypeParameters().get()
          .getTypeVariableDeclarations()) {
        List<ActualTypeArgument> actualTypeArgumentList = new ArrayList<>();
        JavaTypeSymbolReference argTypeSymbolReference = new JavaTypeSymbolReference(
            typeVariableDeclaration.getName(), typeVariableDeclaration.getEnclosingScope().get(),
            interfaceType.getDimension());
        for (ASTComplexReferenceType type : typeVariableDeclaration.getUpperBounds()) {
          handle(type);
          ActualTypeArgument typeArgument = new ActualTypeArgument(false, true,
              this.getResult().get());
          actualTypeArgumentList.add(typeArgument);
          argTypeSymbolReference.setActualTypeArguments(actualTypeArgumentList);
        }
        classArgumentList.add(new ActualTypeArgument(false, false, argTypeSymbolReference));
        interfaceType.setActualTypeArguments(classArgumentList);
        
      }
    }
    this.setResult(interfaceType);
  }
  
  public void handle(ASTSwitchStatement node) {
    handle(node.getExpression());
  }
  
  public void handle(ASTConstantExpressionSwitchLabel node) {
    node.getConstantExpression().accept(this);
  }
  
  public void handle(ASTModifier node) {
    node.accept(this);
  }
  
  public void handle(ASTPrimitiveModifier node) {
    if (node.getModifier() == ASTConstantsJavaDSL.PRIVATE) {
      this.setResult(new JavaTypeSymbolReference("private", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.PUBLIC) {
      this.setResult(new JavaTypeSymbolReference("public", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.PROTECTED) {
      this.setResult(new JavaTypeSymbolReference("protected", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.STATIC) {
      this.setResult(new JavaTypeSymbolReference("static", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.TRANSIENT) {
      this.setResult(new JavaTypeSymbolReference("transient", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.FINAL) {
      this.setResult(new JavaTypeSymbolReference("final", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.ABSTRACT) {
      this.setResult(new JavaTypeSymbolReference("abstract", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.NATIVE) {
      this.setResult(new JavaTypeSymbolReference("native", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.SYNCHRONIZED) {
      this.setResult(
          new JavaTypeSymbolReference("synchronized", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.VOLATILE) {
      this.setResult(new JavaTypeSymbolReference("volatile", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.STRICTFP) {
      this.setResult(new JavaTypeSymbolReference("strictfp", node.getEnclosingScope().get(), 0));
    }
  }
  
  public void handle(ASTEnumDeclaration node) {
    this.setResult(new JavaTypeSymbolReference(node.getName(), node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTEnumConstantDeclaration node) {
    this.setResult(new JavaTypeSymbolReference(node.getName(), node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTQualifiedName node) {
    String finalName = String.join(".", node.getParts());
    JavaTypeSymbolReference type = new JavaTypeSymbolReference(finalName,
        node.getEnclosingScope().get(), 0);
    this.setResult(type);
  }
  
  public void handle(ASTDeclaratorId node) {
  }
  
  public void handle(ASTLastFormalParameter node) {
    node.getType().accept(this);
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      type.setDimension(1);
      this.setResult(type);
    }
  }
  
  public void handle(ASTLocalVariableDeclarationStatement node) {
    handle(node.getLocalVariableDeclaration());
  }
  
  public void handle(ASTCatchType node) {
    if (node.getQualifiedNames().size() == 1) {
      for (ASTQualifiedName qualifiedName : node.getQualifiedNames()) {
        String name = "";
        for (String s : qualifiedName.getParts()) {
          name = name + s;
        }
        this.setResult(new JavaTypeSymbolReference(name, node.getEnclosingScope().get(), 0));
      }
    }
    else {
      this.setResult(null);
    }
    
  }
  
  public void handle(ASTExpressionStatement node) {
    handle(node.getExpression());
  }
  
  public void handle(ASTInnerCreator node) {
    this.setResult(new JavaTypeSymbolReference(node.getName(), node.getEnclosingScope().get(), 0));
  }
  
  public void handle(ASTPrimaryGenericInvocationExpression node) {
    //// TODO: 05.08.2016 node.getTypeArguments() <String, Integer>?
    handle(node.getGenericInvocationSuffix());
  }
  
  public void handle(ASTGenericInvocationSuffix node) {
    if (node.nameIsPresent()) {
      JavaTypeSymbolReference methodType = new JavaTypeSymbolReference(node.getName().get(),
          node.getEnclosingScope().get(), 0);
      if (node.argumentsIsPresent()) {
        List<ActualTypeArgument> argList = new ArrayList<>();
        for (ASTExpression astExpression : node.getArguments().get().getExpressions()) {
          handle(astExpression);
          if (this.getResult().isPresent()) {
            ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false,
                this.getResult().get());
            argList.add(actualTypeArgument);
          }
        }
        methodType.setActualTypeArguments(argList);
        this.setResult(methodType);
      }
    }
  }
  
  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTExpression)
   */
  @Override
  public void handle(ASTExpression node) {
    node.accept(getRealThis());
  }
  
}
