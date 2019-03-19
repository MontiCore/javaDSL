/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.types;

import de.monticore.expressions.assignmentexpressions._ast.*;
import de.monticore.expressions.assignmentexpressions._cocos.AssignmentExpressionsCoCoChecker;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.commonexpressions._ast.ASTArguments;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsCoCoChecker;
import de.monticore.java.cocos.annotations.AnnotationMethodModifiers;
import de.monticore.java.cocos.annotations.AnnotationMethodReturnTypes;
import de.monticore.java.cocos.annotations.AnnotationNameNotAsEnclosingType;
import de.monticore.java.cocos.annotations.AnnotationTypeModifiers;
import de.monticore.java.cocos.classes.*;
import de.monticore.java.cocos.constructors.*;
import de.monticore.java.cocos.enums.*;
import de.monticore.java.cocos.expressions.*;
import de.monticore.java.cocos.fieldandlocalvars.*;
import de.monticore.java.cocos.interfaces.*;
import de.monticore.java.cocos.methods.*;
import de.monticore.java.cocos.methods.MethodOverride;
import de.monticore.java.cocos.names.ClassValidSuperTypes;
import de.monticore.java.cocos.names.InterfaceValidSuperTypes;
import de.monticore.java.cocos.statements.AssertIsValid;
import de.monticore.java.cocos.statements.CatchIsValid;
import de.monticore.java.cocos.statements.DoWhileConditionHasBooleanType;
import de.monticore.java.cocos.statements.ForConditionHasBooleanType;
import de.monticore.java.cocos.statements.ForEachIsValid;
import de.monticore.java.cocos.statements.IfConditionHasBooleanType;
import de.monticore.java.cocos.statements.ResourceInTryStatementCloseable;
import de.monticore.java.cocos.statements.ReturnTypeAssignmentIsValid;
import de.monticore.java.cocos.statements.SwitchStatementValid;
import de.monticore.java.cocos.statements.SynchronizedArgIsReftype;
import de.monticore.java.cocos.statements.ThrowIsValid;
import de.monticore.java.cocos.statements.WhileConditionHasBooleanType;
import de.monticore.java.javadsl._ast.ASTSwitchStatement;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.expressions.javaclassexpressions._ast.*;
import de.monticore.expressions.javaclassexpressions._cocos.JavaClassExpressionsCoCoChecker;
import de.monticore.expressions.shiftexpressions._ast.*;
import de.monticore.expressions.shiftexpressions._ast.ASTLogicalRightShiftExpression;
import de.monticore.expressions.shiftexpressions._cocos.ShiftExpressionsCoCoChecker;

/**
 *  on 23.10.2016.
 */
public class JavaDSLTypeChecker {

  HCJavaDSLTypeResolver typeResolver = new HCJavaDSLTypeResolver();

  public JavaDSLCoCoChecker getAllTypeChecker(){
    JavaDSLCoCoChecker javaDSLTypeCheckers = new JavaDSLChecker();
    javaDSLTypeCheckers.addChecker(getAnnotationChecker());
    javaDSLTypeCheckers.addChecker(getClassChecker());
    javaDSLTypeCheckers.addChecker(getConstructorChecker());
    javaDSLTypeCheckers.addChecker(getEnumChecker());
    javaDSLTypeCheckers.addChecker(getCommonExpressionChecker());
    javaDSLTypeCheckers.addChecker(getShiftExpressionChecker());
    javaDSLTypeCheckers.addChecker(getJavaClassExpressionChecker());
    javaDSLTypeCheckers.addChecker(getAssignmentExpressionChecker());
    javaDSLTypeCheckers.addChecker(getJavaExpressionChecker());
    javaDSLTypeCheckers.addChecker(getFieldAndLocalVarsChecker());
    javaDSLTypeCheckers.addChecker(getInterfaceChecker());
    javaDSLTypeCheckers.addChecker(getMethodChecker());
    javaDSLTypeCheckers.addChecker(getNameChecker());
    javaDSLTypeCheckers.addChecker(getStatementChecker());
    return javaDSLTypeCheckers;
  }
  
  public JavaDSLCoCoChecker getAnnotationChecker(){
    JavaDSLCoCoChecker annotationChecker = new JavaDSLCoCoChecker();
    annotationChecker.addCoCo(new AnnotationMethodModifiers(typeResolver));
    annotationChecker.addCoCo(new AnnotationMethodReturnTypes(typeResolver));
    annotationChecker.addCoCo(new AnnotationNameNotAsEnclosingType());
    annotationChecker.addCoCo(new AnnotationTypeModifiers(typeResolver));
    return annotationChecker;
  }

  public JavaDSLCoCoChecker getClassChecker(){
    JavaDSLCoCoChecker classChecker = new JavaDSLCoCoChecker();
    classChecker.addCoCo(new AbstractMethodMayNotBePrivate());
    classChecker.addCoCo(new AbstractMethodMayNotBeStatic());
    classChecker.addCoCo(new AbstractOrNativeMethodHasNoBody());
    classChecker.addCoCo(new ClassBoundErasuresAreDifferent(typeResolver ));
    classChecker.addCoCo(new ClassCanOnlyExtendClass(typeResolver));
    classChecker.addCoCo(new ClassImplementsAllInterfaceMethods(typeResolver));
    classChecker.addCoCo(new ClassInnerTypeNotNamedAsEnclosing());
    classChecker.addCoCo(new ClassMethodSignatureNoOverrideEquivalent());
    classChecker.addCoCo(new ClassNoCircularity(typeResolver));
    classChecker.addCoCo(new ClassNoDuplicateDirectSuperInterface(typeResolver));
    classChecker.addCoCo(new ClassNoFinalSuperClass());
    classChecker.addCoCo(new ClassNoModifierDuplicate(typeResolver));
    classChecker.addCoCo(new ClassNotStatic());
    classChecker.addCoCo(new ClassOptionalBoundsAreInterfaces());
    classChecker.addCoCo(new ConcreteClassMayNotHaveAbstractMethod());
    classChecker.addCoCo(new ConstructorMayNotBeAbstract());
    classChecker.addCoCo(new GenericClassNotSubClassOfThrowable(typeResolver));
    classChecker.addCoCo(new NonAbstractClassImplementsAllAbstractMethods(typeResolver));
    classChecker.addCoCo(new NoProtectedOrPrivateTopLevelClass());
    classChecker.addCoCo(new NotAbstractAndNotFinal());
    classChecker.addCoCo(new SuperClassMayNotBeFinal());
    return classChecker;
  }

  public JavaDSLCoCoChecker getConstructorChecker(){
    JavaDSLCoCoChecker constructorChecker = new JavaDSLCoCoChecker();
    constructorChecker.addCoCo(new ConstructorFormalParametersDifferentName());
    constructorChecker.addCoCo(new ConstructorModifiersValid(typeResolver));
    constructorChecker.addCoCo(new ConstructorMustNamedAsClass());
    constructorChecker.addCoCo(new ConstructorNoAccessModifierPair(typeResolver));
    constructorChecker.addCoCo(new ConstructorNoDuplicateModifier(typeResolver));
    constructorChecker.addCoCo(new ConstructorsNoDuplicateSignature());
    return constructorChecker;
  }


  public JavaDSLCoCoChecker getEnumChecker(){
    JavaDSLCoCoChecker enumChecker = new JavaDSLCoCoChecker();
    enumChecker.addCoCo(new EnumConstructorValidModifiers());
    enumChecker.addCoCo(new EnumMayNotBeAbstract());
    enumChecker.addCoCo(new EnumMethodModifiersValid());
    enumChecker.addCoCo(new EnumModifiersValid());
    enumChecker.addCoCo(new EnumNoFinalizerMethod());
    return enumChecker;
  }

  public ShiftExpressionsCoCoChecker getShiftExpressionChecker(){
    ShiftExpressionsCoCoChecker expressionChecker = new ShiftExpressionsCoCoChecker();
    expressionChecker.addCoCo(new ArrayAccessValid(typeResolver));
    expressionChecker.addCoCo(new QualifiedNameValid(typeResolver));
    expressionChecker.addCoCo(new LogicalRightShiftOpValid(typeResolver));
    expressionChecker.addCoCo(new RightShiftOpValid(typeResolver));
    expressionChecker.addCoCo(new LeftShiftOpValid(typeResolver));
    expressionChecker.addCoCo(new PrimaryThisValid());
    return expressionChecker;
  }
  
  public CommonExpressionsCoCoChecker getCommonExpressionChecker(){
    CommonExpressionsCoCoChecker expressionChecker = new CommonExpressionsCoCoChecker();
    expressionChecker.addCoCo(new BooleanAndValid(typeResolver));
    expressionChecker.addCoCo(new BooleanNotValid(typeResolver));
    expressionChecker.addCoCo(new BooleanOrValid(typeResolver));
    expressionChecker.addCoCo(new LogicalNotValid(typeResolver));
    expressionChecker.addCoCo(new ConditionValid(typeResolver));
    expressionChecker.addCoCo(new MethodInvocationValid(typeResolver));
    expressionChecker.addCoCo(new MultOpValid(typeResolver));
    expressionChecker.addCoCo(new ModuloOpValid(typeResolver));
    expressionChecker.addCoCo(new DivideOpValid(typeResolver));
    expressionChecker.addCoCo(new PlusOpValid(typeResolver));
    expressionChecker.addCoCo(new MinusOpValid(typeResolver));
    expressionChecker.addCoCo(new GreaterEqualOpValid(typeResolver));
    expressionChecker.addCoCo(new GreaterThanOpValid(typeResolver));
    expressionChecker.addCoCo(new LessEqualOpValid(typeResolver));
    expressionChecker.addCoCo(new LessThanOpValid(typeResolver));
    expressionChecker.addCoCo(new NotEqualsTestValid(typeResolver));
    expressionChecker.addCoCo(new EqualsTestValid(typeResolver));
    return expressionChecker;
  }
  
  public AssignmentExpressionsCoCoChecker getAssignmentExpressionChecker(){
    AssignmentExpressionsCoCoChecker expressionChecker = new AssignmentExpressionsCoCoChecker();
    expressionChecker.addCoCo(new AssignmentCompatible(typeResolver));
    expressionChecker.addCoCo(new BinaryOrOpValid(typeResolver));
    expressionChecker.addCoCo(new BinaryXorOpValid(typeResolver));
    expressionChecker.addCoCo(new BinaryAndOpValid(typeResolver));
    expressionChecker.addCoCo(new IncSuffixOpValid(typeResolver));
    expressionChecker.addCoCo(new DecSuffixOpValid(typeResolver));
    expressionChecker.addCoCo(new PlusPrefixOpValid(typeResolver));
    expressionChecker.addCoCo(new MinusPrefixOpValid(typeResolver));
    expressionChecker.addCoCo(new IncPrefixOpValid(typeResolver));
    expressionChecker.addCoCo(new DecPrefixOpValid(typeResolver));
    return expressionChecker;
  }
  
  public JavaClassExpressionsCoCoChecker getJavaClassExpressionChecker(){
    JavaClassExpressionsCoCoChecker expressionChecker = new JavaClassExpressionsCoCoChecker();
    expressionChecker.addCoCo(new CastConversionValid(typeResolver));
    expressionChecker.addCoCo(new InstanceOfValid(typeResolver));
    expressionChecker.addCoCo(new MethodGenericInvocationValid(typeResolver));
    expressionChecker.addCoCo(new NameExpValid(typeResolver));
    expressionChecker.addCoCo(new PrimarySuperValid());
    return expressionChecker;
  }

  public JavaDSLCoCoChecker getJavaExpressionChecker(){
    JavaDSLCoCoChecker expressionChecker = new JavaDSLCoCoChecker();
    expressionChecker.addCoCo(new ArrayDimensionByExpressionValid(typeResolver));
    expressionChecker.addCoCo(new ArrayInitializerValid(typeResolver));
    expressionChecker.addCoCo(new ClassInnerInstanceCreationValid(typeResolver));
    expressionChecker.addCoCo(new ClassInstanceCreationValid(typeResolver));
    return expressionChecker;
  }
  public JavaDSLCoCoChecker getFieldAndLocalVarsChecker(){
    JavaDSLCoCoChecker fieldChecker = new JavaDSLCoCoChecker();
    fieldChecker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    fieldChecker.addCoCo(new FieldModifierAccessCombinations(typeResolver));
    fieldChecker.addCoCo(new FieldNoDuplicateModifier(typeResolver));
    fieldChecker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    return fieldChecker;
  }

  public JavaDSLCoCoChecker getInterfaceChecker(){
    JavaDSLCoCoChecker interfaceChecker = new JavaDSLCoCoChecker();
    interfaceChecker.addCoCo(new InterfaceBoundErasuresAreDifferent(typeResolver));
    interfaceChecker.addCoCo(new InterfaceCannotDependSelf());
    interfaceChecker.addCoCo(new InterfaceCannotExtendClasses());
    interfaceChecker.addCoCo(new InterfaceInnerTypeNotNamedAsEnclosing());
    interfaceChecker.addCoCo(new InterfaceMethodSignatureNoOverrideEquivalent());
    interfaceChecker.addCoCo(new InterfaceMethodsNotAllowedModifiers());
    interfaceChecker.addCoCo(new InterfaceNoDuplicateModifier(typeResolver));
    interfaceChecker.addCoCo(new InterfaceNoFinalMethod());
    interfaceChecker.addCoCo(new InterfaceNoStaticMethod());
    interfaceChecker.addCoCo(new InterfaceOptionalBoundsAreInterfaces());
    return interfaceChecker;
  }
  public JavaDSLCoCoChecker getMethodChecker(){
    JavaDSLCoCoChecker methodChecker = new JavaDSLCoCoChecker();
    methodChecker.addCoCo(new AbstractMethodDefinition());
    methodChecker.addCoCo(new MethodAbstractAndOtherModifiers());
    methodChecker.addCoCo(new MethodBodyAbsenceAndPresence());
    methodChecker.addCoCo(new MethodExceptionThrows(typeResolver));
    methodChecker.addCoCo(new MethodFormalParametersDifferentName());
    methodChecker.addCoCo(new MethodHiding());
    methodChecker.addCoCo(new MethodNoAccessPairModifier(typeResolver));
    methodChecker.addCoCo(new MethodNoDuplicateModifier(typeResolver));
    methodChecker.addCoCo(new MethodNoNativeAndStrictfp());
    methodChecker.addCoCo(new MethodOverride());
    return methodChecker;
  }


  public JavaDSLCoCoChecker getNameChecker(){
    JavaDSLCoCoChecker nameChecker = new JavaDSLCoCoChecker();
    nameChecker.addCoCo(new ClassValidSuperTypes(typeResolver));
    nameChecker.addCoCo(new InterfaceValidSuperTypes(typeResolver));
    return nameChecker;
  }


  public JavaDSLCoCoChecker getStatementChecker(){
    JavaDSLCoCoChecker statementChecker = new JavaDSLCoCoChecker();
    statementChecker.addCoCo(new AssertIsValid(typeResolver));
    statementChecker.addCoCo(new CatchIsValid(typeResolver));
    statementChecker.addCoCo(new DoWhileConditionHasBooleanType(typeResolver));
    statementChecker.addCoCo(new ForConditionHasBooleanType(typeResolver));
    statementChecker.addCoCo(new ForEachIsValid(typeResolver));
    statementChecker.addCoCo(new IfConditionHasBooleanType(typeResolver));
    statementChecker.addCoCo(new ResourceInTryStatementCloseable(typeResolver));
    statementChecker.addCoCo(new ReturnTypeAssignmentIsValid(typeResolver));
    statementChecker.addCoCo(new SwitchStatementValid(typeResolver));
    statementChecker.addCoCo(new SynchronizedArgIsReftype(typeResolver));
    statementChecker.addCoCo(new ThrowIsValid(typeResolver));
    statementChecker.addCoCo(new WhileConditionHasBooleanType(typeResolver));
    return statementChecker;
  }


  private static class JavaDSLChecker extends JavaDSLCoCoChecker {

    @Override
    public void traverse(ASTSwitchStatement node) {}

    @Override
    public void traverse(ASTQualifiedNameExpression node) {}

    @Override
    public void traverse(ASTThisExpression node) {}

    @Override
    public void traverse(ASTSuperExpression node) {}

    @Override
    public void traverse(ASTGenericInvocationExpression node) {}

    @Override
    public void traverse(ASTArrayExpression node) {}

    @Override
    public void traverse(ASTCallExpression node) {}

    @Override
    public void traverse(ASTTypeCastExpression node) {}

    @Override
    public void traverse(ASTIncSuffixExpression node) {}
    
    @Override
    public void traverse(ASTDecSuffixExpression node) {}

    @Override
    public void traverse(ASTPlusPrefixExpression node) {}
    
    @Override
    public void traverse(ASTMinusPrefixExpression node) {}
    
    @Override
    public void traverse(ASTIncPrefixExpression node) {}
    
    @Override
    public void traverse(ASTDecPrefixExpression node) {}

    @Override
    public void traverse(ASTBooleanNotExpression node) {}

    @Override
    public void traverse(ASTLogicalNotExpression node) {}

    @Override
    public void traverse(ASTMultExpression node) {}
    
    @Override
    public void traverse(ASTPlusExpression node) {}
    
    @Override
    public void traverse(ASTMinusExpression node) {}

    @Override
    public void traverse(ASTLeftShiftExpression node) {}
    
    @Override
    public void traverse(ASTRightShiftExpression node) {}
    
    @Override
    public void traverse(ASTLogicalRightShiftExpression node) {}

    @Override
    public void traverse(ASTLessEqualExpression node) {}
    
    @Override
    public void traverse(ASTGreaterEqualExpression node) {}
    
    @Override
    public void traverse(ASTLessThanExpression node) {}
    
    @Override
    public void traverse(ASTGreaterThanExpression node) {}

    @Override
    public void traverse(ASTInstanceofExpression node) {}

    @Override
    public void traverse(ASTEqualsExpression node) {}
    
    @Override
    public void traverse(ASTNotEqualsExpression node) {}

    @Override
    public void traverse(ASTBinaryAndExpression node) {}

    @Override
    public void traverse(ASTBinaryXorExpression node) {}

    @Override
    public void traverse(ASTBinaryOrOpExpression node) {}

    @Override
    public void traverse(ASTBooleanAndOpExpression node) {}

    @Override
    public void traverse(ASTBooleanOrOpExpression node) {}

    @Override
    public void traverse(ASTConditionalExpression node) {}

    @Override
    public void traverse(ASTPlusAssignmentExpression node) {}
    
    @Override
    public void traverse(ASTMultAssignmentExpression node) {}
    
    @Override
    public void traverse(ASTDivideAssignmentExpression node) {}
    
    @Override
    public void traverse(ASTAndAssignmentExpression node) {}
    
    @Override
    public void traverse(ASTOrAssignmentExpression node) {}
    
    @Override
    public void traverse(ASTRightShiftAssignmentExpression node) {}
    
    @Override
    public void traverse(ASTModuloAssignmentExpression node) {}
    
    @Override
    public void traverse(ASTLeftShiftAssignmentExpression node) {}

    @Override
    public void traverse(ASTBracketExpression node) {}

    @Override
    public void traverse(ASTPrimaryThisExpression node) {}

    @Override
    public void traverse(ASTPrimarySuperExpression node) {}

    @Override
    public void traverse(ASTLiteralExpression node) {}

    @Override
    public void traverse(ASTNameExpression node) {}

    @Override
    public void traverse(ASTClassExpression node) {}

    @Override
    public void traverse(ASTPrimaryGenericInvocationExpression node) {}

    @Override
    public void traverse(ASTGenericInvocationSuffix node) {}

    @Override
    public void traverse(ASTSuperSuffix node) {}

    @Override
    public void traverse(ASTArguments node) {}
  }
}
