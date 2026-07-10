package de.monticore.codeAdaption.updater.spoonUpdater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spoon.reflect.reference.CtTypeReference;

class SpoonServicesTest {
  @TempDir Path temporaryDirectory;

  @BeforeEach
  void initializeMill() {
    AdapterAbstractTest.initMills();
  }

  @Test
  void reloadReplacesTheModelAndTypeReferencesAreClonedFromCachedPrototypes() throws IOException {
    Path first = sourceDirectory("first", "First");
    Path second = sourceDirectory("second", "Second");
    SpoonWorkspace workspace = new SpoonWorkspace();

    workspace.load(first);
    assertEquals("First", workspace.model().getAllTypes().iterator().next().getSimpleName());
    CtTypeReference<?> firstReference = workspace.createTypeReference("List<String>");
    CtTypeReference<?> secondReference = workspace.createTypeReference("List<String>");
    assertNotSame(firstReference, secondReference);

    workspace.load(second);
    assertEquals("Second", workspace.model().getAllTypes().iterator().next().getSimpleName());
  }

  @Test
  void typeResolutionUsesTheSourcePathWhenPackagesContainTheSameSimpleName()
      throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("packages"));
    Path first = javaSource(sources, "alpha", "Shared", "class Shared {}");
    javaSource(sources, "beta", "Shared", "class Shared {}");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    SpoonElementResolver resolver = new SpoonElementResolver(workspace::model);

    ASTTypeDeclaration sourceType = loadType(first, "Shared");

    assertEquals("alpha.Shared", resolver.getSpoonType(sourceType).getQualifiedName());
  }

  @Test
  void methodResolutionRequiresAnExactDeclaredName() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("methods"));
    Path source =
        javaSource(
            sources,
            "sample",
            "Child",
            "class Base { void run() {} } class Child extends Base { void targetRun() {} }");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    SpoonElementResolver resolver = new SpoonElementResolver(workspace::model);
    ASTTypeDeclaration child = loadType(source, "Child");
    ASTMethodDeclaration targetRun = loadMethod(source, "Child", "targetRun");

    assertEquals("targetRun", resolver.getSpoonMethod(child, targetRun).getSimpleName());
  }

  @Test
  void clonedMethodParametersRemainBoundToTheClonedBody() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("generation"));
    Path source =
        javaSource(
            sources,
            "sample",
            "Echo",
            "class Echo { String echo(String value) { return value; } }");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    SpoonElementResolver resolver = new SpoonElementResolver(workspace::model);
    SpoonGenerationService generation = new SpoonGenerationService(workspace, resolver);
    ASTTypeDeclaration echo = loadType(source, "Echo");
    ASTMethodDeclaration template = loadMethod(source, "Echo", "echo");

    generation.addMethod(
        echo,
        template,
        "renamedEcho",
        List.of("String"),
        List.of("renamedValue"),
        "String",
        false,
        null,
        null);

    String generated =
        resolver.getSpoonType(echo).getMethodsByName("renamedEcho").get(0).toString();
    assertTrue(generated.contains("return renamedValue"), generated);
  }

  @Test
  void clonedPrimitiveMethodReplacesNullWithACompilableDefault() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("primitive-return"));
    Path source =
        javaSource(
            sources,
            "sample",
            "Template",
            "class Template { Object value() { return null; } }");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    SpoonElementResolver resolver = new SpoonElementResolver(workspace::model);
    SpoonGenerationService generation = new SpoonGenerationService(workspace, resolver);
    ASTTypeDeclaration type = loadType(source, "Template");
    ASTMethodDeclaration template = loadMethod(source, "Template", "value");

    generation.addMethod(
        type,
        template,
        "number",
        List.of(),
        List.of(),
        "int",
        false,
        null,
        null);

    String generated = resolver.getSpoonType(type).getMethodsByName("number").get(0).toString();
    assertTrue(generated.contains("return 0"), generated);
  }

  @Test
  void typeRewriteDoesNotTouchResolvedLibraryTypesWithTheSameSimpleName() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("library-type"));
    javaSource(
        sources,
        "sample",
        "UsesDate",
        "class UsesDate { java.util.Date createdAt; }");
    Path cd = temporaryDirectory.resolve("Model.cd");
    Files.writeString(cd, "classdiagram Model { class Date; }");
    ASTCDType date =
        JavaLoader.parseCD(cd.toString()).getCDDefinition().getCDClassesList().get(0);
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    SpoonTransformationService transformations =
        new SpoonTransformationService(workspace, new SpoonElementResolver(workspace::model));

    transformations.updateCDType(date, "RenamedDate");

    var renderedType = workspace.model().getAllTypes().iterator().next();
    assertEquals(
        "java.util.Date",
        renderedType.getField("createdAt").getType().getQualifiedName());
  }

  @Test
  void typeRewriteUpdatesUnresolvedTypesFromTheAdapterPackage() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("adapter-type"));
    javaSource(
        sources,
        "sample",
        "UsesRole",
        "class UsesRole { Role role; Role first(Role fallback) { return fallback; } }");
    Path cd = temporaryDirectory.resolve("Reference.cd");
    Files.writeString(cd, "classdiagram Reference { class Role; }");
    ASTCDType role =
        JavaLoader.parseCD(cd.toString()).getCDDefinition().getCDClassesList().get(0);
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    SpoonTransformationService transformations =
        new SpoonTransformationService(workspace, new SpoonElementResolver(workspace::model));

    transformations.updateCDType(role, "HiwiRole");

    CtTypeReference<?> fieldType =
        workspace.model().getAllTypes().iterator().next().getField("role").getType();
    assertEquals("HiwiRole", fieldType.getSimpleName());
    assertTrue(
        workspace.model().getAllTypes().iterator().next().toString().contains("HiwiRole first"));
  }

  private Path sourceDirectory(String directoryName, String typeName) throws IOException {
    Path directory = Files.createDirectory(temporaryDirectory.resolve(directoryName));
    Files.writeString(
        directory.resolve(typeName + ".java"),
        "class " + typeName + " {}",
        StandardCharsets.UTF_8);
    return directory;
  }

  private Path javaSource(
      Path sourceRoot, String packageName, String typeName, String declaration) throws IOException {
    Path directory = Files.createDirectories(sourceRoot.resolve(packageName.replace('.', '/')));
    Path source = directory.resolve(typeName + ".java");
    Files.writeString(
        source,
        "package " + packageName + "; " + declaration,
        StandardCharsets.UTF_8);
    return source;
  }

  private ASTTypeDeclaration loadType(Path source, String typeName) {
    JavaAstElemCollector collector = collect(source);
    return collector.getAllTypeDeclarations().stream()
        .filter(type -> typeName.equals(type.getName()))
        .findFirst()
        .orElseThrow();
  }

  private ASTMethodDeclaration loadMethod(Path source, String typeName, String methodName) {
    JavaAstElemCollector collector = collect(source);
    ASTTypeDeclaration type =
        collector.getAllTypeDeclarations().stream()
            .filter(candidate -> typeName.equals(candidate.getName()))
            .findFirst()
            .orElseThrow();
    return collector.getAllMethodDeclarations(type).stream()
        .filter(method -> methodName.equals(method.getName()))
        .findFirst()
        .orElseThrow();
  }

  private JavaAstElemCollector collect(Path source) {
    ASTOrdinaryCompilationUnit ast = JavaLoader.loadJava(source.toFile());
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    ast.accept(traverser);
    return collector;
  }
}
