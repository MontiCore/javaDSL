package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.utils.JavaLoader;
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
                JavaLoader.parseCD(cd.toString()))
            .iterator()
            .next();

    assertEquals(
        "concrete", merged.getPackageDeclaration().getMCQualifiedName().getQName());
    assertTrue(JavaLoader.print(merged).contains("void send()"));
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
            JavaLoader.parseCD(cd.toString()));

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
            JavaLoader.parseCD(cd.toString()));

    ASTOrdinaryCompilationUnit child =
        merged.stream()
            .filter(unit -> unit.getTypeDeclarationList().get(0).getName().equals("Child"))
            .findFirst()
            .orElseThrow();
    assertTrue(JavaLoader.print(child).contains("import domain.Base;"));
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
                JavaLoader.parseCD(cd.toString())));
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
                    JavaLoader.parseCD(cd.toString())));

    assertTrue(exception.getMessage().contains("multiple packages"));
  }

  private ASTOrdinaryCompilationUnit parse(String relativePath, String source) throws IOException {
    Path file = tempDir.resolve(relativePath);
    Files.createDirectories(file.getParent());
    Files.writeString(file, source);
    return JavaLoader.loadJava(file.toFile());
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
