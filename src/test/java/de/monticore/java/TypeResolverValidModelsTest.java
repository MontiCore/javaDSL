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
package de.monticore.java;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.java.cocos.annotations.AnnotationMethodModifiers;
import de.monticore.java.cocos.annotations.AnnotationMethodReturnTypes;
import de.monticore.java.cocos.annotations.AnnotationNameNotAsEnclosingType;
import de.monticore.java.cocos.annotations.AnnotationTypeModifiers;
import de.monticore.java.cocos.classes.ClassBoundErasuresAreDifferent;
import de.monticore.java.cocos.classes.ClassCanOnlyExtendClass;
import de.monticore.java.cocos.classes.ClassImplementsAllInterfaceMethods;
import de.monticore.java.cocos.classes.ClassInnerTypeNotNamedAsEnclosing;
import de.monticore.java.cocos.classes.ClassMethodSignatureNoOverrideEquivalent;
import de.monticore.java.cocos.classes.ClassNoCircularity;
import de.monticore.java.cocos.classes.ClassNoDuplicateDirectSuperInterface;
import de.monticore.java.cocos.classes.ClassNoFinalSuperClass;
import de.monticore.java.cocos.classes.ClassNoModifierDuplicate;
import de.monticore.java.cocos.classes.ClassNotStatic;
import de.monticore.java.cocos.classes.ClassOptionalBoundsAreInterfaces;
import de.monticore.java.cocos.classes.GenericClassNotSubClassOfThrowable;
import de.monticore.java.cocos.classes.NoProtectedOrPrivateTopLevelClass;
import de.monticore.java.cocos.classes.NonAbstractClassImplementsAllAbstractMethods;
import de.monticore.java.cocos.constructors.ConstructorFormalParametersDifferentName;
import de.monticore.java.cocos.constructors.ConstructorModifiersValid;
import de.monticore.java.cocos.constructors.ConstructorMustNamedAsClass;
import de.monticore.java.cocos.constructors.ConstructorNoAccessModifierPair;
import de.monticore.java.cocos.constructors.ConstructorNoDuplicateModifier;
import de.monticore.java.cocos.constructors.ConstructorsNoDuplicateSignature;
import de.monticore.java.cocos.enums.EnumConstructorValidModifiers;
import de.monticore.java.cocos.enums.EnumMayNotBeAbstract;
import de.monticore.java.cocos.enums.EnumMethodModifiersValid;
import de.monticore.java.cocos.enums.EnumModifiersValid;
import de.monticore.java.cocos.enums.EnumNoFinalizerMethod;
import de.monticore.java.cocos.expressions.AdditiveOpsValid;
import de.monticore.java.cocos.expressions.ArrayAccessValid;
import de.monticore.java.cocos.expressions.ArrayCreatorValid;
import de.monticore.java.cocos.expressions.ArrayDimensionByExpressionValid;
import de.monticore.java.cocos.expressions.ArrayInitializerValid;
import de.monticore.java.cocos.expressions.AssignmentCompatible;
import de.monticore.java.cocos.expressions.BitwiseOpsValid;
import de.monticore.java.cocos.expressions.BooleanAndOrValid;
import de.monticore.java.cocos.expressions.BooleanNotValid;
import de.monticore.java.cocos.expressions.CastConversionValid;
import de.monticore.java.cocos.expressions.ClassInnerInstanceCreationValid;
import de.monticore.java.cocos.expressions.ClassInstanceCreationValid;
import de.monticore.java.cocos.expressions.ComparisonValid;
import de.monticore.java.cocos.expressions.ConditionValid;
import de.monticore.java.cocos.expressions.FieldAccessValid;
import de.monticore.java.cocos.expressions.IdentityTestValid;
import de.monticore.java.cocos.expressions.InstanceOfValid;
import de.monticore.java.cocos.expressions.MethodGenericInvocationValid;
import de.monticore.java.cocos.expressions.MethodInvocationValid;
import de.monticore.java.cocos.expressions.MultiplicativeOpsValid;
import de.monticore.java.cocos.expressions.PrefixOpValid;
import de.monticore.java.cocos.expressions.PrimarySuperValid;
import de.monticore.java.cocos.expressions.PrimaryThisValid;
import de.monticore.java.cocos.expressions.ShiftOpValid;
import de.monticore.java.cocos.expressions.SuffixOpValid;
import de.monticore.java.cocos.fieldandlocalvars.FieldInitializerAssignmentCompatible;
import de.monticore.java.cocos.fieldandlocalvars.FieldModifierAccessCombinations;
import de.monticore.java.cocos.fieldandlocalvars.FieldNoDuplicateModifier;
import de.monticore.java.cocos.fieldandlocalvars.LocalVariableInitializerAssignmentCompatible;
import de.monticore.java.cocos.interfaces.InterfaceBoundErasuresAreDifferent;
import de.monticore.java.cocos.interfaces.InterfaceCannotDependSelf;
import de.monticore.java.cocos.interfaces.InterfaceCannotExtendClasses;
import de.monticore.java.cocos.interfaces.InterfaceInnerTypeNotNamedAsEnclosing;
import de.monticore.java.cocos.interfaces.InterfaceMethodSignatureNoOverrideEquivalent;
import de.monticore.java.cocos.interfaces.InterfaceMethodsNotAllowedModifiers;
import de.monticore.java.cocos.interfaces.InterfaceNoDuplicateModifier;
import de.monticore.java.cocos.interfaces.InterfaceNoFinalMethod;
import de.monticore.java.cocos.interfaces.InterfaceNoStaticMethod;
import de.monticore.java.cocos.interfaces.InterfaceOptionalBoundsAreInterfaces;
import de.monticore.java.cocos.methods.AbstractMethodDefinition;
import de.monticore.java.cocos.methods.MethodAbstractAndOtherModifiers;
import de.monticore.java.cocos.methods.MethodBodyAbsenceAndPresence;
import de.monticore.java.cocos.methods.MethodExceptionThrows;
import de.monticore.java.cocos.methods.MethodFormalParametersDifferentName;
import de.monticore.java.cocos.methods.MethodHiding;
import de.monticore.java.cocos.methods.MethodNoAccessPairModifier;
import de.monticore.java.cocos.methods.MethodNoDuplicateModifier;
import de.monticore.java.cocos.methods.MethodNoNativeAndStrictfp;
import de.monticore.java.cocos.methods.MethodOverride;
import de.monticore.java.cocos.methods.MethodReturnVoid;
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
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author$$
 * @since TODO
 */
public class TypeResolverValidModelsTest extends AbstractCoCoTestClass {
  
  HCJavaDSLTypeResolver typeResolver = new HCJavaDSLTypeResolver();
  
  @BeforeClass
  public static void init() {
    Log.enableFailQuick(false);
  }
  
  @Before
  public void setUp() {
    Log.getFindings().clear();
  }
  
  /* Test annotations CoCos */
  
  @Test
  public void TestValidAnnotationMethodModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AnnotationMethodModifiers(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/annotations/AnnotationMethodModifier");
  }
  
  @Test
  public void TestValidAnnotationMethodReturnType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AnnotationMethodReturnTypes(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/annotations/AnnotationMethodReturnType", checker);
  }
  
  @Test
  public void TestAnnotationNameNotAsEnclosingType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AnnotationNameNotAsEnclosingType());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/annotations/AnnotationNameNotAsEnclosingType", checker);
  }
  
  @Test
  public void TestValidAnnotationTypeModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AnnotationTypeModifiers(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/annotations/AnnotationTypeModifier", checker);
  }
  
  /* Test class CoCos */
  
  @Test
  public void TestClassBoundErasuresDifferent() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassBoundErasuresAreDifferent(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassBoundErasuresAreDifferent", checker);
  }
  
  @Test
  public void TestClassCanOnlyExtendClass() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassCanOnlyExtendClass(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassCanOnlyExtendClass", checker);
  }
  
  @Test
  public void TestClassImplementsAllInterfaceMethods() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassImplementsAllInterfaceMethods(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassImplementsAllInterfaceMethods", checker);
  }
  
  @Test
  public void TestClassInnerTypeNotNamedAsEnclosing() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassInnerTypeNotNamedAsEnclosing());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassInnerTypeNotNamedAsEnclosing", checker);
  }
  
  @Test
  public void TestClassMethodSignatureNoOverrideEquivalent() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassMethodSignatureNoOverrideEquivalent());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassMethodSignatureNoOverrideEquivalent", checker);
  }
  
  @Test
  public void TestClassNoCircularity() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNoCircularity(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassNoCircularity", checker);
  }
  
  @Test
  public void TestClassNoDuplicateDirectSuperInterface() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNoDuplicateDirectSuperInterface(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassNoDuplicateDirectSuperInterface", checker);
  }
  
  @Test
  public void TestClassNoFinalSuperClass() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNoFinalSuperClass());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassNoFinalSuperClass", checker);
  }
  
  @Test
  public void TestClassNoModifierDuplicate() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNoModifierDuplicate(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassNoModifierDuplicate", checker);
  }
  
  @Test
  public void TestClassNotStatic() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNotStatic());
    testModelNoErrors("src/test/resources", "typeSystemTestModels/valid/classes/ClassNotStatic",
        checker);
  }
  
  @Test
  public void TestClassOptionalBoundsAreInterfaces() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassOptionalBoundsAreInterfaces());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/ClassOptionalBoundsAreInterfaces", checker);
  }
  
  @Test
  public void TestGenericClassNotSubClassOfThrowable() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new GenericClassNotSubClassOfThrowable(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/GenericClassNotSubClassOfThrowable", checker);
  }
  
  @Test
  public void TestNonAbstractClassImplementsAllAbstractMethods() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new NonAbstractClassImplementsAllAbstractMethods(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/NonAbstractClassImplementsAllAbstractMethods", checker);
  }
  
  @Test
  public void TestNoProtectedOrPrivateTopLevelClass() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new NoProtectedOrPrivateTopLevelClass());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/classes/NoProtectedOrPrivateTopLevelClass", checker);
  }
  
  /* Test constructor CoCos */
  
  @Test
  public void ConstructorFormalParametersDifferentName() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorFormalParametersDifferentName());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/constructors/ConstructorFormalParametersDifferentName",
        checker);
  }
  
  @Test
  public void TestConstructorModifiersValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorModifiersValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/constructors/ConstructorModifiersValid", checker);
  }
  
  @Test
  public void TestConstructorMustNamedAsClass() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorMustNamedAsClass());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/constructors/ConstructorMustNamedAsClass", checker);
  }
  
  @Test
  public void TestConstructorNoAccessModifierPair() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorNoAccessModifierPair(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/constructors/ConstructorNoAccessModifierPair", checker);
  }
  
  @Test
  public void TestConstructorNoDuplicateModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorNoDuplicateModifier(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/constructors/ConstructorNoDuplicateModifier", checker);
  }
  
  @Test
  public void TestConstructorsNoDuplicateSignature() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorsNoDuplicateSignature());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/constructors/ConstructorsNoDuplicateSignature", checker);
  }
  
  /* Test enum CoCos */
  
  @Test
  public void TestEnumConstructorValidModifiers() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumConstructorValidModifiers());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/enums/EnumConstructorValidModifiers", checker);
  }
  
  @Test
  public void TestEnumMayNotBeAbstract() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumMayNotBeAbstract());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/enums/EnumMayNotBeAbstract", checker);
  }
  
  @Test
  public void TestEnumMethodModifiersValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumMethodModifiersValid());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/enums/EnumMethodModifiersValid", checker);
  }
  
  @Test
  public void TestEnumModifiersValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumModifiersValid());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/enums/EnumModifiersValid", checker);
  }
  
  @Test
  public void TestEnumNoFinalizerMethod() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumNoFinalizerMethod());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/enums/EnumToFinalizerMethod", checker);
  }
  
  /* Test for expression CoCos */
  
  @Test
  public void TestAdditiveOpsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AdditiveOpsValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/AdditiveOps", checker);
  }
  
  @Test
  public void TestArrayAccess() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ArrayAccessValid(typeResolver));
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    checker.addCoCo(new AssignmentCompatible(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/ArrayAccess", checker);
  }
  
  @Test
  public void TestArrayCreatorValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ArrayCreatorValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/ArrayCreator", checker);
  }
  
  @Test
  public void TestArrayDimensionByExpressionValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ArrayDimensionByExpressionValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/ArrayDimensionByExpression", checker);
  }
  
  // TODO: Test for ArrayDimensionByInitializerValid
  
  @Test
  public void TestArrayInitializerValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ArrayInitializerValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/ArrayInitializer", checker);
  }
  
  @Test
  public void TestAssignmentCompatible() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AssignmentCompatible(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/AssignmentCompatible", checker);
  }
  
  @Test
  public void TestBitwiseOpsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new BitwiseOpsValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/BitwiseOpsValid", checker);
  }
  
  @Test
  public void TestBooleanAndOrValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new BooleanAndOrValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/BooleanAndOrValid", checker);
  }
  
  @Test
  public void TestBooleanNotValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new BooleanNotValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/BooleanNot", checker);
  }
  
  @Test
  public void TestCastConversionValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new CastConversionValid(typeResolver));
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/CastConversion", checker);
  }
  
  @Test
  public void TestClassInnerInstanceCreationValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassInnerInstanceCreationValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/ClassInnerInstanceCreation", checker);
  }
  
  @Test
  public void TestClassInstanceCreationValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassInstanceCreationValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/ClassInstanceCreation", checker);
  }
  
  @Test
  public void TestComparison() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ComparisonValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/Comparison", checker);
  }
  
  @Test
  public void TestCondition() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConditionValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/Condition", checker);
  }
  
  @Test
  public void TestFieldAccess() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldAccessValid(typeResolver));
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/FieldAccess", checker);
  }
  
  @Test
  public void TestIdentityTestValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new IdentityTestValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/IdentityTest", checker);
  }
  
  @Test
  public void TestInstanceOf() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InstanceOfValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/InstanceOf", checker);
  }
  
  @Test
  public void TestMethodInvocationValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodInvocationValid(typeResolver));
    checker.addCoCo(new MethodGenericInvocationValid(typeResolver));
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/MethodInvocation", checker);
  }
  
  @Test
  public void TestMultiplicativeOpsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MultiplicativeOpsValid(typeResolver));
    checker.addCoCo(new AdditiveOpsValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/MultiplicativeOps", checker);
  }
  
  @Test
  public void TestPrefixOpValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new PrefixOpValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/PrefixOp", checker);
  }
  
  @Test
  public void TestPrimarySuperValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new PrimarySuperValid());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/PrimarySuper", checker);
  }
  
  @Test
  public void TestPrimaryThisValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new PrimaryThisValid());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/PrimaryThis", checker);
  }
  
  @Test
  public void TestShiftOpValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ShiftOpValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/ShiftOp", checker);
  }
  
  @Test
  public void TestSuffixOpValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new SuffixOpValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/expressions/SuffixOp", checker);
  }
  
  /* Test fieldandlocalvars CoCos */
  
  @Test
  public void TestFieldInitializerAssignmentCompatible() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/fieldsAndLocalVars/FieldInitializerAssignmentCompatible",
        checker);
  }
  
  @Test
  public void TestFieldModifierAccessCombinations() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldModifierAccessCombinations(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/fieldsAndLocalVars/FieldModifierAccessCombinations", checker);
  }
  
  @Test
  public void TestFieldNoDuplicateModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldNoDuplicateModifier(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/fieldsAndLocalVars/FieldNoDuplicateModifier", checker);
  }
  
  @Test
  public void TestLocalVariableInitializerAssignmentCompatible() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/fieldsAndLocalVars/LocalVariableInitializerAssignmentCompatible",
        checker);
  }
  
  /* Test interface CoCos */
  
  @Test
  public void TestInterfaceBoundErasuresAreDifferent() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceBoundErasuresAreDifferent(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceBoundErasuresAreDifferent", checker);
  }
  
  @Test
  public void TestInterfaceCannotDependSelf() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceCannotDependSelf());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceCannotDependSelf", checker);
  }
  
  @Test
  public void TestInterfaceCannotExtendClasses() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceCannotExtendClasses());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceCannotExtendClasses", checker);
  }
  
  @Test
  public void TestInterfaceInnerTypeNotNamedAsEnclosing() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceInnerTypeNotNamedAsEnclosing());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceInnerTypeNotNamedAsEnclosing", checker);
  }
  
  @Test
  public void TestInterfaceMethodSignatureNoOverrideEquivalent() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceMethodSignatureNoOverrideEquivalent());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceMethodSignatureNoOverrideEquivalent",
        checker);
  }
  
  @Test
  public void TestInterfaceMethodsNotAllowedModifiers() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceMethodsNotAllowedModifiers());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceMethodsNotAllowedModifiers", checker);
  }
  
  @Test
  public void TestInterfaceNoDuplicateModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceNoDuplicateModifier(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceNoDuplicateModifier", checker);
  }
  
  @Test
  public void TestInterfaceNoFinalMethod() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceNoFinalMethod());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceNoFinalMethod", checker);
  }
  
  @Test
  public void TestInterfaceNoStaticMethod() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceNoStaticMethod());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceNoStaticMethod", checker);
  }
  
  @Test
  public void TestInterfaceOptionalBoundsAreInterfaces() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceOptionalBoundsAreInterfaces());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/interfaces/InterfaceOptionalBoundsAreInterfaces", checker);
  }
  
  /* Test method CoCos */
  
  @Test
  public void TestAbstractMethodDefinition() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AbstractMethodDefinition());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/AbstractMethodDefinition", checker);
  }
  
  @Test
  public void TestMethodAbstractAndOtherModifiers() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodAbstractAndOtherModifiers());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodAbstractAndOtherModifiers", checker);
  }
  
  @Test
  public void TestMethodBodyAbsenceAndPresence() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodBodyAbsenceAndPresence());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodBodyAbsenceAndPresence", checker);
  }
  
  @Test
  public void TestMethodExceptionThrows() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodExceptionThrows(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodExceptionThrows", checker);
  }
  
  @Test
  public void TestMethodReturnVoid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodReturnVoid());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodReturnVoid", checker);
  }
  
  @Test
  public void TestMethodFormalParametersDifferentName() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodFormalParametersDifferentName());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodFormalParametersDifferentName", checker);
  }
  
  @Test
  public void TestMethodHiding() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodHiding());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodHiding", checker);
  }
  
  @Test
  public void TestMethodNoAccessPairModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodNoAccessPairModifier(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodNoAccessPairModifier", checker);
  }
  
  @Test
  public void TestMethodNoDuplicateModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodNoDuplicateModifier(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodNoDuplicateModifier", checker);
  }
  
  @Test
  public void TestMethodNoNativeAndStrictfp() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodNoNativeAndStrictfp());
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/methods/MethodNoNativeAndStrictfp", checker);
  }
  
  @Test
  public void TestMethodOverride() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodOverride());
    testModelNoErrors("src/test/resources", 
        "typeSystemTestModels/valid/methods/MethodOverride", checker);
  }
  
  /* Test name CoCos */
  
  @Test
  public void TestClassValidSuperTypes() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassValidSuperTypes(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/names/ClassValidSuperTypes", checker);
  }
  
  @Test
  public void TestInterfaceValidSuperTypes() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceValidSuperTypes(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/names/InterfaceValidSuperTypes", checker);
  }
  
  /* Test statement CoCos */
  
  @Test
  public void TestAssertIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AssertIsValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/AssertIsValid", checker);
  }
  
  @Test
  public void TestCatchIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new CatchIsValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/CatchIsValid", checker);
  }
  
  @Test
  public void TestDoWhileConditionHasBooleanType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new DoWhileConditionHasBooleanType(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/DoWhileConditionHasBooleanType", checker);
  }
  
  @Test
  public void TestForConditionHasBooleanType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ForConditionHasBooleanType(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/ForConditionHasBooleanType", checker);
  }
  
  @Test
  public void TestForEachIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ForEachIsValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/ForEachIsValid", checker);
  }
  
  @Test
  public void TestIfConditionHasBooleanType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new IfConditionHasBooleanType(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/IfConditionHasBooleanType", checker);
  }
  
  @Test
  public void TestResourceInTryStatementCloseable() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ResourceInTryStatementCloseable(typeResolver));
    testModelNoErrors("src/test/resources", 
        "typeSystemTestModels/valid/statements/ResourceInTryStatementCloseable", checker);
  }
  
  @Test
  public void TestReturnTypeAssignmentIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ReturnTypeAssignmentIsValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/ReturnTypeAssignmentIsValid", checker);
  }
  
  @Test
  public void TestSwitchStatementValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new SwitchStatementValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/SwitchStatementValid", checker);
  }
  
  @Test
  public void TestSynchronizedArgIsReftype() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new SynchronizedArgIsReftype(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/SynchronizedArgIsReftype", checker);
  }
  
  @Test
  public void TestThrowIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ThrowIsValid(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/ThrowIsValid", checker);
  }
  
  @Test
  public void TestWhileConditionHasBooleanType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new WhileConditionHasBooleanType(typeResolver));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/valid/statements/WhileConditionHasBooleanType", checker);
  }
  
  // TODO: Test for FieldNamesMustBePairWiseDifferent &
  // NestedTypeMayNotHaveSameNameAsEnclosingType
  
}
