/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.runtime.junit.TestWithMCLanguage;

import java.nio.file.Path;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;

@TestWithMCLanguage(JavaDSLMill.class)
public class ParsableAndCompilableModelsTest {
  
  @JavaSourceTest(basePath = "src/test/resources/parsableAndCompilableModels", files = {
      "parserBugs/ExplicitGenericInvocations.java",
      "parserBugs/InterfaceWithStaticMethod.java",
      "simpleTestClasses/types/SimpleAnnotationTestModel.java",
      "simpleTestClasses/types/SimpleClassTestModel.java",
      "simpleTestClasses/types/SimpleEnumTestModels.java",
      "simpleTestClasses/types/SimpleInterfaceTestModel.java",
      "simpleTestClasses/EmptyClass.java",
      "simpleTestClasses/HelloWorld.java",
      "simpleTestClasses/VarVariables.java",
      "simpleTestClasses/OneFieldClass.java",
      "simpleTestClasses/QualifiedNameTestClass.java",
      "stressfulPackage/StressfulSyntax.java",
      "symbolTable/enums/EnumViaJavaEnum.java",
      "symbolTable/enums/EnumViaJavaInterface.java",
      "symbolTable/resolve/GeneralResolveTestClass.java",
      "symbolTable/resolve/TypeVariableShadowingTestClass.java",
      "symbolTable/typeArgumentsAndParameters/TypeArgumentTestClass.java",
      "symbolTable/typeArgumentsAndParameters/TypeParameterTestClass.java",
      "symbolTable/ScopesSymbolTableTestClass.java",
      "symbolTable/VariablesTestClass.java"
  })
  public void testParsableAndCompilableModels(Path path) {
    assertParsingSuccess(path);
  }
}
