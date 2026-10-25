package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TopCodeComposerTest extends AdapterAbstractTest {

  @TempDir Path temporaryDirectory;

  private TopCodeComposer composer;

  @BeforeEach
  void setUp() {
    initMills();
    composer = new TopCodeComposer(new AdaptedCodeMerger());
  }

  @Test
  void emitsAdaptedTypeDirectlyWhenConcreteHwcIsMissing() throws IOException {
    ASTOrdinaryCompilationUnit adapted =
        javaSource("adapted/Account.java", "package target; class Account { void adapted() {} }");

    Set<ASTOrdinaryCompilationUnit> result =
        composer.compose(Set.of(), linkedSet(adapted), concreteIndex("class Account;"));

    assertEquals(Set.of("Account"), typeNames(result));
    assertTrue(JavaLoader.print(result.iterator().next()).contains("void adapted()"));
  }

  @Test
  void createsTopCompanionWithoutMergingMembersOrRewritingPublicTypeReferences()
      throws IOException {
    ASTOrdinaryCompilationUnit concrete =
        javaSource(
            "concrete/Account.java",
            "package target; class Account { int handwritten; void own() {} }");
    ASTOrdinaryCompilationUnit adapted =
        javaSource(
            "adapted/Account.java",
            "package target; class Account { Account() {} Account copy(Account value) { return new Account(); } int adapted; }");

    Set<ASTOrdinaryCompilationUnit> result =
        composer.compose(
            linkedSet(concrete), linkedSet(adapted), concreteIndex("class Account;"));

    String handwritten = JavaLoader.print(unitNamed(result, "Account"));
    String top = JavaLoader.print(unitNamed(result, "AccountTOP"));
    assertTrue(handwritten.contains("class Account extends AccountTOP"));
    assertTrue(handwritten.contains("int handwritten"));
    assertFalse(handwritten.contains("int adapted"));
    assertTrue(top.contains("class AccountTOP"));
    assertTrue(top.contains("AccountTOP()"));
    assertTrue(top.contains("Account copy(Account value)"));
    assertTrue(top.contains("new Account()"));
  }

  @Test
  void keepsExistingReferenceTopWhenNoConcreteHwcExists() throws IOException {
    ASTOrdinaryCompilationUnit adapted =
        javaSource(
            "adapted/Account.java", "package target; class Account extends AccountTOP {}");

    Set<ASTOrdinaryCompilationUnit> result =
        composer.compose(Set.of(), linkedSet(adapted), concreteIndex("class Account;"));

    assertEquals(Set.of("Account"), typeNames(result));
    assertTrue(JavaLoader.print(result.iterator().next()).contains("extends AccountTOP"));
  }

  @Test
  void shiftsReferenceTopToTopTopWhenConcreteHwcOwnsThePublicType() throws IOException {
    ASTOrdinaryCompilationUnit concrete =
        javaSource(
            "concrete/Account.java",
            "package target; class Account extends AccountTOP { void own() {} }");
    ASTOrdinaryCompilationUnit adapted =
        javaSource(
            "adapted/Account.java",
            "package target; class Account extends AccountTOP { void adapted() {} }");

    Set<ASTOrdinaryCompilationUnit> result =
        composer.compose(
            linkedSet(concrete), linkedSet(adapted), concreteIndex("class Account;"));

    String handwritten = JavaLoader.print(unitNamed(result, "Account"));
    String top = JavaLoader.print(unitNamed(result, "AccountTOP"));
    assertTrue(handwritten.contains("class Account extends AccountTOP"));
    assertTrue(top.contains("class AccountTOP extends AccountTOPTOP"));
  }

  @Test
  void preservesUnrelatedQualifiedTopSuperclassWhenCreatingTopCompanion() throws IOException {
    ASTOrdinaryCompilationUnit concrete =
        javaSource("concrete/Account.java", "package target; class Account {}");
    ASTOrdinaryCompilationUnit adapted =
        javaSource(
            "adapted/Account.java",
            "package target; class Account extends external.AccountTOP {}");

    Set<ASTOrdinaryCompilationUnit> result =
        composer.compose(
            linkedSet(concrete), linkedSet(adapted), concreteIndex("class Account;"));

    String top = JavaLoader.print(unitNamed(result, "AccountTOP"));
    assertTrue(top.contains("class AccountTOP extends external.AccountTOP"), top);
    assertFalse(top.contains("extends AccountTOPTOP"), top);
  }

  @Test
  void preservesFluentPublicSelfTypeInGeneratedTopCode() throws IOException {
    ASTOrdinaryCompilationUnit concrete =
        javaSource(
            "concrete/PersonBuilder.java",
            "package target; class PersonBuilder extends PersonBuilderTOP { void validate() {} }");
    ASTOrdinaryCompilationUnit adapted =
        javaSource(
            "adapted/PersonBuilder.java",
            "package target; class PersonBuilder {"
                + " private String name;"
                + " PersonBuilder setName(String name) { this.name = name; return this; }"
                + " void decorate(Support support) { support.decorate(this); }"
                + " }");

    TopCodeComposer.CompositionResult result =
        composer.composeWithSelfTypeBindings(
            linkedSet(concrete),
            linkedSet(adapted),
            concreteIndex("class PersonBuilder;"));
    Path output = temporaryDirectory.resolve("fluent-output");
    JavaLoader.printAST(result.code(), output);
    Path packagePath = output.resolve("target");
    Files.writeString(
        packagePath.resolve("Support.java"),
        "package target; class Support { void decorate(PersonBuilder builder) {} }");
    Files.writeString(
        packagePath.resolve("Usage.java"),
        "package target; class Usage { void use() {"
            + " new PersonBuilder().setName(\"Ada\").validate();"
            + " } }");

    new OutputCodeService().cleanCode(output, result.topToPublicSelfTypes());

    String top = Files.readString(packagePath.resolve("PersonBuilderTOP.java"));
    assertTrue(top.contains("return ((PersonBuilder) (this));"), top);
    assertTrue(top.contains("support.decorate(((PersonBuilder) (this)));"), top);
    assertGeneratedJavaCompiles(output);
  }

  @Test
  void rejectsConcreteClassWithUnrelatedSuperclass() throws IOException {
    ASTOrdinaryCompilationUnit concrete =
        javaSource(
            "concrete/Account.java", "package target; class Account extends ExistingBase {}");
    ASTOrdinaryCompilationUnit adapted =
        javaSource("adapted/Account.java", "package target; class Account {}");

    CodeAdaptationException exception =
        assertThrows(
            CodeAdaptationException.class,
            () ->
                composer.compose(
                    linkedSet(concrete), linkedSet(adapted), concreteIndex("class Account;")));

    assertTrue(exception.getMessage().contains("ExistingBase"));
  }

  @Test
  void rejectsTopSuperclassImportedFromAnotherPackage() throws IOException {
    ASTOrdinaryCompilationUnit concrete =
        javaSource(
            "concrete/PersonBuilder.java",
            "package target; import external.PersonBuilderTOP;"
                + " class PersonBuilder extends PersonBuilderTOP {}");
    ASTOrdinaryCompilationUnit adapted =
        javaSource("adapted/PersonBuilder.java", "package target; class PersonBuilder {}");

    CodeAdaptationException exception =
        assertThrows(
            CodeAdaptationException.class,
            () ->
                composer.compose(
                    linkedSet(concrete),
                    linkedSet(adapted),
                    concreteIndex("class PersonBuilder;")));

    assertTrue(exception.getMessage().contains("PersonBuilderTOP"));
  }

  private ASTOrdinaryCompilationUnit javaSource(String relativePath, String source)
      throws IOException {
    Path file = temporaryDirectory.resolve(relativePath);
    Files.createDirectories(file.getParent());
    Files.writeString(file, source);
    return JavaLoader.loadJava(file.toFile());
  }

  private CDModelIndex concreteIndex(String declarations) throws IOException {
    Path model = temporaryDirectory.resolve("Concrete.cd");
    Files.writeString(model, "classdiagram Concrete { " + declarations + " }");
    return CDModelIndex.of(JavaLoader.parseCD(model.toString()));
  }

  private ASTOrdinaryCompilationUnit unitNamed(
      Set<ASTOrdinaryCompilationUnit> units, String typeName) {
    return units.stream()
        .filter(
            unit ->
                unit.getTypeDeclarationList().stream()
                    .anyMatch(type -> type.getName().equals(typeName)))
        .findFirst()
        .orElseThrow();
  }

  private Set<String> typeNames(Set<ASTOrdinaryCompilationUnit> units) {
    LinkedHashSet<String> names = new LinkedHashSet<>();
    units.forEach(
        unit -> unit.getTypeDeclarationList().forEach(type -> names.add(type.getName())));
    return names;
  }

  private static <T> LinkedHashSet<T> linkedSet(T... values) {
    return new LinkedHashSet<>(java.util.List.of(values));
  }
}
