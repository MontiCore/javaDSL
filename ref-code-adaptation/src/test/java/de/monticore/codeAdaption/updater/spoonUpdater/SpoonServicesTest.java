package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.updater.CodeUpdater.MethodBodySpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
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
import java.util.Map;
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
        MethodBodySpec.empty());

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
        MethodBodySpec.empty());

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
    SpoonTransformationService transformations = services(workspace).transformations();

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
    SpoonTransformationService transformations = services(workspace).transformations();

    transformations.updateCDType(role, "HiwiRole");

    CtTypeReference<?> fieldType =
        workspace.model().getAllTypes().iterator().next().getField("role").getType();
    assertEquals("HiwiRole", fieldType.getSimpleName());
    assertTrue(
        workspace.model().getAllTypes().iterator().next().toString().contains("HiwiRole first"));
  }

  @Test
  void methodRenameUpdatesOnlyTheSelectedOverload() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("overload-rename"));
    Path source =
        javaSource(
            sources,
            "sample",
            "Service",
            "class Service { void run(String value) {} void run(int first, int second) {} "
                + "void call() { run(\"x\"); run(1, 2); } }");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    Services services = services(workspace);

    services
        .transformations()
        .updateMethod(loadType(source, "Service"), loadMethod(source, "Service", "run", 1), "execute");

    String rendered = workspace.model().getAllTypes().iterator().next().toString();
    assertTrue(rendered.contains("execute(\"x\")"), rendered);
    assertTrue(rendered.contains("run(1, 2)"), rendered);
    assertEquals(1, workspace.model().getAllTypes().iterator().next().getMethodsByName("run").size());
  }

  @Test
  void methodRenameUpdatesStaticAndInheritedInvocations() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("static-inherited"));
    Path source =
        javaSource(
            sources,
            "sample",
            "Base",
            "class Base { static void create() {} void run() {} } "
                + "class Child extends Base { void call() { run(); Base.create(); } }");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    Services services = services(workspace);
    ASTTypeDeclaration base = loadType(source, "Base");

    services.transformations().updateMethod(base, loadMethod(source, "Base", "run"), "execute");
    services.transformations().updateMethod(base, loadMethod(source, "Base", "create"), "make");

    String rendered = workspace.model().getAllTypes().stream()
        .filter(type -> "Child".equals(type.getSimpleName()))
        .findFirst()
        .orElseThrow()
        .toString();
    assertTrue(rendered.contains("execute()"), rendered);
    assertTrue(rendered.contains("Base.make()"), rendered);
  }

  @Test
  void stableMethodRewriteSelectsTheOverloadAndAddsACompatibleArgument() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("signature-repair"));
    javaSource(
        sources,
        "sample",
        "Service",
        "class Service { void target(String value) {} void target(int value) {} "
            + "void call(String suffix) { target(\"x\"); target(1); } }");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    Services services = services(workspace);
    services
        .repairs()
        .registerMethodRewrite(
            StableElementKey.method("Service", "target", List.of("String")),
            StableElementKey.method("Service", "execute", List.of("String", "String")));

    services.repairs().prepareForPrint(Map.of());

    String rendered = workspace.model().getAllTypes().iterator().next().toString();
    assertTrue(rendered.contains("execute(\"x\", suffix)"), rendered);
    assertTrue(rendered.contains("target(1)"), rendered);
  }

  @Test
  void groupingRunsBeforeOwnerAwareExecutableRepair() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("grouped-owner"));
    javaSource(
        sources,
        "sample",
        "ConcreteService",
        "class ConcreteService { void target(String value) {} } "
            + "class Caller { ConcreteService service; "
            + "void call(String suffix) { service.target(\"x\"); } }");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    Services services = services(workspace);
    services.transformations().setGroupingMappings(Map.of("ConcreteService", "ServiceGroup"));
    services
        .repairs()
        .registerMethodRewrite(
            StableElementKey.method("ReferenceService", "target", List.of("String")),
            StableElementKey.method(
                "ConcreteService", "execute", List.of("String", "String")));

    services.transformations().prepareForPrint();

    String caller = workspace.model().getAllTypes().stream()
        .filter(type -> "Caller".equals(type.getSimpleName()))
        .findFirst()
        .orElseThrow()
        .toString();
    assertTrue(caller.contains("ServiceGroup service"), caller);
    assertTrue(caller.contains("service.execute(\"x\", suffix)"), caller);
  }

  @Test
  void constructorCallsFollowTypeReferenceRewrites() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("constructor-type"));
    javaSource(
        sources,
        "sample",
        "Factory",
        "class Factory { Role create() { return new Role(); } }");
    Path cd = temporaryDirectory.resolve("ConstructorReference.cd");
    Files.writeString(cd, "classdiagram Reference { class Role; }");
    ASTCDType role = JavaLoader.parseCD(cd.toString()).getCDDefinition().getCDClassesList().get(0);
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    Services services = services(workspace);

    services.transformations().updateCDType(role, "HiwiRole");

    String rendered = workspace.model().getAllTypes().iterator().next().toString();
    assertTrue(rendered.contains("HiwiRole create()"), rendered);
    assertTrue(rendered.contains("new HiwiRole()"), rendered);
  }

  @Test
  void executablePreparationRemovesOnlyIllegalInterfaceBodies() throws IOException {
    Path sources = Files.createDirectory(temporaryDirectory.resolve("interface-methods"));
    javaSource(
        sources,
        "sample",
        "Contract",
        "interface Contract { void run(); default void keep() {} static void utility() {} }");
    SpoonWorkspace workspace = new SpoonWorkspace();
    workspace.load(sources);
    Services services = services(workspace);
    var contract = workspace.model().getAllTypes().iterator().next();
    contract.getMethodsByName("run").get(0).setBody(workspace.factory().createBlock());

    services.repairs().prepareForPrint(Map.of());

    assertNull(contract.getMethodsByName("run").get(0).getBody());
    assertNotNull(contract.getMethodsByName("keep").get(0).getBody());
    assertNotNull(contract.getMethodsByName("utility").get(0).getBody());
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

  private ASTMethodDeclaration loadMethod(
      Path source, String typeName, String methodName, int parameterCount) {
    JavaAstElemCollector collector = collect(source);
    ASTTypeDeclaration type =
        collector.getAllTypeDeclarations().stream()
            .filter(candidate -> typeName.equals(candidate.getName()))
            .findFirst()
            .orElseThrow();
    return collector.getAllMethodDeclarations(type).stream()
        .filter(method -> methodName.equals(method.getName()))
        .filter(method -> collector.getAllParameters(type, method).size() == parameterCount)
        .findFirst()
        .orElseThrow();
  }

  private Services services(SpoonWorkspace workspace) {
    SpoonElementResolver resolver = new SpoonElementResolver(workspace::model);
    SpoonExecutableRepairService repairs = new SpoonExecutableRepairService(workspace, resolver);
    return new Services(
        repairs, new SpoonTransformationService(workspace, resolver, repairs));
  }

  private record Services(
      SpoonExecutableRepairService repairs, SpoonTransformationService transformations) {}

  private JavaAstElemCollector collect(Path source) {
    ASTOrdinaryCompilationUnit ast = JavaLoader.loadJava(source.toFile());
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    ast.accept(traverser);
    return collector;
  }
}
