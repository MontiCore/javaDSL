/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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
import java.util.HashMap;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTAnonymousClass;
import de.monticore.java.javadsl._ast.ASTArrayCreator;
import de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression;
import de.monticore.java.javadsl._ast.ASTArrayDimensionByInitializer;
import de.monticore.java.javadsl._ast.ASTCatchType;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTConstantExpressionSwitchLabel;
import de.monticore.java.javadsl._ast.ASTCreatedName;
import de.monticore.java.javadsl._ast.ASTDeclaratorId;
import de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration;
import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._ast.ASTExplicitGenericInvocation;
import de.monticore.java.javadsl._ast.ASTExplicitGenericInvocationSuffix;
import de.monticore.java.javadsl._ast.ASTExpression;
import de.monticore.java.javadsl._ast.ASTExpressionStatement;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTIdentifierAndTypeArgument;
import de.monticore.java.javadsl._ast.ASTIfStatement;
import de.monticore.java.javadsl._ast.ASTInnerCreator;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTJavaBlock;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._ast.ASTLastFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclarationStatement;
import de.monticore.java.javadsl._ast.ASTMethodBody;
import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTMethodSignature;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimaryExpression;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._ast.ASTReturnStatement;
import de.monticore.java.javadsl._ast.ASTSwitchStatement;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTVariableInitializer;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
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
 * @version $$Revision$$, $$Date$$
 * @since TODO
 */

public class HCJavaDSLTypeResolver extends JavaDSLTypeResolver<JavaTypeSymbolReference> {
  
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
    if (node.getVariableInitializer().isPresent()) {
      handle(node.getVariableInitializer().get());
    }
  }
  
  public void handle(ASTVariableInitializer node) {
    node.accept(this);
  }
  
  public void handle(ASTExpression node) {
    if (node.expressionIsPresent() && (node.prefixOpIsPresent() || node.suffixOpIsPresent())) {
      handle(node.getExpression().get());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference type = this.getResult().get();
        if (!JavaDSLHelper.isNumericType(type)) {
          this.setResult(null);
        }
      }
    }
    else if (node.arrayExpressionIsPresent() && node.indexExpressionIsPresent()) {
      handle(node.getArrayExpression().get());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference arrayType = this.getResult().get();
        if (arrayType.getDimension() > 0) {
          if (JavaDSLHelper.isReifiableType(arrayType)) {
            handle(node.getIndexExpression().get());
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
    else if (node.leftExpressionIsPresent() && node.rightExpressionIsPresent()) {
      JavaTypeSymbolReference leftType;
      JavaTypeSymbolReference rightType;
      if (node.additiveOpIsPresent()) {
        handle(node.getLeftExpression().get());
        if (this.getResult().isPresent()) {
          leftType = this.getResult().get();
          handle(node.getRightExpression().get());
          if (this.getResult().isPresent()) {
            rightType = this.getResult().get();
            this.setResult(
                JavaDSLHelper
                    .resolveTypeForExpressions(leftType, rightType, node.getAdditiveOp().get())
                    .orElse(null));
          }
        }
      }
      else if (node.multiplicativeOpIsPresent()) {
        handle(node.getLeftExpression().get());
        if (this.getResult().isPresent()) {
          leftType = this.getResult().get();
          handle(node.getRightExpression().get());
          if (this.getResult().isPresent()) {
            rightType = this.getResult().get();
            this.setResult(
                JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "multiplicativeOp")
                    .orElse(null));
          }
        }
      }
      else if (node.shiftOpIsPresent()) {
        handle(node.getLeftExpression().get());
        if (this.getResult().isPresent()) {
          leftType = this.getResult().get();
          handle(node.getRightExpression().get());
          if (this.getResult().isPresent()) {
            rightType = this.getResult().get();
            this.setResult(
                JavaDSLHelper
                    .resolveTypeForExpressions(leftType, rightType, node.getShiftOp().get())
                    .orElse(null));
          }
        }
      }
      else if (node.comparisonIsPresent()) {
        handle(node.getLeftExpression().get());
        if (this.getResult().isPresent()) {
          leftType = this.getResult().get();
          handle(node.getRightExpression().get());
          if (this.getResult().isPresent()) {
            rightType = this.getResult().get();
            this.setResult(
                JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "comparison")
                    .orElse(null));
          }
        }
      }
      else if (node.identityTestIsPresent()) {
        handle(node.getLeftExpression().get());
        if (this.getResult().isPresent()) {
          leftType = this.getResult().get();
          handle(node.getRightExpression().get());
          if (this.getResult().isPresent()) {
            rightType = this.getResult().get();
            this.setResult(
                JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "identityTest")
                    .orElse(null));
          }
        }
      }
      else if (node.binaryAndOpIsPresent() || node.binaryOrOpIsPresent() || node
          .binaryXorOpIsPresent()) {
        handle(node.getLeftExpression().get());
        if (this.getResult().isPresent()) {
          leftType = this.getResult().get();
          handle(node.getRightExpression().get());
          if (this.getResult().isPresent()) {
            rightType = this.getResult().get();
            this.setResult(
                JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "bitwiseOp")
                    .orElse(null));
          }
        }
      }
      else if (node.booleanAndOpIsPresent() || node.booleanOrOpIsPresent()) {
        handle(node.getLeftExpression().get());
        if (this.getResult().isPresent()) {
          leftType = this.getResult().get();
          handle(node.getRightExpression().get());
          if (this.getResult().isPresent()) {
            rightType = this.getResult().get();
            this.setResult(
                JavaDSLHelper.resolveTypeForExpressions(leftType, rightType, "booleanOp")
                    .orElse(null));
          }
        }
      }
      else if (node.assignmentIsPresent()) {
        handle(node.getLeftExpression().get());
        if (this.getResult().isPresent()) {
          leftType = this.getResult().get();
          handle(node.getRightExpression().get());
          if (this.getResult().isPresent()) {
            rightType = this.getResult().get();
            if (JavaDSLHelper.assignmentConversionAvailable(rightType, leftType)) {
              this.setResult(leftType);
            }
            else {
              this.setResult(null);
            }
          }
        }
      }
    }
    else if (node.explicitGenericInvocationIsPresent() && node.expressionIsPresent()) {
      List<JavaTypeSymbolReference> actualArguments = new ArrayList<>();
      List<JavaTypeSymbolReference> typeArguments = new ArrayList<>();
      String methodName = "";
      ASTExplicitGenericInvocation genericInvocation = node.getExplicitGenericInvocation().get();
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
        for (ASTExpression expression : genericInvocation.getExplicitGenericInvocationSuffix()
            .getArguments().get().getExpressions()) {
          expression.accept(this);
          if (this.getResult().isPresent()) {
            actualArguments.add(this.getResult().get());
          }
          else {
            this.setResult(null);
          }
        }
        if (genericInvocation.getExplicitGenericInvocationSuffix().getArguments().get()
            .getExpressions().size() == actualArguments.size()) {
          if (genericInvocation.getExplicitGenericInvocationSuffix().getName().isPresent()) {
            methodName = genericInvocation.getExplicitGenericInvocationSuffix().getName().get();
          }
          if (node.getExpression().get().primaryExpressionIsPresent()) {
            if (node.getExpression().get().getPrimaryExpression().get().nameIsPresent()) {
              String symbolName = node.getExpression().get().getPrimaryExpression().get().getName()
                  .get();
              this.handle(node.getExpression().get().getPrimaryExpression().get());
              JavaTypeSymbolReference type = this.getResult().get();
              if (node.getEnclosingScope().get().resolve(type.getName(), JavaTypeSymbol.KIND)
                  .isPresent()) {
                JavaTypeSymbol expSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                    .resolve(type.getName(), JavaTypeSymbol.KIND).get();
                if (type.getName().equals(symbolName) || type.getName().endsWith(symbolName)) {
                  HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                      .resolveManyInSuperType(methodName, true, null, expSymbol,
                          typeArguments, actualArguments);
                  if (methodSymbols.size() == 1) {
                    this.setResult(methodSymbols.values().iterator().next());
                  }
                  else {
                    this.setResult(null);
                  }
                }
                else {
                  HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                      .resolveManyInSuperType(methodName, false, type, expSymbol,
                          typeArguments, actualArguments);
                  if (methodSymbols.size() == 1) {
                    this.setResult(methodSymbols.values().iterator().next());
                  }
                  else {
                    this.setResult(null);
                  }
                }
              }
            }
            else {
              this.handle(node.getExpression().get().getPrimaryExpression().get());
              if (this.getResult().isPresent()) {
                JavaTypeSymbolReference type = this.getResult().get();
                if (node.getEnclosingScope().get().resolve(type.getName(), JavaTypeSymbol.KIND)
                    .isPresent()) {
                  JavaTypeSymbol exSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                      .resolve(type.getName(),
                          JavaTypeSymbol.KIND)
                      .get();
                  HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                      .resolveManyInSuperType(methodName, false, type, exSymbol,
                          typeArguments, actualArguments);
                  if (methodSymbols.size() == 1) {
                    this.setResult(methodSymbols.values().iterator().next());
                  }
                  else {
                    this.setResult(null);
                  }
                }
              }
            }
          }
          else {
            this.handle(node.getExpression().get());
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
    }
    if (node.callExpressionIsPresent()) {
      List<JavaTypeSymbolReference> paramTypes = new ArrayList<>();
      for (ASTExpression astExpression : node.getParameterExpression()) {
        this.handle(astExpression);
        if (this.getResult().isPresent()) {
          paramTypes.add(this.getResult().get());
        }
      }
      if (paramTypes.size() == node.getParameterExpression().size()) {
        ASTExpression callExpression = node.getCallExpression().get();
        if (callExpression.primaryExpressionIsPresent()) {
          // b(...);
          if (callExpression.getPrimaryExpression().get().nameIsPresent()) {
            String methodName = callExpression.getPrimaryExpression().get().getName().get();
            String enclosingTypeName = JavaDSLHelper
                .getEnclosingTypeSymbolName(callExpression.getPrimaryExpression().get());
            JavaTypeSymbol enclosingType = (JavaTypeSymbol) node.getEnclosingScope().get()
                .resolve(enclosingTypeName, JavaTypeSymbol.KIND).get();
            HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                .resolveManyInSuperType(methodName, false, null, enclosingType, null,
                    paramTypes);
            if (methodSymbols.size() == 1) {
              this.setResult(methodSymbols.values().iterator().next());
            }
            else {
              this.setResult(null);
            }
          }
        }
        else if (callExpression.expressionIsPresent() && callExpression.nameIsPresent()) {
          String name = callExpression.getName().get();
          if (callExpression.getExpression().get().primaryExpressionIsPresent()) {
            if (callExpression.getExpression().get().getPrimaryExpression().get().nameIsPresent()) {
              String symbolName = callExpression.getExpression().get().getPrimaryExpression().get()
                  .getName().get();
              this.handle(callExpression.getExpression().get());
              JavaTypeSymbolReference type = this.getResult().get();
              if (node.getEnclosingScope().get().resolve(type.getName(),
                  JavaTypeSymbol.KIND).isPresent()) {
                JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                    .resolve(type.getName(), JavaTypeSymbol.KIND).get();
                if (type.getName().endsWith(symbolName) || type.getName().equals(symbolName)) {
                  HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                      .resolveManyInSuperType(name, true, null, typeSymbol, null,
                          paramTypes);
                  if (methodSymbols.size() == 1) {
                    this.setResult(methodSymbols.values().iterator().next());
                  }
                  else {
                    this.setResult(null);
                  }
                }
                else {
                  HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                      .resolveManyInSuperType(name, false, type, typeSymbol, null,
                          paramTypes);
                  if (methodSymbols.size() == 1) {
                    HashMap<String, JavaTypeSymbolReference> substituted = JavaDSLHelper
                        .getSubstitutedTypes(typeSymbol, type);
                    JavaTypeSymbolReference fReturn = JavaDSLHelper
                        .applyTypeSubstitution(substituted,
                            methodSymbols.values().iterator().next());
                    this.setResult(fReturn);
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
            else {
              this.handle(callExpression.getExpression().get());
              if (this.getResult().isPresent()) {
                JavaTypeSymbolReference type = this.getResult().get();
                // String completeName = JavaDSLHelper.getCompleteName(type);
                if (node.getEnclosingScope().get().resolveMany(type.getName(), JavaTypeSymbol.KIND)
                    .size() == 1) {
                  JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                      .resolve(type.getName(), JavaTypeSymbol.KIND).get();
                  HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                      .resolveManyInSuperType(name, false, null, typeSymbol, null,
                          paramTypes);
                  if (methodSymbols.size() == 1) {
                    HashMap<String, JavaTypeSymbolReference> substituted = JavaDSLHelper
                        .getSubstitutedTypes(typeSymbol, type);
                    JavaTypeSymbolReference fReturn = JavaDSLHelper
                        .applyTypeSubstitution(substituted,
                            methodSymbols.values().iterator().next());
                    this.setResult(fReturn);
                  }
                  else {
                    this.setResult(null);
                  }
                }
              }
            }
          }
          else {
            this.handle(callExpression.getExpression().get());
            if (this.getResult().isPresent()) {
              JavaTypeSymbolReference expType = this.getResult().get();
              // String completeName = JavaDSLHelper.getCompleteName(expType);
              if (node.getEnclosingScope().get().resolveMany(expType.getName(), JavaTypeSymbol.KIND)
                  .size() == 1) {
                JavaTypeSymbol expSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                    .resolve(expType.getName(), JavaTypeSymbol.KIND).get();
                HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                    .resolveManyInSuperType(name, false, null, expSymbol, null, paramTypes);
                if (methodSymbols.size() == 1) {
                  this.setResult(methodSymbols.values().iterator().next());
                }
                else {
                  this.setResult(null);
                }
              }
            }
          }
        }
      }
      else {
        this.setResult(null);
      }
    }
    else if (node.nameIsPresent() && node.expressionIsPresent()) {
      handle(node.getExpression().get());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference expType = this.getResult().get();
        if ("class".equals(node.getName().get())) {
          JavaTypeSymbolReference classType = new JavaTypeSymbolReference("java.lang.Class",
              node.getEnclosingScope().get(), 0);
          List<ActualTypeArgument> arg = new ArrayList<>();
          ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false, expType);
          arg.add(actualTypeArgument);
          classType.setActualTypeArguments(arg);
          this.setResult(classType);
        }
        else if (this.getResult().get().getDimension() > 0
            && "length".equals(node.getName().get())) {
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
          if ("super".equals(node.getName().get())) {
            if (typeSymbol.getSuperClass().isPresent()) {
              JavaTypeSymbolReference classType = typeSymbol.getSuperClass().get();
              JavaTypeSymbolReference superType = new JavaTypeSymbolReference(classType.getName(),
                  node.getEnclosingScope().get(), classType.getDimension());
              superType.setActualTypeArguments(classType.getActualTypeArguments());
              this.setResult(superType);
            }
            else {
              this.setResult(null);
            }
          }
          else if (typeSymbol.isEnum()) {
            if (typeSymbol.getSpannedScope()
                .resolveMany(node.getName().get(), JavaTypeSymbol.KIND)
                .size() == 1) {
              this.setResult(
                  new JavaTypeSymbolReference(typeSymbol.getName(),
                      typeSymbol.getEnclosingScope(),
                      0));
            }
            else {
              this.setResult(null);
            }
          }
          else if (typeSymbol.getSpannedScope()
              .resolveMany(node.getName().get(), JavaFieldSymbol.KIND)
              .size() == 1) {
            JavaTypeSymbolReference fieldType = typeSymbol.getField(node.getName().get()).get()
                .getType();
            String completeTypeName = JavaDSLHelper.getCompleteName(fieldType);
            JavaTypeSymbolReference newType = new JavaTypeSymbolReference(completeTypeName,
                fieldType.getEnclosingScope(), fieldType.getDimension());
            newType.setActualTypeArguments(fieldType.getActualTypeArguments());
            this.setResult(newType);
          }
          else if (typeSymbol.getSpannedScope()
              .resolveMany(node.getName().get(), JavaMethodSymbol.KIND)
              .size() == 1) {
            HashMap<String, JavaTypeSymbolReference> substituted = JavaDSLHelper
                .getSubstitutedTypes(typeSymbol, expType);
            JavaTypeSymbolReference sReturnType = JavaDSLHelper.applyTypeSubstitution(substituted,
                typeSymbol.getMethod(node.getName().get()).get().getReturnType());
            //// TODO: 01.11.2016 complete name?
            this.setResult(sReturnType);
          }
          else {
            this.setResult(null);
          }
        }
        else {
          String name = expType.getName() + "." + node.getName().get();
          JavaTypeSymbolReference pName = new JavaTypeSymbolReference(name,
              node.getEnclosingScope().get(), 0);
          pName = new JavaTypeSymbolReference(JavaDSLHelper.getCompleteName(pName),
              pName.getEnclosingScope(), pName.getDimension());
          this.setResult(pName);
        }
      }
    }
    else if (node.innerCreatorIsPresent() && node.expressionIsPresent()) {
      handle(node.getExpression().get());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference expType = this.getResult().get();
        String completeName = JavaDSLHelper.getCompleteName(expType);
        handle(node.getInnerCreator().get());
        JavaTypeSymbolReference innerType = this.getResult().get();
        JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
            completeName + "." + innerType.getName(), innerType.getEnclosingScope(), 0);
        this.setResult(finalType);
      }
    }
    else if (node.getCreator().isPresent()) {
      node.getCreator().get().accept(this);
    }
    else if (node.getPrimaryExpression().isPresent()) {
      handle(node.getPrimaryExpression().get());
    }
    else if (node.instanceofTypeIsPresent() && node.expressionIsPresent()) {
      handle(node.getExpression().get());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference expType = this.getResult().get();
        handle(node.getInstanceofType().get());
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
    else if (node.typeCastTypeIsPresent() && node.expressionIsPresent()) {
      handle(node.getExpression().get());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference expressionType = new JavaTypeSymbolReference(
            JavaDSLHelper.getCompleteName(this.getResult().get()),
            this.getResult().get().getEnclosingScope(), this.getResult().get().getDimension());
        node.getTypeCastType().get().accept(this);
        if (this.getResult().isPresent()) {
          JavaTypeSymbolReference castType = this.getResult().get();
          if (!JavaDSLHelper.castTypeConversionAvailable(expressionType, castType)) {
            this.setResult(null);
          }
        }
      }
    }
    else if (node.expressionIsPresent() && node.booleanNotIsPresent()) {
      handle(node.getExpression().get());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference expressionType = this.getResult()
            .get();
        if ("!".equals(node.getBooleanNot().get())
            && !JavaDSLHelper.isBooleanType(expressionType)) {
          this.setResult(null);
        }
        if ("~".equals(node.getBooleanNot().get())
            && !JavaDSLHelper.isIntegralType(expressionType)) {
          this.setResult(null);
        }
      }
    }
    else if (node.conditionIsPresent() && node.trueExpressionIsPresent() && node
        .falseExpressionIsPresent()) {
      handle(node.getCondition().get());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference conditionType = this.getResult().get();
        if (!JavaDSLHelper.isBooleanType(conditionType)) {
          this.setResult(null);
        }
        else {
          handle(node.getTrueExpression().get());
          if (this.getResult().isPresent()) {
            JavaTypeSymbolReference trueType = this.getResult().get();
            handle(node.getFalseExpression().get());
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
  }
  
  public void handle(ASTPrimaryExpression node) {
    if (node.expressionIsPresent()) {
      handle(node.getExpression().get());
    }
    else if (node.nameIsPresent() && node.isThis()) {
      if (JavaDSLHelper
          .resolveFieldInSuperType(node.getEnclosingScope().get(), node.getName().get())
          .isPresent()) {
        JavaFieldSymbol fieldSymbol = JavaDSLHelper
            .resolveFieldInSuperType(node.getEnclosingScope().get(), node.getName().get()).get();
        if (fieldSymbol.getType() != null) {
          this.setResult(JavaDSLHelper
              .convertToWildCard(fieldSymbol.getType()));
        }
        else if (fieldSymbol.getAstNode().isPresent()) {
          ASTJavaDSLNode javaDSLNode = (ASTJavaDSLNode) fieldSymbol.getAstNode().get();
          javaDSLNode.accept(this);
        }
      }
      // else if
      // (node.getEnclosingScope().get().resolveMany(node.getName().get(),
      // JavaTypeSymbol.KIND)
      // .size() == 1) {
      // this.setResult(
      // new JavaTypeSymbolReference(node.getName().get(),
      // node.getEnclosingScope().get(), 0));
      // }
      // else if (JavaDSLHelper
      // .visibleTypeSymbolFound(node.getEnclosingScope().get(),
      // node.getName().get())
      // .isPresent()) {
      // this.setResult(
      // new JavaTypeSymbolReference(node.getName().get(),
      // node.getEnclosingScope().get(), 0));
      // }
      else {
        JavaTypeSymbolReference type = new JavaTypeSymbolReference(node.getName().get(),
            node.getEnclosingScope().get(), 0);
        String completeName = JavaDSLHelper.getCompleteName(type);
        this.setResult(
            new JavaTypeSymbolReference(completeName, node.getEnclosingScope().get(), 0));
      }
    }
    else if (node.nameIsPresent() && node.getEnclosingScope().isPresent()) {
      if (JavaDSLHelper
          .resolveFieldInSuperType(node.getEnclosingScope().get(), node.getName().get())
          .isPresent()) {
        JavaFieldSymbol fieldSymbol = JavaDSLHelper
            .resolveFieldInSuperType(node.getEnclosingScope().get(), node.getName().get()).get();
        if (fieldSymbol.getType() != null) {
          this.setResult(JavaDSLHelper.convertToWildCard(fieldSymbol.getType()));
        }
        else if (fieldSymbol.getAstNode().isPresent()) {
          ASTJavaDSLNode javaDSLNode = (ASTJavaDSLNode) fieldSymbol.getAstNode().get();
          javaDSLNode.accept(this);
        }
      }
      // else if
      // (node.getEnclosingScope().get().resolveMany(node.getName().get(),
      // JavaTypeSymbol.KIND)
      // .size() == 1) {
      // this.setResult(
      // new JavaTypeSymbolReference(node.getName().get(),
      // node.getEnclosingScope().get(), 0));
      // }
      // else if (JavaDSLHelper
      // .visibleTypeSymbolFound(node.getEnclosingScope().get(),
      // node.getName().get())
      // .isPresent()) {
      // this.setResult(
      // new JavaTypeSymbolReference(node.getName().get(),
      // node.getEnclosingScope().get(), 0));
      // }
      else {
        JavaTypeSymbolReference type = new JavaTypeSymbolReference(node.getName().get(),
            node.getEnclosingScope().get(), 0);
        String completeName = JavaDSLHelper.getCompleteName(type);
        this.setResult(
            new JavaTypeSymbolReference(completeName, node.getEnclosingScope().get(), 0));
      }
    }
    else if (node.getLiteral().isPresent()) {
      node.getLiteral().get().accept(this);
    }
    else if (node.isThis()) {
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
    else if (node.isSuper()) {
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
      else {
        this.setResult(null);
      }
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
    if (node.getModifier() == 1) {
      this.setResult(new JavaTypeSymbolReference("private", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 2) {
      this.setResult(new JavaTypeSymbolReference("public", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 3) {
      this.setResult(new JavaTypeSymbolReference("protected", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 4) {
      this.setResult(new JavaTypeSymbolReference("static", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 5) {
      this.setResult(new JavaTypeSymbolReference("transient", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 6) {
      this.setResult(new JavaTypeSymbolReference("final", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 7) {
      this.setResult(new JavaTypeSymbolReference("abstract", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 8) {
      this.setResult(new JavaTypeSymbolReference("native", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 10) {
      this.setResult(
          new JavaTypeSymbolReference("synchronized", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 12) {
      this.setResult(new JavaTypeSymbolReference("volatile", node.getEnclosingScope().get(), 0));
    }
    if (node.getModifier() == 13) {
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
  
  public void handle(ASTExplicitGenericInvocation node) {
    //// TODO: 05.08.2016 node.getTypeArguments() <String, Integer>?
    handle(node.getExplicitGenericInvocationSuffix());
  }
  
  public void handle(ASTExplicitGenericInvocationSuffix node) {
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
  
}
