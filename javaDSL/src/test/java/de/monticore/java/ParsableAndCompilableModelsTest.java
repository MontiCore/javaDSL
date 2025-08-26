/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static de.monticore.java.JavaDSLAssertions.*;

public class ParsableAndCompilableModelsTest extends AbstractTest {
  
  @ParameterizedTest
  @ValueSource(strings = {
      // Parser bugs
      "src/test/resources/parsableAndCompilableModels/parserBugs/ExplicitGenericInvocations.java",
      "src/test/resources/parsableAndCompilableModels/parserBugs/InterfaceWithStaticMethod.java",
      // Simple test classes
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleAnnotationTestModel.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleClassTestModel.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleEnumTestModels.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleInterfaceTestModel.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/EmptyClass.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/HelloWorld.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/VarVariables.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/OneFieldClass.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/QualifiedNameTestClass.java",
      // Stressful package
      "src/test/resources/parsableAndCompilableModels/stressfulPackage/StressfulSyntax.java",
      // Symbol table
      "src/test/resources/parsableAndCompilableModels/symbolTable/enums/EnumViaJavaEnum.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/enums/EnumViaJavaInterface.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/resolve/GeneralResolveTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/resolve/TypeVariableShadowingTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/typeArgumentsAndParameters/TypeArgumentTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/typeArgumentsAndParameters/TypeParameterTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/ScopesSymbolTableTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/VariablesTestClass.java"
  })
  public void testParsableAndCompilableModels(String path) {
    assertParsingSuccess(path);
  }
}
