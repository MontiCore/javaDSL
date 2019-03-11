/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.types;

import de.monticore.expressions.assignmentexpressions._ast.*;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.javaclassexpressions._ast.*;
import de.monticore.expressions.shiftexpressions._ast.*;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.mcbasicliterals._ast.ASTBooleanLiteral;
import de.monticore.mcbasicliterals._ast.ASTCharLiteral;
import de.monticore.mcbasicliterals._ast.ASTNullLiteral;
import de.monticore.mcbasicliterals._ast.ASTStringLiteral;
import de.monticore.mcjavaliterals._ast.ASTDoubleLiteral;
import de.monticore.mcjavaliterals._ast.ASTFloatLiteral;
import de.monticore.mcjavaliterals._ast.ASTIntLiteral;
import de.monticore.mcjavaliterals._ast.ASTLongLiteral;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.types.mcbasictypes._ast.*;
import de.monticore.types.mccollectiontypes._ast.*;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCArrayType;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCMultipleGenericType;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCTypeVariableDeclaration;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCWildcardType;
import de.monticore.types.mcsimplegenerictypes._ast.ASTMCBasicGenericType;

import java.util.*;
import java.util.Map.Entry;


public class HCJavaDSLTypeResolver extends GenericTypeResolver<JavaTypeSymbolReference>
    implements JavaDSLVisitor {
  
  protected Stack<List<JavaTypeSymbolReference>> parameterStack = new Stack<>();

  protected String fullName = "";

  protected boolean isCallExpr = false;
  
  public JavaDSLVisitor getRealThis() {
    return this;
  }
  
  public void handle(ASTFieldDeclaration node) {
    node.getMCType().accept(this);
  }
  
  public void handle(ASTLocalVariableDeclaration node) {
    node.getMCType().accept(this);
  }
  
  public void handle(ASTMCMultipleGenericType type) {
    // TODO MB : new Types !!!
    // handle(type.getSimpleReferenceTypeList().get(0));
  }

  protected void addTypeArguments(JavaTypeSymbolReference typeRef, List<ASTMCTypeArgument> typeArguments) {
    List<ActualTypeArgument> actualTypeArgumentList = new ArrayList<>();
    for (ASTMCTypeArgument typeArgument : typeArguments) {
      typeArgument.accept(this);
      ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false,
              this.getResult().get());
      actualTypeArgumentList.add(actualTypeArgument);
    }
    typeRef.setActualTypeArguments(actualTypeArgumentList);
  }
  public void handle(ASTMCBasicGenericType type) {
    JavaTypeSymbolReference typeSymbolReference = new JavaTypeSymbolReference(
            String.join(".", type.getNameList()), type.getEnclosingScope(), 0);
    String completeName = JavaDSLHelper.getCompleteName(typeSymbolReference);
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
            completeName, type.getEnclosingScope(), 0);
    addTypeArguments(finalType, type.getMCTypeArgumentList());
    this.setResult(finalType);
  }

  public void handle(ASTMCListType type) {
    JavaTypeSymbolReference typeSymbolReference = new JavaTypeSymbolReference(
            String.join(".", type.getNameList()), type.getEnclosingScope(), 0);
    String completeName = JavaDSLHelper.getCompleteName(typeSymbolReference);
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
            completeName, type.getEnclosingScope(), 0);
    addTypeArguments(finalType, type.getMCTypeArgumentList());
    this.setResult(finalType);
  }

  public void handle(ASTMCOptionalType type) {
    JavaTypeSymbolReference typeSymbolReference = new JavaTypeSymbolReference(
            String.join(".", type.getNameList()), type.getEnclosingScope(), 0);
    String completeName = JavaDSLHelper.getCompleteName(typeSymbolReference);
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
            completeName, type.getEnclosingScope(), 0);
    addTypeArguments(finalType, type.getMCTypeArgumentList());
    this.setResult(finalType);
  }

  public void handle(ASTMCMapType type) {
    JavaTypeSymbolReference typeSymbolReference = new JavaTypeSymbolReference(
            String.join(".", type.getNameList()), type.getEnclosingScope(), 0);
    String completeName = JavaDSLHelper.getCompleteName(typeSymbolReference);
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
            completeName, type.getEnclosingScope(), 0);
    addTypeArguments(finalType, type.getMCTypeArgumentList());
    this.setResult(finalType);
  }

  public void handle(ASTMCSetType type) {
    JavaTypeSymbolReference typeSymbolReference = new JavaTypeSymbolReference(
            String.join(".", type.getNameList()), type.getEnclosingScope(), 0);
    String completeName = JavaDSLHelper.getCompleteName(typeSymbolReference);
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
            completeName, type.getEnclosingScope(), 0);
    addTypeArguments(finalType, type.getMCTypeArgumentList());
    this.setResult(finalType);
  }

  public void handle(ASTMCArrayType type) {
    type.getMCType().accept(this);
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference typeSymbolReference = this.getResult().get();
      typeSymbolReference.setDimension(type.getDimensions());
      this.setResult(typeSymbolReference);
    }
  }
  
  public void handle(ASTMCQualifiedType type) {
    JavaTypeSymbolReference typeSymbolReference = new JavaTypeSymbolReference(
        String.join(".", type.getNameList()), type.getEnclosingScope(), 0);
    String completeName = JavaDSLHelper.getCompleteName(typeSymbolReference);
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(
        completeName, type.getEnclosingScope(), 0);
    this.setResult(finalType);
  }

  public void handle(ASTMCWildcardType type) {
    JavaTypeSymbolReference wildType = new JavaTypeSymbolReference("ASTWildcardType",
        type.getEnclosingScope(), 0);
    List<ActualTypeArgument> typeArguments = new ArrayList<>();
    if (type.isPresentLowerBound()) {
      type.getLowerBound().accept(this);
      if (this.getResult().isPresent()) {
        ActualTypeArgument typeArgument = new ActualTypeArgument(true, false,
            this.getResult().get());
        typeArguments.add(typeArgument);
      }
    }
    if (type.isPresentUpperBound()) {
      type.getUpperBound().accept(this);
      if (this.getResult().isPresent()) {
        ActualTypeArgument typeArgument = new ActualTypeArgument(false, true,
            this.getResult().get());
        typeArguments.add(typeArgument);
      }
    }
    wildType.setActualTypeArguments(typeArguments);
    this.setResult(wildType);
  }
  
  public void handle(ASTMCPrimitiveType type) {
    this.setResult(new JavaTypeSymbolReference(type.toString(), type.getEnclosingScope(), 0));
  }
  
  public void handle(ASTMCVoidType type) {
    this.setResult(new JavaTypeSymbolReference("void", type.getEnclosingScope(), 0));
  }
  
  public void handle(ASTMethodDeclaration node) {
    handle(node.getMethodSignature());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference methodType = this.getResult().get();
      this.setResult(methodType);
    }
  }
  
  public void handle(ASTMethodSignature node) {
    node.getMCReturnType().accept(this);
  }
  
  public void handle(ASTFormalParameter node) {
    node.getMCType().accept(this);
  }
  
  public void handle(ASTVariableDeclarator node) {
    if (node.isPresentVariableInititializerOrExpression()) {
      handle(node.getVariableInititializerOrExpression());
    }
  }
  
  public void handle(ASTVariableInitializer node) {
    node.accept(this);
  }
  
  public void handle(ASTPlusPrefixExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (!JavaDSLHelper.isNumericType(type)) {
        this.setResult(null);
      }
    }
  }

  public void handle(ASTMinusPrefixExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (!JavaDSLHelper.isNumericType(type)) {
        this.setResult(null);
      }
    }
  }

  public void handle(ASTIncPrefixExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (!JavaDSLHelper.isNumericType(type)) {
        this.setResult(null);
      }
    }
  }

  public void handle(ASTDecPrefixExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (!JavaDSLHelper.isNumericType(type)) {
        this.setResult(null);
      }
    }
  }

  public void handle(ASTSuperExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      Scope scope = node.getEnclosingScope();
      JavaTypeSymbolReference refType = this.getResult().get();
      JavaTypeSymbol type = node.getEnclosingScope()
          .<JavaTypeSymbol> resolve(refType.getName(), JavaTypeSymbol.KIND).get();
      Collection<JavaTypeSymbolReference> superTypes = new HashSet(type.getSuperTypes());
      if (type.getSuperClass().isPresent()) {
        superTypes.add(type.getSuperClass().get());
      }
      if (node.getSuperSuffix().isPresentArguments()) {
        List<JavaTypeSymbolReference> paramTypes = new ArrayList<>();
        for (ASTExpression paramExpression : node.getSuperSuffix().getArguments()
            .getExpressionList()) {
          this.handle(paramExpression);
          if (this.getResult().isPresent()) {
            paramTypes.add(this.getResult().get());
          }
        }
        if (node.getSuperSuffix().isPresentName()) {
          
          for (JavaTypeSymbolReference superType : superTypes) {
            JavaTypeSymbol currentSymbol = scope
                .<JavaTypeSymbol> resolve(superType.getName(), JavaTypeSymbol.KIND).get();
            HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methods = JavaDSLHelper
                .resolveManyInSuperType(node.getSuperSuffix().getName(), false,
                    superType, currentSymbol, new ArrayList<JavaTypeSymbolReference>(),
                    paramTypes);
            if (methods.size() == 1) {
              Entry<JavaMethodSymbol, JavaTypeSymbolReference> m = methods.entrySet().iterator()
                  .next();
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
  
  public void handle(ASTPlusExpression node) {
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
                .resolveTypeForExpressions(leftType, rightType, "+").orElse(null));
      }
    }
  }

  public void handle(ASTMinusExpression node) {
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
                .resolveTypeForExpressions(leftType, rightType, "-").orElse(null));
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
  
  public void handle(ASTDivideExpression node) {
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

  public void handle(ASTModuloExpression node) {
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
  
  public void handle(ASTRightShiftExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper
                .resolveTypeForExpressions(leftType, rightType, node.getShiftOp())
                .orElse(null));
      }
    }
  }
  
  public void handle(ASTLeftShiftExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper
                .resolveTypeForExpressions(leftType, rightType, node.getShiftOp())
                .orElse(null));
      }
    }
  }

  public void handle(ASTLogicalRightShiftExpression node) {
    handle(node.getLeftExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference leftType = this.getResult().get();
      handle(node.getRightExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference rightType = this.getResult().get();
        this.setResult(
            JavaDSLHelper
                .resolveTypeForExpressions(leftType, rightType, node.getShiftOp())
                .orElse(null));
      }
    }
  }

  public void handle(ASTLessEqualExpression node) {
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

  public void handle(ASTGreaterEqualExpression node) {
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

  public void handle(ASTLessThanExpression node) {
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

  public void handle(ASTGreaterThanExpression node) {
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
  
  public void handle(ASTEqualsExpression node) {
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
  
  public void handle(ASTNotEqualsExpression node) {
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

  public void handle(ASTBinaryAndExpression node) {
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
  
  public void handle(ASTBinaryXorExpression node) {
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
  
  public void handle(ASTIncSuffixExpression node) {
    handle(node.getExpression());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (!JavaDSLHelper.isNumericType(type)) {
        this.setResult(null);
      }
    }
  }

  public void handle(ASTDecSuffixExpression node) {
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
    ASTPrimaryGenericInvocationExpression genericInvocation = node
            .getPrimaryGenericInvocationExpression();
    genericInvocation.getExtTypeArguments().accept(this);
    if (this.getResult().isPresent()) {
      typeArguments.add(this.getResult().get());
    }
    else {
      this.setResult(null);
    }

    for (ASTExpression expression : genericInvocation.getGenericInvocationSuffix()
            .getArguments().getExpressionList()) {
      expression.accept(this);
      if (this.getResult().isPresent()) {
        actualArguments.add(this.getResult().get());
      }
      else {
        this.setResult(null);
      }
    }
    if (genericInvocation.getGenericInvocationSuffix().getArguments()
            .getExpressionList().size() == actualArguments.size()) {
      if (genericInvocation.getGenericInvocationSuffix().isPresentName()) {
        methodName = genericInvocation.getGenericInvocationSuffix().getName();
      }

      this.handle(node.getExpression());
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference expType = this.getResult().get();
        // String completeName = JavaDSLHelper.getCompleteName(expType);
        if (node.getEnclosingScope().resolve(expType.getName(), JavaTypeSymbol.KIND)
                .isPresent()) {
          JavaTypeSymbol expSymbol = (JavaTypeSymbol) node.getEnclosingScope()
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
  
  public void handle(ASTCallExpression node) {
    List<JavaTypeSymbolReference> paramTypes = new ArrayList<>();
    for (ASTExpression paramExpression : node.getArguments().getExpressionList()) {
      this.handle(paramExpression);
      if (this.getResult().isPresent()) {
        paramTypes.add(this.getResult().get());
      }
    }
    if (paramTypes.size() == node.getArguments().getExpressionList().size()) {
      parameterStack.push(paramTypes);
      ASTExpression expr = node.getExpression();
      isCallExpr = true;
      expr.accept(getRealThis());
      isCallExpr = false;
      parameterStack.pop();
    }
  }

  public void handle(ASTQualifiedNameExpression node) {
    if (!isCallExpr) {
      // push dummy element, otherwise we would resolve a method while searching for a field
      parameterStack.push(null);
    }
    isCallExpr = false;
    handle(node.getExpression());
    String name = node.getName();
    Scope scope = node.getEnclosingScope();
    if(!getResult().isPresent()) {
      // expression could be a package name. try to resolve the fullname
      fullName += "." + name;
      Collection<JavaTypeSymbol> resolvedTypes = scope.resolveMany(fullName, JavaTypeSymbol.KIND);
      if (resolvedTypes.size() == 1) {
        JavaTypeSymbol typeSymbol = resolvedTypes.iterator().next();
        setResult(new JavaTypeSymbolReference(typeSymbol.getFullName(), scope, 0));
      }
      // the result is either empty, because name is a package name again, or we found an acutal
      // symbol
      parameterStack.pop();
      return;
    }
    JavaTypeSymbolReference expType = getResult().get();
    setResult(null);

    if (expType.getDimension() > 0 && "length".equals(name)) {
      setResult(new JavaTypeSymbolReference("int", expType.getEnclosingScope(), 0));
      parameterStack.pop();
      return;
    }

    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) scope
        .resolve(expType.getName(), JavaTypeSymbol.KIND).get();

    if (!parameterStack.isEmpty() && parameterStack.peek() != null) {
      // try to resolve a method
      List<JavaTypeSymbolReference> paramTypes = parameterStack.peek();
      HashMap<JavaMethodSymbol, JavaTypeSymbolReference> resolvedMethods = JavaDSLHelper
          .resolveManyInSuperType(name, false, expType, typeSymbol, null,
              paramTypes);

      if (resolvedMethods.size() == 1) {
        JavaMethodSymbol methodSymbol = resolvedMethods.entrySet().iterator().next().getKey();
        HashMap<String, JavaTypeSymbolReference> substituted = JavaDSLHelper
            .getSubstitutedTypes(typeSymbol, expType);
        JavaTypeSymbolReference returnType = JavaDSLHelper.applyTypeSubstitution(substituted,
            methodSymbol.getReturnType());
        setResult(returnType);
      }
      return;
    }

    // try to resolve a local field
    Collection<JavaFieldSymbol> resolvedFields = typeSymbol.getSpannedScope().resolveDownMany(name,
        JavaFieldSymbol.KIND);
    if (resolvedFields.size() == 1) {
      JavaTypeSymbolReference fieldType = resolvedFields.iterator().next().getType();
      String completeTypeName = JavaDSLHelper.getCompleteName(fieldType);
      fieldType = new JavaTypeSymbolReference(completeTypeName, fieldType.getEnclosingScope(),
          fieldType.getDimension());
      fieldType.setActualTypeArguments(fieldType.getActualTypeArguments());
      setResult(fieldType);
      parameterStack.pop();
      return;
    }

    // try to resolve a local constant
    Collection<JavaTypeSymbol> resolvedConstants = typeSymbol.getSpannedScope()
        .resolveDownMany(name, JavaTypeSymbol.KIND);
    if (resolvedConstants.size() == 1) {
      JavaTypeSymbol type = resolvedConstants.iterator().next();
      setResult(new JavaTypeSymbolReference(type.getFullName(),
          scope, 0));
      parameterStack.pop();
      return;
    }

    // try to resolve a type
    Collection<JavaTypeSymbol> resolvedTypes = scope.resolveMany(name, JavaTypeSymbol.KIND);
    if (resolvedTypes.size() == 1) {
      JavaTypeSymbol type = resolvedTypes.iterator().next();
      setResult(new JavaTypeSymbolReference(type.getFullName(),
          scope, 0));
      parameterStack.pop();
      return;
    }
  }

  public void handle(ASTNameExpression node) {
    setResult(null);
    String name = node.getName();
    Scope scope = node.getEnclosingScope();
    String typeSymbolName = JavaDSLHelper.getEnclosingTypeSymbolName(scope);
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) scope.resolve(typeSymbolName, JavaTypeSymbol.KIND)
        .get();
    JavaTypeSymbolReference expType = new JavaTypeSymbolReference(typeSymbolName, scope, 0);

    if (!parameterStack.isEmpty() && parameterStack.peek() != null && isCallExpr) {
      // try to resolve a method
      List<JavaTypeSymbolReference> paramTypes = parameterStack.peek();
      HashMap<JavaMethodSymbol, JavaTypeSymbolReference> resolvedMethods = JavaDSLHelper
          .resolveManyInSuperType(name, false, expType, typeSymbol, null,
              paramTypes);

      if (resolvedMethods.size() == 1) {
        JavaMethodSymbol methodSymbol = resolvedMethods.entrySet().iterator().next().getKey();
        HashMap<String, JavaTypeSymbolReference> substituted = JavaDSLHelper
            .getSubstitutedTypes(typeSymbol, expType);
        JavaTypeSymbolReference returnType = JavaDSLHelper.applyTypeSubstitution(substituted,
            methodSymbol.getReturnType());
        setResult(returnType);
        return;
      }
    }

    // try to resolve a local variable or a field (in super type)
    Optional<JavaFieldSymbol> resolvedFields = JavaDSLHelper.resolveFieldInSuperType(scope, name);
    if (resolvedFields.isPresent()) {
      JavaTypeSymbolReference fieldType = resolvedFields.get().getType();
      String completeTypeName = JavaDSLHelper.getCompleteName(fieldType);
      JavaTypeSymbolReference newType = new JavaTypeSymbolReference(completeTypeName,
          fieldType.getEnclosingScope(), fieldType.getDimension());
      newType.setActualTypeArguments(fieldType.getActualTypeArguments());
      setResult(newType);
      return;
    }

    // try to resolve a local constant
    Collection<JavaTypeSymbol> resolvedConstants = typeSymbol.getSpannedScope()
        .resolveDownMany(name, JavaTypeSymbol.KIND);
    if (resolvedConstants.size() == 1) {
      JavaTypeSymbol type = resolvedConstants.iterator().next();
      setResult(new JavaTypeSymbolReference(type.getFullName(),
          scope, 0));
      return;
    }

    // try to resolve a type
    Collection<JavaTypeSymbol> resolvedTypes = scope.resolveMany(name, JavaTypeSymbol.KIND);
    if (resolvedTypes.size() == 1) {
      JavaTypeSymbol type = resolvedTypes.iterator().next();
      setResult(new JavaTypeSymbolReference(type.getFullName(),
          scope, 0));
      return;
    }

    // no symbol found for this expression. it could be a package name
    fullName = name;
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
      handle(node.getExtType());
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
      node.getExtType().accept(this);
      if (this.getResult().isPresent()) {
        JavaTypeSymbolReference castType = this.getResult().get();
        if (!JavaDSLHelper.castTypeConversionAvailable(expressionType, castType)) {
          this.setResult(null);
        }
      }
    }
  }
  
  public void handle(ASTBooleanNotExpression node) {
    node.getExpression().accept(getRealThis());
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
  
  public void handle(ASTLiteralExpression node) {
    node.getExtLiteral().accept(this);
  }
  
  public void handle(ASTPrimaryThisExpression node) {
    String className = JavaDSLHelper.getEnclosingTypeSymbolName(node);
    JavaTypeSymbol symbol = (JavaTypeSymbol) node.getEnclosingScope()
        .resolve(className, JavaTypeSymbol.KIND).get();
    
    if (symbol.isClass() || symbol.isEnum()) {
      this.setResult(
          new JavaTypeSymbolReference(symbol.getFullName(),
              node.getEnclosingScope(), 0));
    }
    else {
      this.setResult(null);
    }
  }
  
  public void handle(ASTPrimarySuperExpression node) {
    String name = JavaDSLHelper.getEnclosingTypeSymbolName(node);
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope()
        .resolve(name, JavaTypeSymbol.KIND).get();
    if (typeSymbol.isClass()) {
      if (typeSymbol.getSuperClass().isPresent()) {
        String completeName = JavaDSLHelper.getCompleteName(typeSymbol.getSuperClass().get());
        JavaTypeSymbolReference classType = new JavaTypeSymbolReference(completeName,
            node.getEnclosingScope(), 0);
        this.setResult(classType);
      }
      else {
        this.setResult(
            new JavaTypeSymbolReference("java.lang.Object", node.getEnclosingScope(), 0));
      }
    }
  }
  
  public void handle(ASTClassExpression node) {
    JavaTypeSymbolReference classType = new JavaTypeSymbolReference("java.lang.Class",
        node.getEnclosingScope(), 0);
    List<ActualTypeArgument> arg = new ArrayList<>();
    node.getExtReturnType().accept(getRealThis());
    if (getResult().isPresent()) {
      ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false,
          getResult().get());
      arg.add(actualTypeArgument);
      classType.setActualTypeArguments(arg);
      this.setResult(classType);
    }
  }

  public void handle(ASTIntLiteral node) {
    this.setResult(new JavaTypeSymbolReference("int", node.getEnclosingScope(), 0));
  }
  
  public void handle(ASTNullLiteral node) {
    this.setResult(new JavaTypeSymbolReference("null", node.getEnclosingScope(), 0));
  }
  
  public void handle(ASTCharLiteral node) {
    this.setResult(new JavaTypeSymbolReference("char", node.getEnclosingScope(), 0));
  }
  
  public void handle(ASTStringLiteral node) {
    this.setResult(
        new JavaTypeSymbolReference("java.lang.String", node.getEnclosingScope(), 0));
  }
  
  public void handle(ASTBooleanLiteral node) {
    this.setResult(new JavaTypeSymbolReference("boolean", node.getEnclosingScope(), 0));
  }

  public void handle(ASTDoubleLiteral node) {
    this.setResult(new JavaTypeSymbolReference("double", node.getEnclosingScope(), 0));
  }
  
  public void handle(ASTFloatLiteral node) {
    this.setResult(new JavaTypeSymbolReference("float", node.getEnclosingScope(), 0));
  }

  public void handle(ASTLongLiteral node) {
    this.setResult(new JavaTypeSymbolReference("long", node.getEnclosingScope(), 0));
  }

  public void handle(ASTAnonymousClass node) {
    handle(node.getCreatedName());
  }
  
  public void handle(ASTCreatedName node) {
    String finalName = "";
    List<ActualTypeArgument> list = new ArrayList<>();
    if (node.isPresentMCPrimitiveType()) {
      handle(node.getMCPrimitiveType());
      if (this.getResult().isPresent()) {
        finalName = this.getResult().get().getName();
      }
    } else {
      for (int i = 0; i < node.getIdentifierAndTypeArgumentList().size(); i++) {
        handle(node.getIdentifierAndTypeArgumentList().get(i));
        if (this.getResult().isPresent()) {
          if ("".equals(finalName)) {
            finalName = this.getResult().get().getName();
          }
          else {
            finalName = finalName + "." + this.getResult().get().getName();
          }
          if (i == node.getIdentifierAndTypeArgumentList().size() - 1) {
            list = new ArrayList<>(this.getResult().get().getActualTypeArguments());
          }
        }
        else {
          break;
        }
      }
    }
    JavaTypeSymbolReference type = new JavaTypeSymbolReference(finalName,
        node.getEnclosingScope(), 0);
    String completeName = JavaDSLHelper.getCompleteName(type);
    JavaTypeSymbolReference completeType = new JavaTypeSymbolReference(completeName,
        node.getEnclosingScope(), 0);
    completeType.setActualTypeArguments(list);
    this.setResult(completeType);
  }
  
  public void handle(ASTIdentifierAndTypeArgument node) {
    JavaTypeSymbolReference tempType = new JavaTypeSymbolReference(node.getName(),
        node.getEnclosingScope(), 0);
    String completeName = JavaDSLHelper.getCompleteName(tempType);
    List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();
    if (node.isPresentTypeArguments()) {
      if (!node.getTypeArguments().getMCTypeArgumentList().isEmpty()) {
        for (ASTMCTypeArgument typeArgument: node.getTypeArguments().getMCTypeArgumentList()) {
          typeArgument.accept(this);
          JavaTypeSymbolReference argType = this.getResult().get();
          ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, false, argType);
          actualTypeArguments.add(actualTypeArgument);
        }
        tempType.setActualTypeArguments(actualTypeArguments);
      }
    }
    JavaTypeSymbolReference finalType = new JavaTypeSymbolReference(completeName,
        node.getEnclosingScope(), 0);
    finalType.setActualTypeArguments(actualTypeArguments);
    this.setResult(finalType);
  }
  
  public void handle(ASTMethodBody node) {
    node.accept(this);
  }
  
  public void handle(ASTJavaBlock node) {
    node.getBlockStatementList().forEach(astBlockStatement -> astBlockStatement.accept(this));
  }
  
  public void handle(ASTIfStatement node) {
    handle(node.getCondition());
  }
  
  public void handle(ASTReturnStatement node) {
    if (node.isPresentExpression()) {
      handle(node.getExpression());
    }
  }
  
  public void handle(ASTArrayCreator node) {
    handle(node.getCreatedName());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      if (node.getArrayDimensionSpecifier() instanceof ASTArrayDimensionByInitializer) {
        ASTArrayDimensionByInitializer initializer = (ASTArrayDimensionByInitializer) node
            .getArrayDimensionSpecifier();
        type.setDimension(initializer.getDimList().size());
      }
      if (node.getArrayDimensionSpecifier() instanceof ASTArrayDimensionByExpression) {
        ASTArrayDimensionByExpression arrayDimensionByExpression = (ASTArrayDimensionByExpression) node
            .getArrayDimensionSpecifier();
        type.setDimension(arrayDimensionByExpression.getExpressionList().size());
      }
    }
  }
  
  public void handle(ASTArrayDimensionByInitializer node) {
    handle(node.getArrayInitializer());
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference arrType = this.getResult().get();
      arrType.setDimension(node.getDimList().size());
      this.setResult(arrType);
    }
  }
  
  public void handle(ASTArrayDimensionByExpression node) {
    JavaTypeSymbolReference arrayType = this.getResult().get();
    List<JavaTypeSymbolReference> listOfExpression = new ArrayList<>();
    for (ASTExpression astExpression : node.getExpressionList()) {
      handle(astExpression);
      if (!this.getResult().isPresent()) {
        break;
      }
      else {
        listOfExpression.add(this.getResult().get());
      }
    }
    if (listOfExpression.size() == node.getExpressionList().size()) {
      arrayType.setDimension(listOfExpression.size());
      this.setResult(arrayType);
    }
    else {
      this.setResult(null);
      
    }
  }
  
  public void handle(ASTMCTypeVariableDeclaration node) {
    this.setResult(new JavaTypeSymbolReference(
        node.getName(), node.getEnclosingScope(),
        0));
  }
  
  public void handle(ASTClassDeclaration node) {
    String completeName = JavaDSLHelper.getCompleteName(new JavaTypeSymbolReference(node.getName(),
        node.getEnclosingScope(), 0));
    JavaTypeSymbolReference classType = new JavaTypeSymbolReference(completeName,
        node.getEnclosingScope(), 0);
    List<ActualTypeArgument> classArgumentList = new ArrayList<>();
    if (node.isPresentMCTypeParameters()) {
      for (ASTMCTypeVariableDeclaration typeVariableDeclaration : node.getMCTypeParameters()
          .getMCTypeVariableDeclarationList()) {
        List<ActualTypeArgument> argList = new ArrayList<>();
        handle(typeVariableDeclaration);
        JavaTypeSymbolReference varType = this.getResult().get();
        for (ASTMCType type : typeVariableDeclaration.getUpperBoundList()) {
          type.accept(getRealThis());
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
        node.getEnclosingScope(), 0));
    JavaTypeSymbolReference interfaceType = new JavaTypeSymbolReference(completeName,
        node.getEnclosingScope(), 0);
    List<ActualTypeArgument> classArgumentList = new ArrayList<>();
    if (node.isPresentMCTypeParameters()) {
      for (ASTMCTypeVariableDeclaration typeVariableDeclaration : node.getMCTypeParameters()
          .getMCTypeVariableDeclarationList()) {
        List<ActualTypeArgument> actualTypeArgumentList = new ArrayList<>();
        JavaTypeSymbolReference argTypeSymbolReference = new JavaTypeSymbolReference(
            typeVariableDeclaration.getName(), typeVariableDeclaration.getEnclosingScope(),
            interfaceType.getDimension());
        for (ASTMCType type : typeVariableDeclaration.getUpperBoundList()) {
          type.accept(getRealThis());
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
    node.getExpression().accept(getRealThis());
  }
  
  public void handle(ASTConstantExpressionSwitchLabel node) {
    node.getConstantExpression().accept(this);
  }
  
  public void handle(ASTModifier node) {
    node.accept(this);
  }
  
  public void handle(ASTPrimitiveModifier node) {
    if (node.getModifier() == ASTConstantsJavaDSL.PRIVATE) {
      this.setResult(new JavaTypeSymbolReference("private", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.PUBLIC) {
      this.setResult(new JavaTypeSymbolReference("public", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.PROTECTED) {
      this.setResult(new JavaTypeSymbolReference("protected", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.STATIC) {
      this.setResult(new JavaTypeSymbolReference("static", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.TRANSIENT) {
      this.setResult(new JavaTypeSymbolReference("transient", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.FINAL) {
      this.setResult(new JavaTypeSymbolReference("final", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.ABSTRACT) {
      this.setResult(new JavaTypeSymbolReference("abstract", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.NATIVE) {
      this.setResult(new JavaTypeSymbolReference("native", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.SYNCHRONIZED) {
      this.setResult(
          new JavaTypeSymbolReference("synchronized", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.VOLATILE) {
      this.setResult(new JavaTypeSymbolReference("volatile", node.getEnclosingScope(), 0));
    }
    if (node.getModifier() == ASTConstantsJavaDSL.STRICTFP) {
      this.setResult(new JavaTypeSymbolReference("strictfp", node.getEnclosingScope(), 0));
    }
  }
  
  public void handle(ASTEnumDeclaration node) {
    this.setResult(new JavaTypeSymbolReference(node.getName(), node.getEnclosingScope(), 0));
  }
  
  public void handle(ASTEnumConstantDeclaration node) {
    this.setResult(new JavaTypeSymbolReference(node.getName(), node.getEnclosingScope(), 0));
  }
  
  public void handle(ASTMCQualifiedName node) {
    String finalName = String.join(".", node.getPartList());
    JavaTypeSymbolReference type = new JavaTypeSymbolReference(finalName,
        node.getEnclosingScope(), 0);
    this.setResult(type);
  }
  
  public void handle(ASTDeclaratorId node) {
  }
  
  public void handle(ASTLastFormalParameter node) {
    node.getMCType().accept(this);
    if (this.getResult().isPresent()) {
      JavaTypeSymbolReference type = this.getResult().get();
      type.setDimension(1);
      this.setResult(type);
    }
  }
  
  public void handle(ASTLocalVariableDeclarationStatement node) {
    node.getLocalVariableDeclaration().accept(getRealThis());
  }
  
  public void handle(ASTCatchType node) {
    if (node.getMCQualifiedNameList().size() == 1) {
      for (ASTMCQualifiedName qualifiedName : node.getMCQualifiedNameList()) {
        String name = "";
        for (String s : qualifiedName.getPartList()) {
          name = name + s;
        }
        this.setResult(new JavaTypeSymbolReference(name, node.getEnclosingScope(), 0));
      }
    }
    else {
      this.setResult(null);
    }
    
  }
  
  public void handle(ASTExpressionStatement node) {
    node.getExpression().accept(getRealThis());
  }
  
  public void handle(ASTInnerCreator node) {
    this.setResult(new JavaTypeSymbolReference(node.getName(), node.getEnclosingScope(), 0));
  }
  
  public void handle(ASTPrimaryGenericInvocationExpression node) {
    //// TODO: 05.08.2016 node.getTypeArguments() <String, Integer>?
    node.getGenericInvocationSuffix().accept(getRealThis());
  }
  
  public void handle(ASTGenericInvocationSuffix node) {
    if (node.isPresentName()) {
      JavaTypeSymbolReference methodType = new JavaTypeSymbolReference(node.getName(),
          node.getEnclosingScope(), 0);
      if (node.isPresentArguments()) {
        List<ActualTypeArgument> argList = new ArrayList<>();
        for (ASTExpression astExpression : node.getArguments().getExpressionList()) {
          astExpression.accept(getRealThis());
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

  @Override
  public void handle(ASTExpression node) {
    node.accept(getRealThis());
  }

}
