package de.monticore.codeAdaption.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavaTypeUpdateServiceTest {
  @TempDir Path temporaryDirectory;

  @BeforeEach
  void initializeMills() {
    AdapterAbstractTest.initMills();
  }

  @Test
  void findsInterfacesIntroducedOnACompletedSuperclass() throws IOException {
    CDModelIndex input =
        index(
            "Input.cd",
            "classdiagram Input { class BankAccount; "
                + "class SavingsAccount extends BankAccount; }");
    CDModelIndex completed =
        index(
            "Completed.cd",
            "import java.lang.String; classdiagram Completed { "
                + "interface Auditable { String generateReport(); } "
                + "abstract class BankAccount implements Auditable; "
                + "class SavingsAccount extends BankAccount; }");

    Set<String> inputInterfaces =
        JavaTypeUpdateService.effectiveInterfaceNames(input, "SavingsAccount");
    Set<String> completedInterfaces =
        JavaTypeUpdateService.effectiveInterfaceNames(completed, "SavingsAccount");
    completedInterfaces.removeAll(inputInterfaces);

    assertEquals(Set.of("Auditable"), completedInterfaces);
  }

  @ParameterizedTest(name = "{0} implements {1}: {2}")
  @MethodSource("implementationReturnCases")
  void checksImplementationReturnCompatibility(
      String actualReturn, String requiredReturn, boolean compatible) throws IOException {
    CDModelIndex index =
        index(
            "Returns.cd",
            "classdiagram Returns { class Parent; class Child extends Parent; class Other; }");

    assertEquals(
        compatible,
        JavaTypeUpdateService.isCompatibleImplementationReturn(
            index, actualReturn, requiredReturn));
  }

  private static Stream<Arguments> implementationReturnCases() {
    return Stream.of(
        Arguments.of("int", "int", true),
        Arguments.of("Child", "Parent", true),
        Arguments.of("String", "Object", true),
        Arguments.of("Child[]", "Object", true),
        Arguments.of("Parent", "Child", false),
        Arguments.of("Other", "Parent", false),
        Arguments.of("int", "long", false),
        Arguments.of("void", "Object", false),
        Arguments.of("Object", "void", false));
  }

  private CDModelIndex index(String fileName, String source) throws IOException {
    Path cd = temporaryDirectory.resolve(fileName);
    Files.writeString(cd, source);
    return CDModelIndex.of(JavaLoader.parseCD(cd.toString()));
  }
}
