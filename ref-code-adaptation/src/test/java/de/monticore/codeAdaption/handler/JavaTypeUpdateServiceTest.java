package de.monticore.codeAdaption.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

  @Test
  void acceptsExactAndCovariantImplementationReturns() throws IOException {
    CDModelIndex index =
        index(
            "Returns.cd",
            "classdiagram Returns { class Parent; class Child extends Parent; }");

    assertTrue(JavaTypeUpdateService.isCompatibleImplementationReturn(index, "int", "int"));
    assertTrue(
        JavaTypeUpdateService.isCompatibleImplementationReturn(index, "Child", "Parent"));
    assertTrue(
        JavaTypeUpdateService.isCompatibleImplementationReturn(index, "String", "Object"));
    assertTrue(
        JavaTypeUpdateService.isCompatibleImplementationReturn(index, "Child[]", "Object"));
  }

  @Test
  void rejectsReversedUnrelatedAndPrimitiveReturnCompatibility() throws IOException {
    CDModelIndex index =
        index(
            "Returns.cd",
            "classdiagram Returns { class Parent; class Child extends Parent; class Other; }");

    assertFalse(
        JavaTypeUpdateService.isCompatibleImplementationReturn(index, "Parent", "Child"));
    assertFalse(
        JavaTypeUpdateService.isCompatibleImplementationReturn(index, "Other", "Parent"));
    assertFalse(JavaTypeUpdateService.isCompatibleImplementationReturn(index, "int", "long"));
    assertFalse(JavaTypeUpdateService.isCompatibleImplementationReturn(index, "void", "Object"));
    assertFalse(JavaTypeUpdateService.isCompatibleImplementationReturn(index, "Object", "void"));
  }

  private CDModelIndex index(String fileName, String source) throws IOException {
    Path cd = temporaryDirectory.resolve(fileName);
    Files.writeString(cd, source);
    return CDModelIndex.of(JavaLoader.parseCD(cd.toString()));
  }
}
