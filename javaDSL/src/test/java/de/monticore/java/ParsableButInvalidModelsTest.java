/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.IOException;

import de.se_rwth.commons.logging.LogStub;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.java.cocos.FieldNamesMustBePairWiseDifferent;
import de.monticore.java.cocos.NestedTypeMayNotHaveSameNameAsEnclosingType;
import de.monticore.java.cocos.classes.AbstractMethodMayNotBePrivate;
import de.monticore.java.cocos.classes.AbstractMethodMayNotBeStatic;
import de.monticore.java.cocos.classes.AbstractOrNativeMethodHasNoBody;
import de.monticore.java.cocos.classes.ConcreteClassMayNotHaveAbstractMethod;
import de.monticore.java.cocos.classes.ConstructorMayNotBeAbstract;
import de.monticore.java.cocos.classes.NotAbstractAndNotFinal;
import de.monticore.java.cocos.classes.SuperClassMayNotBeFinal;
import de.monticore.java.cocos.methods.MethodFormalParametersVarargsNoArray;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.se_rwth.commons.logging.Log;

/**
 * A simple test for java tool.
 *
 */
public class ParsableButInvalidModelsTest extends AbstractTestClass {

  JavaDSLCoCoChecker javaCoCoChecker;

  {
    javaCoCoChecker = new JavaDSLCoCoChecker();
    // classes cocos
    javaCoCoChecker.addCoCo(new AbstractMethodMayNotBePrivate());
    javaCoCoChecker.addCoCo(new AbstractMethodMayNotBeStatic());
    javaCoCoChecker.addCoCo(new AbstractOrNativeMethodHasNoBody());
    javaCoCoChecker.addCoCo(new ConcreteClassMayNotHaveAbstractMethod());
    javaCoCoChecker.addCoCo(new ConstructorMayNotBeAbstract());
    javaCoCoChecker.addCoCo(new NotAbstractAndNotFinal());
    javaCoCoChecker.addCoCo(new SuperClassMayNotBeFinal());
    // types cocos
    javaCoCoChecker.addCoCo(new FieldNamesMustBePairWiseDifferent());
    javaCoCoChecker.addCoCo(new NestedTypeMayNotHaveSameNameAsEnclosingType());
    // methods cocos
    javaCoCoChecker.addCoCo(new MethodFormalParametersVarargsNoArray());
  }

  @BeforeClass
  public static void init() {
    LogStub.init();
    LogStub.enableFailQuick(false);
  }

  @Before
  public void setUp() throws RecognitionException, IOException {
    LogStub.getFindings().clear();
  }

  @Test
  public void test_classes_AbstractMethodIsNeverPrivateTestClass()
      throws RecognitionException,
      IOException {
    testCoco("src/test/resources",
        "parsableButInvalidModels/classes/AbstractMethodMayNotBePrivateTestClass", javaCoCoChecker,
        AbstractMethodMayNotBePrivate.ERROR_MESSAGE);
  }

  @Test
  public void test_classes_AbstractMethodMayNotBeStaticTestClass()
      throws RecognitionException,
      IOException {
    testCoco("src/test/resources",
        "parsableButInvalidModels/classes/AbstractMethodMayNotBeStaticTestClass", javaCoCoChecker,
        AbstractMethodMayNotBeStatic.ERROR_MESSAGE);
  }

  @Test
  public void test_classes_AbstractMethodHaveNoBodyTestClass() throws RecognitionException,
      IOException {
    testCoco("src/test/resources",
        "parsableButInvalidModels/classes/AbstractOrNativeMethodHasNoBodyTestClass", javaCoCoChecker,
        AbstractOrNativeMethodHasNoBody.ERROR_MESSAGE);
  }

  @Test
  public void test_classes_ConcreteClassMayNotHaveAbstractMethodTestClass()
      throws RecognitionException,
      IOException {
    testCoco("src/test/resources",
        "parsableButInvalidModels/classes/ConcreteClassMayNotHaveAbstractMethodTestClass", javaCoCoChecker,
        ConcreteClassMayNotHaveAbstractMethod.ERROR_MESSAGE);
  }

  @Test
  public void test_classes_ConstructorMayNotBeAbstractTestClass()
      throws RecognitionException,
      IOException {
    testCoco("src/test/resources",
        "parsableButInvalidModels/classes/ConstructorMayNotBeAbstractTestClass", javaCoCoChecker,
        ConstructorMayNotBeAbstract.ERROR_MESSAGE);
  }

  @Test
  public void test_classes_NotAbstractAndNotFinalTestClass() throws RecognitionException,
      IOException {
    testCoco("src/test/resources",
        "parsableButInvalidModels/classes/NotAbstractAndNotFinalTestClass", javaCoCoChecker,
        NotAbstractAndNotFinal.ERROR_MESSAGE, NotAbstractAndNotFinal.ERROR_MESSAGE);
  }

  @Test
  public void test_classes_SuperClassMayNotBeFinalTestClass() throws RecognitionException,
      IOException {
    testCoco("src/test/resources",
        "parsableButInvalidModels/classes/SuperClassMayNotBeFinalTestClass", javaCoCoChecker,
        SuperClassMayNotBeFinal.ERROR_MESSAGE);
  }

  @Test
  public void test_types_FieldNamesMustBePairWiseDifferentTestClass()
      throws RecognitionException, IOException {
    // There are total of 4 cases: class, interface, enum, annotation
    String[] errorMessages = new String[4];
    for (int i = 0; i < 4; i++) {
      errorMessages[i] = FieldNamesMustBePairWiseDifferent.ERROR_MESSAGE;
    }
    testCoco("src/test/resources",
        "parsableButInvalidModels/types/FieldNamesMustBePairWiseDifferentTestClass", javaCoCoChecker,
        errorMessages);
  }

  @Test
  public void test_types_NestedTypeMayNotHaveSameNameAsEnclosingTypeTestClass()
      throws RecognitionException, IOException {
    // There are total of 4x4=16 cases for nesting: class, interface, enum,
    // annotation
    String[] errorMessages = new String[16];
    for (int i = 0; i < 16; i++) {
      errorMessages[i] = NestedTypeMayNotHaveSameNameAsEnclosingType.ERROR_MESSAGE;
    }
    testCoco("src/test/resources",
        "parsableButInvalidModels/types/NestedTypeMayNotHaveSameNameAsEnclosingTypeTestClass", javaCoCoChecker,
        errorMessages);
  }
  
  @Test
  public void test_methods_MethodFormalParametersVarargsNoArray() throws RecognitionException, IOException {
    String[] errorMessages = new String[2];
    errorMessages[0] = String.format(MethodFormalParametersVarargsNoArray.ERROR_MESSAGE, "y");
    errorMessages[1] = String.format(MethodFormalParametersVarargsNoArray.ERROR_MESSAGE, "strings");
    testCoco("src/test/resources", 
        "parsableButInvalidModels/VarargsWithTrailingArrayBrackets", javaCoCoChecker, 
        errorMessages);
  }
}
