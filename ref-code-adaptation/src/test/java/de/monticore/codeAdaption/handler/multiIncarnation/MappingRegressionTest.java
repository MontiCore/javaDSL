package de.monticore.codeAdaption.handler.multiIncarnation;

import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.CodeAdaptationException;
import de.monticore.codeAdaption.CodeAdapter;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Regression coverage for stable mapping identities, grouping, and projected members. */
class MappingRegressionTest extends AdapterAbstractTest {
  @TempDir Path temporaryDirectory;

  @BeforeEach
  void initialize() {
    LogStub.init();
    initMills();
    Log.enableFailQuick(false);
  }

  @Test
  void qualifiedParameterTypesMustProduceDifferentStableMethodKeys() {
    StableElementKey alpha =
        StableElementKey.method("Service", "handle", List.of("alpha.Role"), "void");
    StableElementKey beta =
        StableElementKey.method("Service", "handle", List.of("beta.Role"), "void");

    assertNotEquals(
        alpha,
        beta,
        "Distinct qualified overloads must not collapse to the same mapping/rewrite key");
  }

  @Test
  void commonParentOptionMustNotHideASingleClassInterfaceKindMismatch() throws Exception {
    ASTCDCompilationUnit reference =
        parseCd("KindReference.cd", "classdiagram KindReference { interface Port; }");
    ASTCDCompilationUnit concrete =
        parseCd(
            "KindConcrete.cd",
            "classdiagram KindConcrete { class Parent; class ConcretePort extends Parent; }");
    ASTCDType concretePort = CDModelIndex.of(concrete).type("ConcretePort").orElseThrow();
    StableElementKey referenceKey = StableElementKey.type("Port");
    IncarnationContext context =
        new IncarnationContext(
            "ref",
            Map.of(
                referenceKey,
                List.of(
                    new IncarnationContext.MappedElement(
                        StableElementKey.type(concretePort), concretePort.getSymbol()))),
            Map.of());

    assertThrows(
        CodeAdaptationException.class,
        () ->
            AdaptationConflictDetector.validate(
                CDModelIndex.of(reference),
                CDModelIndex.of(concrete),
                Set.of("ref"),
                Map.of("ref", context),
                Set.of(),
                true),
        "The option is for exact multi-incarnation groups, not arbitrary single mappings");
  }

  @Test
  void oneConcreteIncarnationMustNotSilentlyChooseBetweenTwoExactGroupingInterfaces()
      throws Exception {
    ASTCDCompilationUnit reference =
        parseCd("GroupingReference.cd", "classdiagram GroupingReference { class R1; class R2; }");
    ASTCDCompilationUnit concrete =
        parseCd(
            "GroupingConcrete.cd",
            "classdiagram GroupingConcrete { "
                + "interface G1; interface G2; "
                + "class A implements G1, G2; "
                + "class B implements G1; "
                + "class C implements G2; }");
    CDModelIndex concreteIndex = CDModelIndex.of(concrete);
    Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings =
        new LinkedHashMap<>();
    mappings.put(
        StableElementKey.type("R1"),
        List.of(mapped(concreteIndex, "A"), mapped(concreteIndex, "B")));
    mappings.put(
        StableElementKey.type("R2"),
        List.of(mapped(concreteIndex, "A"), mapped(concreteIndex, "C")));

    IncarnationMappingSupport support =
        new IncarnationMappingSupport(CDModelIndex.of(reference), concreteIndex);

    assertThrows(
        IllegalStateException.class,
        () -> support.assembleContext("mapping", mappings),
        "A shared incarnation in incompatible exact groups must be diagnosed, not overwritten");
  }

  @Test
  void knownExternalCovariantReturnMustBeAccepted() throws Exception {
    Path referenceCd =
        write(
            "CovariantReference.cd",
            "import java.util.List; classdiagram CovariantReference { "
                + "interface Provider { public List values(); } "
                + "class Service implements Provider; }");
    Path concreteCd =
        write(
            "CovariantConcrete.cd",
            "import java.util.ArrayList; classdiagram CovariantConcrete { "
                + "<<ref=\"Service\">> class ConcreteService { "
                + "<<ref=\"Provider.values\">> public ArrayList values(); } }");
    Path referenceJava = Files.createDirectory(temporaryDirectory.resolve("covariant-reference"));
    Path output = temporaryDirectory.resolve("covariant-output");
    Files.writeString(
        referenceJava.resolve("Service.java"),
        "import de.monticore.codeAdaption.utils.Adapt;\n"
            + "import java.util.List;\n"
            + "@Adapt(ref={\"Service\"}, template=\"${}\")\n"
            + "public class Service {\n"
            + "  @Adapt(ref={\"Provider.values\"}, template=\"${}\")\n"
            + "  public List values() { return new java.util.ArrayList(); }\n"
            + "}\n");

    assertDoesNotThrow(
        () ->
            new CodeAdapter(
                    Set.of(
                        ANNOTATION_MATCHING,
                        IGNORE_NON_MATCHED_VAR,
                        IGNORE_NON_MATCHED_TYPE),
                    Set.of(STEREOTYPE_MAPPING, NAME_MAPPING, INHERITANCE))
                .adaptWithoutConcreteCode(
                    referenceCd.toFile(),
                    concreteCd.toFile(),
                    Set.of("ref"),
                    referenceJava,
                    output,
                    true,
                    true),
        "A completed ArrayList implementation must satisfy a List interface contract");

    Path generated =
        Files.walk(output)
            .filter(path -> path.getFileName().toString().equals("ConcreteService.java"))
            .findFirst()
            .orElseThrow();
    String source = Files.readString(generated);
    assertTrue(source.contains("List values("), source);
  }

  @Test
  void mappedMethodRenameMustSurviveCompletedMemberProjection() throws Exception {
    Path referenceCd =
        write(
            "RenameReference.cd",
            "classdiagram RenameReference { class Service { void execute(); } }");
    Path concreteCd =
        write(
            "RenameConcrete.cd",
            "classdiagram RenameConcrete { "
                + "<<ref=\"Service\">> class ConcreteService { "
                + "<<ref=\"execute\">> void run(); } }");
    Path referenceJava = Files.createDirectory(temporaryDirectory.resolve("reference-java"));
    Path output = temporaryDirectory.resolve("output");
    Files.writeString(
        referenceJava.resolve("Service.java"),
        "import de.monticore.codeAdaption.utils.Adapt;\n"
            + "@Adapt(ref={\"Service\"}, template=\"${}\")\n"
            + "public class Service {\n"
            + "  @Adapt(ref={\"Service.execute\"}, template=\"${}\")\n"
            + "  public void execute() { System.out.println(\"kept\"); }\n"
            + "}\n");

    new CodeAdapter(
            Set.of(ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE),
            Set.of(STEREOTYPE_MAPPING, NAME_MAPPING, INHERITANCE))
        .adaptWithoutConcreteCode(
            referenceCd.toFile(),
            concreteCd.toFile(),
            Set.of("ref"),
            referenceJava,
            output,
            false,
            true);

    Path generated =
        Files.walk(output)
            .filter(path -> path.getFileName().toString().equals("ConcreteService.java"))
            .findFirst()
            .orElseThrow();
    String source = Files.readString(generated);
    assertTrue(source.contains("void run("), source);
    assertTrue(source.contains("kept"), source);
  }

  private ASTCDCompilationUnit parseCd(String fileName, String source) throws Exception {
    return JavaLoader.parseCD(write(fileName, source).toString());
  }

  private Path write(String fileName, String source) throws Exception {
    Path file = temporaryDirectory.resolve(fileName);
    Files.writeString(file, source);
    return file;
  }

  private static IncarnationContext.MappedElement mapped(CDModelIndex index, String typeName) {
    ASTCDType type = index.type(typeName).orElseThrow();
    return new IncarnationContext.MappedElement(StableElementKey.type(type), type.getSymbol());
  }
}
