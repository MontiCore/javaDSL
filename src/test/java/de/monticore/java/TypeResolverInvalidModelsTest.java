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

import java.util.Arrays;
import java.util.Collection;

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
import de.monticore.java.cocos.expressions.BinaryAndOpValid;
import de.monticore.java.cocos.expressions.BinaryOrOpValid;
import de.monticore.java.cocos.expressions.BinaryXorOpValid;
import de.monticore.java.cocos.expressions.BooleanAndValid;
import de.monticore.java.cocos.expressions.BooleanNotValid;
import de.monticore.java.cocos.expressions.BooleanOrValid;
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
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;

/**
 *
 * Created by Odgrlb
 */
public class TypeResolverInvalidModelsTest extends AbstractCoCoTestClass {

  HCJavaDSLTypeResolver typeResolver = new HCJavaDSLTypeResolver();

  @BeforeClass
  public static void init() {
    Log.enableFailQuick(false);
  }

  @Before
  public void setUp() {
    Log.getFindings().clear();
  }

  /*
   * Tests for annotation CoCos
   */

  @Test
  public void TestInvalidAnnotationMethodModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AnnotationMethodModifiers(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0101 method in an annotation type declaration must not be declared final at method 'getName'."),
        Finding.error(
            "0xA0102 method in an annotation type declaration must not be declared native at method 'getName1'."),
        Finding.error(
            "0xA0103 method in an annotation type declaration must not be declared private at method 'getName2'."),
        Finding.error(
            "0xA0104 method in an annotation type declaration must not be declared protected at method 'getName3'."),
        Finding.error(
            "0xA0105 method in an annotation type declaration must not be declared static at method 'getName4'."),
        Finding.error(
            "0xA0106 method in an annotation type declaration must not be declared strictFP at method 'getName5'."),
        Finding.error(
            "0xA0107 method in an annotation type declaration must not be declared synchronized at method 'getName6'."),
        Finding.error(
            "0xA0108 method in an annotation type declaration must not be declared transient at method 'getName7'."),
        Finding.error(
            "0xA0109 method in an annotation type declaration must not be declared volatile at method 'getName8'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/annotations/AnnotationMethodModifier", checker,
        expectedErrors);
  }

  @Test
  public void TestInvalidAnnotationMethodReturnType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AnnotationMethodReturnTypes(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0110 return type of a method in an annotation type declaration may only be a primitive, String, Class, an Annotation or an Enum at method 'getInteger'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/annotations/AnnotationMethodReturnType", checker,
        expectedErrors);
  }

  @Test
  public void TestAnnotationNameNotAsEnclosingType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AnnotationNameNotAsEnclosingType());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0111 annotation type can not be named as the enclosing type."),
        Finding.error(
            "0xA0111 annotation type can not be named as the enclosing type."),
        Finding.error(
            "0xA0111 annotation type can not be named as the enclosing type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/annotations/AnnotationNameNotAsEnclosingType", checker,
        expectedErrors);
  }

  @Test
  public void TestInvalidAnnotationTypeModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AnnotationTypeModifiers(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0112 annotation type must not be declared final at declaration of annotation 'getType'."),
        Finding.error(
            "0xA0113 annotation type must not be declared native at declaration of annotation 'getType1'."),
        Finding.error(
            "0xA0114 annotation type must not be declared private at declaration of annotation 'getType2'."),
        Finding.error(
            "0xA0115 annotation type must not be declared protected at declaration of annotation 'getType3'."),
        Finding.error(
            "0xA0116 annotation type must not be declared static at declaration of annotation 'getType4'."),
        Finding.error(
            "0xA0117 annotation type must not be declared synchronized at declaration of annotation 'getType5'."),
        Finding.error(
            "0xA0118 annotation type must not be declared transient at declaration of annotation 'getType6'."),
        Finding.error(
            "0xA0119 annotation type must not be declared volatile at declaration of annotation 'getType7'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/annotations/AnnotationTypeModifier", checker, expectedErrors);
  }

  /*
  * Test for class CoCos
   */
  @Test
  public void TestClassBoundErasuresDifferent() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassBoundErasuresAreDifferent(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0201 bounds of type-variable T must not have same erasures.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassBoundErasuresAreDifferent", checker,
        expectedErrors);
  }

  @Test
  public void TestClassCanOnlyExtendClass() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassCanOnlyExtendClass(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0202 class is not extending class in declaration of 'ClassCanOnlyExtendClass'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassCanOnlyExtendClass", checker, expectedErrors);
  }

  @Test
  public void TestClassImplementsAllInterfaceMethods() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassImplementsAllInterfaceMethods(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0207 method 'getName' of the interface 'List' must be implemented in class 'ClassImplementsAllInterfaceMethods."),
        Finding.error(
            "0xA0205 private method 'getLetter' of the interface 'List' is overridden in class 'ClassImplementsAllInterfaceMethods."),
        Finding.error(
            "0xA0207 method 'getLastName' of the interface 'List' must be implemented in class 'ClassImplementsAllInterfaceMethods.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassImplementsAllInterfaceMethods", checker,
        expectedErrors);
  }

  @Test
  public void TestClassInnerTypeNotNamedAsEnclosing() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassInnerTypeNotNamedAsEnclosing());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0209 inner class must not be named same as enclosing class."),
        Finding.error("0xA0210 inner interface must not be named same as enclosing class.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassInnerTypeNotNamedAsEnclosing", checker,
        expectedErrors);
  }

  @Test
  public void TestClassMethodSignatureNoOverrideEquivalent() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassMethodSignatureNoOverrideEquivalent());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0212 method 'getName' is duplicated in class 'ClassMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0212 method 'getName' is duplicated in class 'ClassMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0212 method 'setName' is duplicated in class 'ClassMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0211 erasure of method 'setList' is same with another method in class 'ClassMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0212 method 'get' is duplicated in class 'ClassMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0211 erasure of method 'setNaming' is same with another method in class 'ClassMethodSignatureNoOverrideEquivalent'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassMethodSignatureNoOverrideEquivalent", checker,
        expectedErrors);
  }

  @Test
  public void TestClassNoCircularity() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNoCircularity(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding
            .error("0xA0213 a circularity exists in the hierarchy of type 'ClassNoCircularity'."),
        Finding.error("0xA0213 a circularity exists in the hierarchy of type 'A'."),
        Finding.error("0xA0213 a circularity exists in the hierarchy of type 'B'."),
        Finding.error("0xA0213 a circularity exists in the hierarchy of type 'C'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassNoCircularity", checker, expectedErrors);
  }

  @Test
  public void TestClassNoDuplicateDirectSuperInterface() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNoDuplicateDirectSuperInterface(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0214 super interface 'java.util.List' cannot be implemented more than once with different arguments in class 'ClassNoSuperInterfaceDuplicate'."),
        Finding.error(
            "0xA0214 super interface 'java.util.List' cannot be implemented more than once with different arguments in class 'InnerClass'.")

    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassNoDuplicateDirectSuperInterface", checker,
        expectedErrors);
  }

  @Test
  public void TestClassNoFinalSuperClass() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNoFinalSuperClass());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0215 class is extending a final class 'String'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassNoFinalSuperClass", checker, expectedErrors);
  }

  @Test
  public void TestClassNoModifierDuplicate() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNoModifierDuplicate(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0217 modifier 'public' is mentioned more than once in the class declaration 'ClassNoModifierDuplicate'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassNoModifierDuplicate", checker, expectedErrors);
  }

  @Test
  public void TestClassNotStatic() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassNotStatic());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0218 a non-member class is not allowed to be static in the declaration of the class 'ClassNotStatic'.")
    );
    testModelForErrors("src/test/resources", "typeSystemTestModels/invalid/classes/ClassNotStatic",
        checker, expectedErrors);
  }

  @Test
  public void TestOptionalBoundsAreInterfaces() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassOptionalBoundsAreInterfaces());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0219 bound 'String' of type-variable 'T' must be an interface.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/ClassOptionalBoundsAreInterfaces", checker,
        expectedErrors);
  }

  @Test
  public void TestGenericClassNotSubClassOfThrowable() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new GenericClassNotSubClassOfThrowable(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0220 generic class 'GenericClassNotSubClassOfThrowable' may not subclass 'java.lang.Throwable'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/GenericClassNotSubClassOfThrowable", checker,
        expectedErrors);
  }

  @Test
  public void TestNonAbstractClassImplementsAllAbstractMethods() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new NonAbstractClassImplementsAllAbstractMethods(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0221 abstract method 'getString' of super class 'ClassCanOnlyExtendClass' is not overridden in class 'ClassImplementsAllAbstractMethods'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/NonAbstractClassImplementsAllAbstractMethods",
        checker, expectedErrors);
  }

  @Test
  public void TestNoProtectedOrPrivateTopLevelClass() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new NoProtectedOrPrivateTopLevelClass());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0223 top-level class 'NoProtectedOrPrivateTopLevelClass' may not be declared 'private'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/classes/NoProtectedOrPrivateTopLevelClass", checker,
        expectedErrors);
  }


  /*
  * Test constructor CoCos
  */

  @Test
  public void TestConstructorFormalParametersDifferentName() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorFormalParametersDifferentName());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0301 constructor 'ConstructorFormalParametersDifferentName' contains multiple formal parameters with name 'n'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/constructors/ConstructorFormalParametersDifferentName",
        checker, expectedErrors);
  }

  @Test
  public void TestConstructorModifiersValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorModifiersValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0302 a constructor cannot be declared 'abstract'."),
        Finding.error(
            "0xA0303 a constructor cannot be declared 'final'."),
        Finding.error(
            "0xA0304 a constructor cannot be declared 'static'."),
        Finding.error(
            "0xA0305 a constructor cannot be declared 'native'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/constructors/ConstructorModifiersValid", checker,
        expectedErrors);
  }

  @Test
  public void TestConstructorMustNamedAsClass() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorMustNamedAsClass());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0306 the constructor is not named same as its enclosing class 'ConstructorMustNamedAsClass'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/constructors/ConstructorMustNamedAsClass", checker,
        expectedErrors);
  }

  @Test
  public void ConstructorNoAccessModifierPair() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorNoAccessModifierPair(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0307 modifiers 'public', 'protected' and private are mentioned in the same constructor declaration 'ConstructorNoAccessModifierPair'."),
        Finding.error(
            "0xA0308 modifiers 'public' and 'protected' are mentioned in the same constructor declaration 'ConstructorNoAccessModifierPair'."),
        Finding.error(
            "0xA0309 modifiers 'public' and 'private' are mentioned in the same constructor declaration 'ConstructorNoAccessModifierPair'."),
        Finding.error(
            "0xA0310 modifiers 'protected' and 'private' are mentioned in the same constructor declaration 'ConstructorNoAccessModifierPair'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/constructors/ConstructorNoAccessModifierPair", checker,
        expectedErrors);
  }

  @Test
  public void TestConstructorNoDuplicateModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorNoDuplicateModifier(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0311 modifier 'public' is mentioned more than once in the constructor declaration 'ConstructorNoDuplicateModifier'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/constructors/ConstructorNoDuplicateModifier", checker,
        expectedErrors);
  }

  @Test
  public void TestConstructorsNoDuplicateSignature() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConstructorsNoDuplicateSignature());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0312 erasure of constructor 'ConstructorsNoDuplicateSignature' is same with another constructor in class 'ConstructorsNoDuplicateSignature'."),
        Finding.error(
            "0xA0313 constructor 'ConstructorsNoDuplicateSignature' is duplicated in class 'ConstructorsNoDuplicateSignature'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/constructors/ConstructorsNoDuplicateSignature", checker,
        expectedErrors);
  }

  /*
  * Test for enum CoCos
   */

  @Test
  public void TestEnumConstructorValidModifiers() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumConstructorValidModifiers());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0401 an enum constructor must not be declared 'public' at declaration of enum 'DayFood'."),
        Finding.error(
            "0xA0402 an enum constructor must not be declared 'protected' at declaration of enum 'DayMonth'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/enums/EnumConstructorValidModifiers", checker,
        expectedErrors);
  }
  
  @Test
  public void TestEnumMayNotBeAbstract() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumMayNotBeAbstract());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("An enum may not be abstract.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/enums/EnumMayNotBeAbstract", checker, expectedErrors);
  }

  @Test
  public void TestEnumMethodModifiersValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumMethodModifiersValid());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0403 an enum must not contain abstract in enum declaration 'DayFood'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/enums/EnumMethodModifiersValid", checker, expectedErrors);
  }

  @Test
  public void TestEnumModifiersValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumModifiersValid());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding
            .error("0xA0404 an enum must not be declared 'abstract' at declaration of enum 'A'."),
        Finding.error("0xA0405 an enum must not be declared 'final' at declaration of enum 'B'.")
    );
    testModelForErrors("src/test/resources/", 
        "typeSystemTestModels/invalid/enums/EnumModifiersValid", checker, expectedErrors);
  }

  @Test
  public void TestEnumNoFinalizerMethod() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new EnumNoFinalizerMethod());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0406 an enum must not declare a 'finalize' method at declaration of enum 'DayFood'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/enums/EnumNoFinalizerMethod", checker, expectedErrors);
  }

  @Test
  public void TestMethodReturnVoid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodReturnVoid());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA1208 Invalid return type for 'test'. The void type must have dimension 0.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodReturnVoid", checker, expectedErrors);
  }

  /*
  * Test for expression CoCos
   */
  @Test
  public void TestAdditiveOpsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AdditiveOpsValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0501 types of both operands of the additive operators must be numeric types."),
        Finding.error(
            "0xA0501 types of both operands of the additive operators must be numeric types.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/AdditiveOps", checker, expectedErrors);
  }

  @Test
  public void TestArrayAccessValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ArrayAccessValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0502 an array index expression must have a type promotable to 'int'."),
        Finding.error("0xA0502 an array index expression must have a type promotable to 'int'."),
        Finding.error("0xA0502 an array index expression must have a type promotable to 'int'."),
        Finding.error("0xA0503 an array required, but 'long' found.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/ArrayAccess", checker, expectedErrors);
  }

  @Test
  public void TestArrayCreatorValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ArrayCreatorValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0504 a type of an array must be a reifiable type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/ArrayCreator", checker, expectedErrors);
  }

  @Test
  public void TestArrayDimensionByExpressionValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ArrayDimensionByExpressionValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0505 an array size must be specified by a type promotable to 'int'."),
        Finding.error("0xA0505 an array size must be specified by a type promotable to 'int'."),
        Finding.error("0xA0505 an array size must be specified by a type promotable to 'int'."),
        Finding.error("0xA0505 an array size must be specified by a type promotable to 'int'."),
        Finding.error("0xA0505 an array size must be specified by a type promotable to 'int'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/ArrayDimensionByExpression", checker,
        expectedErrors);
  }
  
  //TODO: Test for ArrayDimensionByInitializer

  @Test
  public void TestArrayInitializerValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ArrayInitializerValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0506 an array component type must be a reifiable type."),
        Finding.error("0xA0506 an array component type must be a reifiable type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/ArrayInitializer", checker, expectedErrors);
  }

  @Test
  public void TestAssignmentCompatible() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AssignmentCompatible(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0509 type '' cannot be converted to type ''."),
        Finding.error("0xA0509 type '' cannot be converted to type ''."),
        Finding.error("0xA0507 first operand of assignment expression must be a variable."),
        Finding.error("0xA0509 type '' cannot be converted to type ''.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/AssignmentCompatible", checker, expectedErrors);
  }

  @Test
  public void TestBitwiseOpsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new BinaryOrOpValid(typeResolver));
    checker.addCoCo(new BinaryAndOpValid(typeResolver));
    checker.addCoCo(new BinaryXorOpValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0511 operands of the bitwise/logical exclusive AND operator must both be of either an integral type or the type boolean."),
        Finding.error(
            "0xA0512 operands of the bitwise/logical exclusive XOR operator must both be of either an integral type or the type boolean."),
        Finding.error(
            "0xA0510 operands of the bitwise/logical exclusive OR operator must both be of either an integral type or the type boolean.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/BitwiseOps", checker, expectedErrors);
  }

  @Test
  public void TestBooleanAndOrValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new BooleanAndValid(typeResolver));
    checker.addCoCo(new BooleanOrValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0514 operands of the conditional AND operator must both be of type boolean."),
        Finding
            .error("0xA0513 operands of the conditional OR operator must both be of type boolean.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/BooleanAndOrValid", checker, expectedErrors);
  }

  @Test
  public void TestBooleanNotValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new BooleanNotValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0515 operand of the boolean NOT '!' operator must be of type boolean."),
        Finding.error(
            "0xA0516 operand of the boolean NOT '~' operator must be convertible to primitive integral type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/BooleanNot", checker, expectedErrors);
  }

  @Test
  public void TestCastConversionValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new CastConversionValid(typeResolver));
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0518 cannot cast an expression of type 'java.lang.String' to the target type 'java.lang.Integer'."),
        Finding.error(
            "0xA0518 cannot cast an expression of type 'typeSystemTestModels.invalid.expressions.AdditiveOps' to the target type 'typeSystemTestModels.invalid.expressions.ArrayInitializer'."),
        Finding.error("0xA0518 cannot cast an expression of type 'int' to the target type 'java.util.List'."),
        Finding.error("0xA0614 cannot assign a value to 'str'."),
        Finding.error("0xA0614 cannot assign a value to 'a'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/CastConversion", checker, expectedErrors);
  }

  @Test
  public void TestWarningCastConversionValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new CastConversionValid(typeResolver));
    Collection<Finding> expectedWarning = Arrays.asList(
        Finding.warning("0xA0517 possible unchecked cast conversion from 'java.lang.Object' to 'java.util.Map'."),
        Finding
            .warning("0xA0517 possible unchecked cast conversion from 'java.util.ArrayList' to 'java.util.ArrayList'.")
    );
    testModelForWarning("src/test/resources",
        "typeSystemTestModels/invalid/expressions/CastConversion", checker, expectedWarning);
  }

  @Test
  public void TestClassInnerInstanceCreationValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0524 cannot create an instance of inner enum class."),
        Finding.error("0xA0523 cannot create an instance of final inner class."),
        Finding.error("0xA0521 inner class 'A' is ambiguous."),
        Finding.error("0xA0522 inner class 'B' does not exist in class 'InnerClass'."),
        Finding.error("0xA0526 cannot create an instance of inner enum class."),
        Finding.error("0xA0525 cannot create an instance of inner abstract class.")
    );
    checker.addCoCo(new ClassInnerInstanceCreationValid(typeResolver));
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/ClassInnerInstanceCreation", checker,
        expectedErrors);
  }

  @Test
  public void TestClassInstanceCreationValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ClassInstanceCreationValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0527 a class cannot be instantiated with a wildcard type argument."),
        Finding.error("0xA0530 cannot create an instance of enum class."),
        Finding.error("0xA0531 cannot create an instance of abstract class."),
        Finding.error("0xA0529 cannot create an instance of enum class."),
        Finding.error("0xA0528 cannot create an instance of final class.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/ClassInstanceCreation", checker, expectedErrors);
  }

  @Test
  public void TestComparison() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ComparisonValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0532 each operand of a comparison operator must be of numeric type."),
        Finding.error("0xA0532 each operand of a comparison operator must be of numeric type."),
        Finding.error("0xA0532 each operand of a comparison operator must be of numeric type."),
        Finding.error("0xA0532 each operand of a comparison operator must be of numeric type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/Comparison", checker, expectedErrors);
  }

  @Test
  public void TestConditionValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ConditionValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0533 condition expression must have a type boolean."),
        Finding.error("0xA0534 true expression cannot have a type 'void'."),
        Finding.error("0xA0535 false expression cannot have a type 'void'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/Condition", checker, expectedErrors);
  }

  @Test
  public void TestFieldAccess() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldAccessValid(typeResolver));
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0537 field access to 'm' is ambiguous."),
        Finding.error("0xA0614 cannot assign a value to 'n'."),
        Finding.error("0xA0544 constant 'h' is not member of enum 'D'."),
        Finding.error("0xA0542 cannot find symbol 'aa'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/FieldAccess", checker, expectedErrors);
  }

  @Test
  public void TestIdentityTestValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new IdentityTestValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0549 operands of identity test operator have incompatible types."),
        Finding.error("0xA0549 operands of identity test operator have incompatible types.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/IdentityTest", checker, expectedErrors);
  }

  @Test
  public void TestInstanceOf() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InstanceOfValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0550 the relational expression operand of the 'instanceof' operator must be a reference type or the null type."),
        Finding.error(
            "0xA0551 the reference type mentioned after the 'instanceof' operator must denote a reference type."),
        Finding.error(
            "0xA0552 the reference type mentioned after the 'instanceof' operator must denote a reifiable type."),
        Finding.error("0xA0553 incompatible conditional operand types 'java.lang.Integer', 'java.lang.String'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/InstanceOf", checker, expectedErrors);
  }

  @Test
  public void TestMethodInvocationValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0561 the method 'addAndReturn' is not found."),
        Finding.error("0xA0565 method 'getSecondInt' is not found."),
        Finding.error("0xA0611 cannot assign a value of type 'void' to 'int'."),
        Finding.error("0xA0611 cannot assign a value of type '&' to 'java.lang.String'.")
    );
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    checker.addCoCo(new MethodInvocationValid(typeResolver));
    checker.addCoCo(new MethodGenericInvocationValid(typeResolver));
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/MethodInvocation", checker, expectedErrors);
  }

  @Test
  public void TestMultiplicativeOpsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MultiplicativeOpsValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding
            .error("0xA0571 types of both operands of the multiplicative operators must be numeric types."),
        Finding
            .error("0xA0571 types of both operands of the multiplicative operators must be numeric types.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/MultiplicativeOps", checker, expectedErrors);
  }

  @Test
  public void TestPrefixOpValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new PrefixOpValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0572 the operand expression of prefix operator must have type convertible to numeric type."),
        Finding.error(
            "0xA0572 the operand expression of prefix operator must have type convertible to numeric type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/PrefixOp", checker, expectedErrors);
  }

  @Test
  public void TestPrimarySuperValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new PrimarySuperValid());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0575 keyword 'super' is not allowed in interface."),
        Finding.error("0xA0576 keyword 'super' is not allowed in class 'Object'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/PrimarySuper", checker, expectedErrors);
  }
  
  @Test
  public void TestPrimaryThisValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new PrimaryThisValid());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0577 keyword 'this' is not allowed in interface.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/PrimaryThis", checker, expectedErrors);
  }

  @Test
  public void TestShiftOpValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ShiftOpValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0578 operands of shift operator must have Integral type."),
        Finding.error("0xA0578 operands of shift operator must have Integral type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/ShiftOp", checker, expectedErrors);
  }

  @Test
  public void TestSuffixOpValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new SuffixOpValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0579 the operand expression of suffix operator must have type convertible to numeric type."),
        Finding.error(
            "0xA0579 the operand expression of suffix operator must have type convertible to numeric type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/expressions/SuffixOp", checker, expectedErrors);
  }

  /*
  * Fields and local variables
   */

  @Test
  public void TestErrorFieldInitializerAssignmentCompatible() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0601 type mismatch, cannot convert from 'java.util.List[]' to 'java.util.List'."),
        Finding.error("0xA0613 Array component must be a reifiable type."),
        Finding.error("0xA0603 cannot assign a value of type 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture'."),
        Finding.error("0xA0603 cannot assign a value of type 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture'."),
        Finding.error("0xA0603 cannot assign a value of type 'java.util.ArrayList' to 'java.util.List'."),
        Finding.error("0xA0612  type mismatch, cannot convert from 'int' to 'int[]'."),
        Finding.error("0xA0612  type mismatch, cannot convert from 'int' to 'int[]'."),
        Finding.error("0xA0601 type mismatch, cannot convert from 'int[]' to 'int'."),
        Finding.error("0xA0601 type mismatch, cannot convert from 'int[]' to 'int[][]'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/fieldsAndLocalVars/FieldInitializerAssignmentCompatible",
        checker, expectedErrors);
  }

  @Test
  public void TestWarningFieldInitializerAssignmentCompatible() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldInitializerAssignmentCompatible(typeResolver));
    Collection<Finding> expectedWarning = Arrays.asList(
        Finding.warning("0xA0602 Possible unchecked conversion from type 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture'."),
        Finding.warning(
            "0xA0602 Possible unchecked conversion from type 'typeSystemTestModels.invalid.fieldsAndLocalVars.CaptureExample' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.CaptureExample'."),
        Finding.warning("0xA0602 Possible unchecked conversion from type 'typeSystemTestModels.invalid.fieldsAndLocalVars.Box' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.Box'."),
        Finding.warning("0xA0602 Possible unchecked conversion from type 'java.util.ArrayList' to 'java.util.List'."),
        Finding.warning("0xA0602 Possible unchecked conversion from type 'java.util.HashMap' to 'java.util.Map'.")
    );
    testModelForWarning("src/test/resources",
        "typeSystemTestModels/invalid/fieldsAndLocalVars/FieldInitializerAssignmentCompatible",
        checker, expectedWarning);
  }

  @Test
  public void TestFieldModifierAccessCombinations() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldModifierAccessCombinations(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0604 The variable cannot be public, private and protected."),
        Finding.error("0xA0605 The variable cannot be public and private."),
        Finding.error("0xA0606 The variable cannot be public and protected."),
        Finding.error("0xA0607 The variable cannot be private and protected.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/fieldsAndLocalVars/FieldModifierAccessCombinations", checker,
        expectedErrors);
  }

  @Test
  public void TestFieldNoDuplicateModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new FieldNoDuplicateModifier(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0608 The modifier 'public' is mentioned more than once in the field declaration."),
        Finding.error(
            "0xA0608 The modifier 'private' is mentioned more than once in the field declaration."),
        Finding.error(
            "0xA0608 The modifier 'protected' is mentioned more than once in the field declaration.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/fieldsAndLocalVars/FieldModifiersNoDuplication", checker,
        expectedErrors);
  }

  @Test
  public void TestErrorLocalVariableInitializerAssignmentCompatible() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0611 cannot assign a value of type 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture'."),
        Finding.error("0xA0611 cannot assign a value of type 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture'."),
        Finding.error("0xA0611 cannot assign a value of type 'java.util.ArrayList' to 'java.util.List'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/fieldsAndLocalVars/LocalVariableInitializerAssignmentCompatible",
        checker, expectedErrors);
  }

  @Test
  public void TestWarningLocalVariableInitializerAssignmentCompatible() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new LocalVariableInitializerAssignmentCompatible(typeResolver));
    Collection<Finding> expectedWarning = Arrays.asList(
        Finding.warning("0xA0610 Possible unchecked conversion from type 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.Capture'."),
        Finding.warning(
            "0xA0610 Possible unchecked conversion from type 'typeSystemTestModels.invalid.fieldsAndLocalVars.CaptureExample' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.CaptureExample'."),
        Finding.warning("0xA0610 Possible unchecked conversion from type 'typeSystemTestModels.invalid.fieldsAndLocalVars.Box' to 'typeSystemTestModels.invalid.fieldsAndLocalVars.Box'."),
        Finding.warning("0xA0610 Possible unchecked conversion from type 'java.util.ArrayList' to 'java.util.List'."),
        Finding.warning("0xA0610 Possible unchecked conversion from type 'java.util.HashMap' to 'java.util.Map'.")
    );
    testModelForWarning("src/test/resources",
        "typeSystemTestModels/invalid/fieldsAndLocalVars/LocalVariableInitializerAssignmentCompatible",
        checker, expectedWarning);
  }

  /*
   * Interfaces
   */

  @Test
  public void TestInterfaceBoundErasuresAreDifferent() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceBoundErasuresAreDifferent(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0701 bounds of type-variable 'T' must not have the same erasure.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceBoundErasuresAreDifferent", checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceCannotDependSelf() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceCannotDependSelf());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0702 interface 'InterfaceCannotDependSelf' must not extend itself.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceCannotDependSelf", checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceCannotExtendClasses() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceCannotExtendClasses());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding
            .error("0xA0703 interface 'InterfaceCannotExtendClasses' must extend only interface.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceCannotExtendClasses", checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceInnerTypeNotNamedAsEnclosing() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceInnerTypeNotNamedAsEnclosing());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0704 inner interface 'InterfaceInnerTypeNotNamedAsEnclosing' must not be named same as enclosing interface."),
        Finding.error("0xA0705 inner class 'A' must not be named same as enclosing interface.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceInnerTypeNotNamedAsEnclosing", checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceMethodSignatureNoOverrideEquivalent() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceMethodSignatureNoOverrideEquivalent());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0707 method 'getName' is duplicated in interface 'InterfaceMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0707 method 'getName' is duplicated in interface 'InterfaceMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0707 method 'setName' is duplicated in interface 'InterfaceMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0706 erasure of method 'setList' is same with another method in interface 'InterfaceMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0707 method 'get' is duplicated in interface 'InterfaceMethodSignatureNoOverrideEquivalent'."),
        Finding.error(
            "0xA0706 erasure of method 'setNaming' is same with another method in interface 'InterfaceMethodSignatureNoOverrideEquivalent'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceMethodSignatureNoOverrideEquivalent",
        checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceMethodsNotAllowedModifiers() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceMethodsNotAllowedModifiers());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0708 method 'getDouble' is declared strictfp in interface 'InterfaceMethodsNotAllowedModifiers'."),
        Finding.error(
            "0xA0709 method 'getString' is declared native in interface 'InterfaceMethodsNotAllowedModifiers'."),
        Finding.error(
            "0xA0710 method 'getInteger' is declared synchronized in interface 'InterfaceMethodsNotAllowedModifiers'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceMethodsNotAllowedModifiers", checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceNoDuplicateModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceNoDuplicateModifier(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0711 modifier 'public' is declared more than once in interface 'InterfaceNoDuplicateModifier'."),
        Finding.error("0xA0711 modifier 'private' is declared more than once in interface 'A'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceNoDuplicateModifier", checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceNoFinalMethod() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceNoFinalMethod());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0712 method 'getString' is declared final in interface 'InterfaceNoFinalMethod'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceNoFinalMethod", checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceNoStaticMethod() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceNoStaticMethod());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0713 method 'getString' is declared static in interface 'InterfaceNoStaticMethod'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceNoStaticMethod", checker,
        expectedErrors);
  }

  @Test
  public void TestInterfaceOptionalBoundsAreInterfaces() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new InterfaceOptionalBoundsAreInterfaces());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0714 bound 'ArrayList' of type-variable 'T' must be an interface.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/interfaces/InterfaceOptionalBoundsAreInterfaces", checker,
        expectedErrors);
  }

  /*
   * Methods
   */

  @Test
  public void TestAbstractMethodDefinition() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AbstractMethodDefinition());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0801 abstract method 'getString' must be defined by an abstract class.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/AbstractMethodDefinition", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodAbstractAndOtherModifiers() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodAbstractAndOtherModifiers());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0802 abstract method must not be declared 'private'."),
        Finding.error("0xA0803 abstract method must not be declared 'static'."),
        Finding.error("0xA0804 abstract method must not be declared 'final'."),
        Finding.error("0xA0805 abstract method must not be declared 'native'."),
        Finding.error("0xA0806 abstract method must not be declared 'strictfp'."),
        Finding.error("0xA0807 abstract method must not be declared 'synchronized'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodAbstractAndOtherModifiers", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodBodyAbsenceAndPresence() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodBodyAbsenceAndPresence());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0810 method 'getString' must specify a body."),
        Finding.error("0xA0808 abstract method 'getString1' must not specify a body."),
        Finding.error("0xA0809 native method 'getString2' must not specify a body.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodBodyAbsenceAndPresence", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodExceptionThrows() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodExceptionThrows(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0811 no exception of type 'List' can be thrown. An exception must be a subtype of Throwable.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodExceptionThrows", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodFormalParametersDifferentName() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodFormalParametersDifferentName());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0812 formal parameter 'name' is already declared in method 'setName'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodFormalParametersDifferentName", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodHiding() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodHiding());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0813 class method 'getString' is hiding an instance method.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodHiding", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodNoAccessPairModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodNoAccessPairModifier(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error(
            "0xA0814 modifiers 'public', 'protected' and 'private' are mentioned in method 'setName'."),
        Finding
            .error("0xA0816 modifiers 'public' and 'private' are mentioned in method 'setName1'."),
        Finding.error(
            "0xA0815 modifiers 'public' and 'protected' are mentioned in method 'setName2'."),
        Finding.error(
            "0xA0817 modifiers 'private' and 'protected' are mentioned in method 'setName3'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodNoAccessPairModifier", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodNoDuplicateModifier() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodNoDuplicateModifier(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding
            .error("0xA0818 modifier 'strictfp' is declared more than once in method 'setDouble'."),
        Finding.error(
            "0xA0818 modifier 'synchronized' is declared more than once in method 'setDouble'."),
        Finding
            .error("0xA0818 modifier 'private' is declared more than once in method 'setDouble'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodNoDuplicateModifier", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodNoNativeAndStrictfp() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodNoNativeAndStrictfp());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0819 method 'getName' must not be both 'native' and 'strictfp'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodNoNativeAndStrictfp", checker,
        expectedErrors);
  }

  @Test
  public void TestMethodOverride() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodOverride());
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0820 the return type is not compatible with 'Methods.getList'."),
        Finding.error("0xA0820 the return type is not compatible with 'ArrayList.size'."),
        Finding.error("0xA0820 the return type is not compatible with 'ArrayList.size'."),
        Finding.error("0xA0820 the return type is not compatible with 'AbstractCollection.size'."),
        Finding.error("0xA0820 the return type is not compatible with 'AbstractCollection.size'."),
        Finding.error("0xA0820 the return type is not compatible with 'Collection.size'."),
        Finding.error("0xA0820 the return type is not compatible with 'Collection.size'."),
        Finding.error("0xA0820 the return type is not compatible with 'Collection.size'."),
        Finding.error("0xA0822 the visibility (public) of inherited method 'setName' is reduced."),
        Finding
            .error("0xA0823 the visibility (protected) of inherited method 'setName1' is reduced."),
        Finding.error(
            "0xA0825 the method 'id of type 'MethodOverride' has the same erasure as the method 'id' of type 'Methods'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodOverride", checker,
        expectedErrors);
  }

  @Test
  public void TestWarningMethodOverride() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new MethodOverride());
    Collection<Finding> expectedWarnings = Arrays.asList(
        Finding.warning(
            "0xA0821 the return type 'List' for 'toList' from the type 'MethodOverride' needs unchecked conversion to conform to 'toList' in type 'Methods'.")
    );
    testModelForWarning("src/test/resources",
        "typeSystemTestModels/invalid/methods/MethodOverride", checker,
        expectedWarnings);
  }
  
  /*
   * name cocos
   */

    @Test
    public void TestClassValidSuperTypes() {
      JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
      checker.addCoCo(new ClassValidSuperTypes(typeResolver));
      Collection<Finding> expectedErrors = Arrays.asList(
          Finding.error("0xA0705 incorrect number of type arguments for the type 'java.util.ArrayList'."),
          Finding.error("0xA0708 the interface 'java.io.Serializable' is not generic.")
      );
      testModelForErrors("src/test/resources",
          "typeSystemTestModels/invalid/names/ClassValidSuperTypes", checker,
          expectedErrors);
    }

    @Test
    public void TestInterfaceValidSuperTypes() {
      JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
      checker.addCoCo(new InterfaceValidSuperTypes(typeResolver));
      Collection<Finding> expectedErrors = Arrays.asList(
          Finding.error("0xA0712 incorrect number of type arguments for the type 'java.util.List'.")
      );
      testModelForErrors("src/test/resources",
          "typeSystemTestModels/invalid/names/InterfaceValidSuperTypes", checker,
          expectedErrors);
    }

  /*
   * Statements
   */

  @Test
  public void TestAssertIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new AssertIsValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0901 assert-statement expression must have a boolean type."),
        Finding.error("0xA0902 assert-statement message cannot be of type void.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/AssertIsValid", checker,
        expectedErrors);
  }

  @Test
  public void TestCatchIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new CatchIsValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0903 the type of catch clause in try-statement must be either Throwable or subtype of it.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/CatchIsValid", checker,
        expectedErrors);
  }

  @Test
  public void TestDoWhileConditionHasBooleanType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new DoWhileConditionHasBooleanType(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0905 Condition in do-statement must be a boolean expression.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/DoWhileConditionHasBooleanType", checker,
        expectedErrors);
  }

  @Test
  public void TestForConditionHasBooleanType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ForConditionHasBooleanType(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0906 condition of for-loop must be a boolean expression.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/ForConditionHasBooleanType", checker,
        expectedErrors);
  }

  @Test
  public void TestForEachIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ForEachIsValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0907 expression of the for-each statement must be an array or Iterable."),
        Finding.error("0xA0908 variable in the for-each statement is not compatible with the expression, got : java.lang.Double, expected : int."),
        Finding.error("0xA0908 variable in the for-each statement is not compatible with the expression, got : java.lang.Integer, expected : java.lang.String.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/ForEachIsValid", checker,
        expectedErrors);
  }

  @Test
  public void TestIfConditionHasBooleanType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new IfConditionHasBooleanType(typeResolver));
    checker.addCoCo(new IdentityTestValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0549 operands of identity test operator have incompatible types."),
        Finding.error("0xA0909 condition in if-statement must be a boolean expression.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/IfConditionHasBooleanType", checker,
        expectedErrors);
  }
  
  @Test
  public void TestResourceInTryStatementCloseable() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ResourceInTryStatementCloseable(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0920 resource in try-statement must be closeable."),
        Finding.error("0xA0920 resource in try-statement must be closeable.")
    );
    testModelForErrors("src/test/resources", 
        "typeSystemTestModels/invalid/statements/ResourceInTryStatementCloseable", checker,
        expectedErrors);
  }

  @Test
  public void TestReturnTypeAssignmentIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ReturnTypeAssignmentIsValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0910 unexpected return-statement with expression for method with void type."),
        Finding.error("0xA0911 return-statement expected for method 'test1'."),
        Finding.error("0xA0912 expression is missing in return-statement."),
        Finding.error("0xA0913 int type of method cannot have return type java.lang.String.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/ReturnTypeAssignmentIsValid", checker,
        expectedErrors);
  }

  @Test
  public void TestSwitchStatementValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new SwitchStatementValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0917 switch expression in the switch-statement must be char, byte, short, int, Character,\n"
            + "Byte, Short, Integer, or an enum type."),
        Finding.error("0xA0916 type of the case-label is incompatible with the type of the switch-statement."),
        Finding.error("0xA0915 switch-statement must have at most one default label."),
        Finding.error("0xA0916 type of the case-label is incompatible with the type of the switch-statement.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/SwitchStatementValid", checker,
        expectedErrors);
  }

  @Test
  public void TestSynchronizedArgIsReftype() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new SynchronizedArgIsReftype(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0918 expression in synchronized-statement must have a reference type."),
        Finding.error("0xA0918 expression in synchronized-statement must have a reference type.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/SynchronizedArgIsReftype", checker,
        expectedErrors);
  }

  @Test
  public void TestThrowIsValid() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new ThrowIsValid(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0918 exception in throw-statement must be Throwable or subtype of it.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/ThrowIsValid", checker,
        expectedErrors);
  }

  @Test
  public void TestWhileConditionHasBooleanType() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new WhileConditionHasBooleanType(typeResolver));
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0919 condition in while-statement must be boolean expression.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/invalid/statements/WhileConditionHasBooleanType", checker,
        expectedErrors);
  }

  //TODO: Test for FieldNamesMustBePairWiseDifferent & NestedTypeMayNotHaveSameNameAsEnclosingType
  
}
