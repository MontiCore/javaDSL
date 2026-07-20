package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AdaptedCodeMergerTest extends AdapterAbstractTest {

  @TempDir Path tempDir;

  private AdaptedCodeMerger merger;

  @BeforeEach
  void setUp() {
    initMills();
    merger = new AdaptedCodeMerger();
  }

  @Test
  void preservesSameSimpleTypeNameInDifferentPackages() throws IOException {
    ASTOrdinaryCompilationUnit first =
        parse("a/User.java", "package a; public class User { int first; }");
    ASTOrdinaryCompilationUnit second =
        parse("b/User.java", "package b; public class User { int second; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCode(new LinkedHashSet<>(), linkedSet(second, first));

    assertEquals(2, merged.size());
    assertEquals(
        Set.of("a", "b"),
        merged.stream()
            .map(unit -> unit.getPackageDeclaration().getMCQualifiedName().getQName())
            .collect(java.util.stream.Collectors.toSet()));
  }

  @Test
  void mergesValidSameNameOverloads() throws IOException {
    ASTOrdinaryCompilationUnit first =
        parse("first/Service.java", "package p; public class Service { void run(String value) {} }");
    ASTOrdinaryCompilationUnit second =
        parse("second/Service.java", "package p; public class Service { void run(int value) {} }");

    ASTOrdinaryCompilationUnit merged =
        merger.mergeAdaptedCode(linkedSet(first), linkedSet(second)).iterator().next();
    JavaAstElemCollector collector = collect(merged);

    assertEquals(
        2,
        collector
            .getAllMethodDeclarations(merged.getTypeDeclarationList().get(0))
            .size());
  }

  @Test
  void mergesMethodsProducedBySeparateIncarnationPasses() throws IOException {
    ASTOrdinaryCompilationUnit math =
        parse(
            "math/Teacher.java",
            "package Concrete; class Teacher { MathDept firstMathDept(MathDept fallback) { return fallback; } }");
    ASTOrdinaryCompilationUnit science =
        parse(
            "science/Teacher.java",
            "package Concrete; class Teacher { ScienceDept firstScienceDept(ScienceDept fallback) { return fallback; } }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCode(
            merger.mergeAdaptedCode(new LinkedHashSet<>(), linkedSet(math)),
            linkedSet(science));
    ASTOrdinaryCompilationUnit teacher = merged.iterator().next();
    JavaAstElemCollector collector = collect(teacher);

    assertEquals(
        Set.of("firstMathDept", "firstScienceDept"),
        collector.getAllMethodDeclarations(teacher.getTypeDeclarationList().get(0)).stream()
            .map(de.monticore.javalight._ast.ASTMethodDeclaration::getName)
            .collect(java.util.stream.Collectors.toSet()));
  }

  @Test
  void splitKeepsNestedTypesInsideTheirTopLevelOwner() throws IOException {
    ASTOrdinaryCompilationUnit source =
        parse(
            "Combined.java",
            "package p; class Outer { static class Inner { void nested() {} } void outer() {} } "
                + "class Second {}");

    Set<ASTOrdinaryCompilationUnit> split = merger.splitCompilationUnitsByType(linkedSet(source));

    assertEquals(2, split.size());
    ASTOrdinaryCompilationUnit outer =
        split.stream()
            .filter(unit -> unit.getTypeDeclarationList().get(0).getName().equals("Outer"))
            .findFirst()
            .orElseThrow();
    assertEquals(1, outer.getTypeDeclarationList().size());
    assertTrue(JavaLoader.print(outer).contains("class Inner"));

    JavaAstElemCollector collector = collect(outer);
    assertEquals(1, collector.getAllTypeDeclarations().size());
    assertEquals(
        1,
        collector
            .getAllMethodDeclarations(outer.getTypeDeclarationList().get(0))
            .size());
  }

  @Test
  void mergesAdaptedTypeIntoUniqueConcreteTypeAcrossPackages() throws IOException {
    ASTOrdinaryCompilationUnit adapted =
        parse("adapter/Port.java", "package adapter; interface Port { void send(); }");
    ASTOrdinaryCompilationUnit concrete =
        parse("concrete/Port.java", "package concrete; interface Port {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { interface Port; }");

    ASTOrdinaryCompilationUnit merged =
        merger
            .mergeAdaptedCodeIntoConcreteBase(
                linkedSet(concrete),
                linkedSet(adapted),
                CDModelIndex.of(JavaLoader.parseCD(cd.toString())))
            .iterator()
            .next();

    assertEquals(
        "concrete", merged.getPackageDeclaration().getMCQualifiedName().getQName());
    assertTrue(JavaLoader.print(merged).contains("void send()"));
  }

  @Test
  void preservesCompletedAbstractnessWhenMergingIntoConcreteCode() throws IOException {
    ASTOrdinaryCompilationUnit adapted =
        parse(
            "adapter/BankAccount.java",
            "package adapter; abstract class BankAccount implements Auditable {}");
    ASTOrdinaryCompilationUnit concrete =
        parse("concrete/BankAccount.java", "package concrete; class BankAccount {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { abstract class BankAccount; }");

    ASTOrdinaryCompilationUnit merged =
        merger
            .mergeAdaptedCodeIntoConcreteBase(
                linkedSet(concrete),
                linkedSet(adapted),
                CDModelIndex.of(JavaLoader.parseCD(cd.toString())))
            .stream()
            .filter(unit -> unit.getTypeDeclarationList().get(0).getName().equals("BankAccount"))
            .findFirst()
            .orElseThrow();

    String rendered = JavaLoader.print(merged);
    assertTrue(rendered.contains("abstract class BankAccount"), rendered);
    assertTrue(rendered.contains("implements Auditable"), rendered);
  }

  @Test
  void removesStaleAbstractnessWhenCompletedTypeIsConcrete() throws IOException {
    ASTOrdinaryCompilationUnit adapted =
        parse("adapter/BankAccount.java", "package adapter; class BankAccount {}");
    ASTOrdinaryCompilationUnit concrete =
        parse(
            "concrete/BankAccount.java",
            "package concrete; public abstract class BankAccount {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class BankAccount; }");

    ASTOrdinaryCompilationUnit merged =
        merger
            .mergeAdaptedCodeIntoConcreteBase(
                linkedSet(concrete),
                linkedSet(adapted),
                CDModelIndex.of(JavaLoader.parseCD(cd.toString())))
            .iterator()
            .next();

    String rendered = JavaLoader.print(merged);
    assertTrue(rendered.contains("public class BankAccount"), rendered);
    assertFalse(rendered.contains("abstract class BankAccount"), rendered);
  }

  @Test
  void importsRelocatedTypesIntoDependentAdaptedUnits() throws IOException {
    ASTOrdinaryCompilationUnit adaptedAccount =
        parse("adapter/Account.java", "package adapter; class Account {}");
    ASTOrdinaryCompilationUnit adaptedTransaction =
        parse(
            "adapter/Transaction.java",
            "package adapter; class Transaction { Account source; }");
    ASTOrdinaryCompilationUnit concreteAccount =
        parse("concrete/Account.java", "package concrete; class Account {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Account; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(concreteAccount),
            linkedSet(adaptedAccount, adaptedTransaction),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    ASTOrdinaryCompilationUnit transaction =
        merged.stream()
            .filter(unit -> unit.getTypeDeclarationList().get(0).getName().equals("Transaction"))
            .findFirst()
            .orElseThrow();
    assertEquals(
        Set.of("concrete.Account"),
        transaction.getImportDeclarationList().stream()
            .map(importDeclaration -> importDeclaration.getMCQualifiedName().getQName())
            .collect(java.util.stream.Collectors.toSet()));
  }

  @Test
  void importsRelocatedSuperclassIntoDependentAdaptedType() throws IOException {
    ASTOrdinaryCompilationUnit adaptedBase =
        parse("adapter/Base.java", "package adapter; class Base {}");
    ASTOrdinaryCompilationUnit adaptedChild =
        parse("adapter/Child.java", "package adapter; class Child extends Base {}");
    ASTOrdinaryCompilationUnit concreteBase =
        parse("domain/Base.java", "package domain; class Base {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Base; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(concreteBase),
            linkedSet(adaptedBase, adaptedChild),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    ASTOrdinaryCompilationUnit child =
        merged.stream()
            .filter(unit -> unit.getTypeDeclarationList().get(0).getName().equals("Child"))
            .findFirst()
            .orElseThrow();
    assertTrue(JavaLoader.print(child).contains("import domain.Base;"));
  }

  @Test
  void memberImportRepairPreservesUnrelatedExplicitImportBinding() throws IOException {
    ASTOrdinaryCompilationUnit consumer =
        parse(
            "adapter/Consumer.java",
            "package adapter; import java.util.Date; class Consumer { Date createdAt; }");
    ASTOrdinaryCompilationUnit domainDate =
        parse("domain/Date.java", "package domain; class Date {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Date; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(domainDate),
            linkedSet(consumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("import java.util.Date;"), rendered);
    assertFalse(rendered.contains("import domain.Date;"), rendered);
  }

  @Test
  void memberImportRepairAcceptsExactExplicitImportBinding() throws IOException {
    ASTOrdinaryCompilationUnit consumer =
        parse(
            "adapter/Consumer.java",
            "package adapter; import domain.Date; class Consumer { Date createdAt; }");
    ASTOrdinaryCompilationUnit domainDate =
        parse("domain/Date.java", "package domain; class Date {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Date; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(domainDate),
            linkedSet(consumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("import domain.Date;"), rendered);
  }

  @Test
  void memberImportRepairPreservesImplicitJavaLangBinding() throws IOException {
    ASTOrdinaryCompilationUnit consumer =
        parse("adapter/Consumer.java", "package adapter; class Consumer { Runnable action; }");
    ASTOrdinaryCompilationUnit domainRunnable =
        parse("domain/Runnable.java", "package domain; class Runnable {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Runnable; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(domainRunnable),
            linkedSet(consumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("Runnable action"), rendered);
    assertFalse(rendered.contains("import domain.Runnable;"), rendered);
  }

  @Test
  void memberImportRepairAddsExplicitImportUnderUnrelatedWildcard() throws IOException {
    ASTOrdinaryCompilationUnit consumer =
        parse(
            "adapter/Consumer.java",
            "package adapter; import java.io.*; class Consumer { Date createdAt; }");
    ASTOrdinaryCompilationUnit domainDate =
        parse("domain/Date.java", "package domain; class Date {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Date; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(domainDate),
            linkedSet(consumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("import java.io.*;"), rendered);
    assertTrue(rendered.contains("import domain.Date;"), rendered);
  }

  @Test
  void memberImportRepairPreservesMatchingPlatformWildcardBinding() throws IOException {
    ASTOrdinaryCompilationUnit consumer =
        parse(
            "adapter/Consumer.java",
            "package adapter; import java.util.*; class Consumer { Date createdAt; }");
    ASTOrdinaryCompilationUnit domainDate =
        parse("domain/Date.java", "package domain; class Date {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Date; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(domainDate),
            linkedSet(consumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("import java.util.*;"), rendered);
    assertFalse(rendered.contains("import domain.Date;"), rendered);
  }

  @Test
  void memberImportRepairAcceptsMatchingWildcardBinding() throws IOException {
    ASTOrdinaryCompilationUnit consumer =
        parse(
            "adapter/Consumer.java",
            "package adapter; import domain.*; class Consumer { Date createdAt; }");
    ASTOrdinaryCompilationUnit domainDate =
        parse("domain/Date.java", "package domain; class Date {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Date; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(domainDate),
            linkedSet(consumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("import domain.*;"), rendered);
    assertFalse(rendered.contains("import domain.Date;"), rendered);
  }

  @Test
  void memberImportRepairUsesUniqueMatchingWildcardDespiteUnrelatedOutputCandidate()
      throws IOException {
    ASTOrdinaryCompilationUnit consumer =
        parse(
            "adapter/Consumer.java",
            "package adapter; import domain.*; class Consumer { Date createdAt; }");
    ASTOrdinaryCompilationUnit domainDate =
        parse("domain/Date.java", "package domain; class Date {}");
    ASTOrdinaryCompilationUnit unrelatedDate =
        parse("other/Date.java", "package other; class Date {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Date; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(domainDate, unrelatedDate),
            linkedSet(consumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("import domain.*;"), rendered);
    assertFalse(rendered.contains("import domain.Date;"), rendered);
  }

  @Test
  void memberImportRepairDoesNotRebindConcreteOnlyReference() throws IOException {
    ASTOrdinaryCompilationUnit concreteConsumer =
        parse(
            "concrete/Consumer.java",
            "package adapter; class Consumer { External handwritten; }");
    ASTOrdinaryCompilationUnit adaptedConsumer =
        parse(
            "adapter/Consumer.java",
            "package adapter; class Consumer { void generated() {} }");
    ASTOrdinaryCompilationUnit domainExternal =
        parse("domain/External.java", "package domain; class External {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Consumer; class External; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(concreteConsumer, domainExternal),
            linkedSet(adaptedConsumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("External handwritten"), rendered);
    assertFalse(rendered.contains("import domain.External;"), rendered);
  }

  @Test
  void memberImportRepairDoesNotImportForQualifiedReference() throws IOException {
    ASTOrdinaryCompilationUnit consumer =
        parse(
            "adapter/Consumer.java",
            "package adapter; class Consumer { foreign.Date createdAt; }");
    ASTOrdinaryCompilationUnit domainDate =
        parse("domain/Date.java", "package domain; class Date {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Date; }");

    Set<ASTOrdinaryCompilationUnit> merged =
        merger.mergeAdaptedCodeIntoConcreteBase(
            linkedSet(domainDate),
            linkedSet(consumer),
            CDModelIndex.of(JavaLoader.parseCD(cd.toString())));

    String rendered = JavaLoader.print(unitNamed(merged, "Consumer"));
    assertTrue(rendered.contains("foreign.Date createdAt"), rendered);
    assertFalse(rendered.contains("import domain.Date;"), rendered);
  }

  @Test
  void rejectsConflictingImportForRelocatedType() throws IOException {
    ASTOrdinaryCompilationUnit adaptedAccount =
        parse("adapter/Account.java", "package adapter; class Account {}");
    ASTOrdinaryCompilationUnit adaptedTransaction =
        parse(
            "adapter/Transaction.java",
            "package adapter; import other.Account; class Transaction { Account source; }");
    ASTOrdinaryCompilationUnit concreteAccount =
        parse("concrete/Account.java", "package concrete; class Account {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Account; }");

    assertThrows(
        CodeAdaptationException.class,
        () ->
            merger.mergeAdaptedCodeIntoConcreteBase(
                linkedSet(concreteAccount),
                linkedSet(adaptedAccount, adaptedTransaction),
                CDModelIndex.of(JavaLoader.parseCD(cd.toString()))));
  }

  @Test
  void rejectsConflictingImportsFromSeparateMappingPasses() throws IOException {
    ASTOrdinaryCompilationUnit first =
        parse("first/Service.java", "package p; import a.User; class Service {}");
    ASTOrdinaryCompilationUnit second =
        parse("second/Service.java", "package p; import b.User; class Service {}");

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> merger.mergeAdaptedCode(linkedSet(first), linkedSet(second)));

    assertTrue(exception.getMessage().contains("both use the type name 'User'"));
  }

  @Test
  void keepsStaticAndWildcardImportsInSeparateNamespaces() throws IOException {
    ASTOrdinaryCompilationUnit first =
        parse(
            "first/Service.java",
            "package p; import a.User; import a.*; class Service {}");
    ASTOrdinaryCompilationUnit second =
        parse(
            "second/Service.java",
            "package p; import static b.User; import b.*; class Service {}");

    ASTOrdinaryCompilationUnit merged =
        merger.mergeAdaptedCode(linkedSet(first), linkedSet(second)).iterator().next();

    assertEquals(4, merged.getImportDeclarationList().size());
  }

  @Test
  void rejectsConflictingImportsWhenMergingIntoConcreteCode() throws IOException {
    ASTOrdinaryCompilationUnit concrete =
        parse("concrete/Service.java", "package p; import a.User; class Service {}");
    ASTOrdinaryCompilationUnit adapted =
        parse("adapted/Service.java", "package p; import b.User; class Service { void run() {} }");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Service; }");

    assertThrows(
        IllegalStateException.class,
        () ->
            merger.mergeAdaptedCodeIntoConcreteBase(
                linkedSet(concrete),
                linkedSet(adapted),
                CDModelIndex.of(JavaLoader.parseCD(cd.toString()))));
  }

  @Test
  void mergesEnumConstantsFromAdaptedAndConcreteSources() throws IOException {
    ASTOrdinaryCompilationUnit concrete =
        parse("concrete/Colour.java", "package p; enum Colour { RED, BLUE }");
    ASTOrdinaryCompilationUnit adapted =
        parse("adapted/Colour.java", "package p; enum Colour { RED, GREEN }");

    ASTOrdinaryCompilationUnit merged =
        merger.mergeAdaptedCode(linkedSet(concrete), linkedSet(adapted)).iterator().next();

    assertTrue(JavaLoader.print(merged).replaceAll("\\s+", "").contains("RED,GREEN,BLUE"));
  }

  @Test
  void rejectsAmbiguousConcretePackageForAdaptedType() throws IOException {
    ASTOrdinaryCompilationUnit adapted =
        parse("adapter/Account.java", "package adapter; class Account {}");
    ASTOrdinaryCompilationUnit first =
        parse("first/Account.java", "package first; class Account {}");
    ASTOrdinaryCompilationUnit second =
        parse("second/Account.java", "package second; class Account {}");
    Path cd = tempDir.resolve("Concrete.cd");
    Files.writeString(cd, "classdiagram Concrete { class Account; }");

    CodeAdaptationException exception =
        assertThrows(
            CodeAdaptationException.class,
            () ->
                merger.mergeAdaptedCodeIntoConcreteBase(
                    linkedSet(first, second),
                    linkedSet(adapted),
                    CDModelIndex.of(JavaLoader.parseCD(cd.toString()))));

    assertTrue(exception.getMessage().contains("multiple packages"));
  }

  private ASTOrdinaryCompilationUnit parse(String relativePath, String source) throws IOException {
    Path file = tempDir.resolve(relativePath);
    Files.createDirectories(file.getParent());
    Files.writeString(file, source);
    return JavaLoader.loadJava(file.toFile());
  }

  private ASTOrdinaryCompilationUnit unitNamed(
      Set<ASTOrdinaryCompilationUnit> units, String typeName) {
    return units.stream()
        .filter(unit -> unit.getTypeDeclarationList().get(0).getName().equals(typeName))
        .findFirst()
        .orElseThrow();
  }

  @SafeVarargs
  private static <T> LinkedHashSet<T> linkedSet(T... values) {
    return new LinkedHashSet<>(java.util.List.of(values));
  }

  private static JavaAstElemCollector collect(ASTOrdinaryCompilationUnit unit) {
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    unit.accept(traverser);
    return collector;
  }
}
