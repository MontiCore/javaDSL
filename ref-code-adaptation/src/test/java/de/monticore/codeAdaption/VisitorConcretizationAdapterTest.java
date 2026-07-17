package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** End-to-end regression for projecting overloads introduced by Visitor CD completion. */
class VisitorConcretizationAdapterTest extends AdapterAbstractTest {

  private static final Path ROOT =
      Path.of("src/test/resources/de/monticore/codeAdaption/cdconcretization/evaluation/visitor");

  @TempDir Path output;

  @BeforeEach
  void setUp() {
    initMills();
  }

  @Test
  void projectsEveryCompletedVisitorOverload() {
    CodeAdapter adapter =
        new CodeAdapter(
            Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE),
            Set.of(
                STEREOTYPE_MAPPING,
                NAME_MAPPING,
                SRC_TARGET_ASSOC_MAPPING,
                INHERITANCE,
                ALLOW_CARD_RESTRICTION,
                METHOD_OVERLOADING,
                ADAPTED_NAME_MAPPING,
                ALLOW_ADDITIONAL_PARAMETERS,
                STRICT_PARAMETER_ORDER));

    adapter.adapt(
        ROOT.resolve("VisitorRef.cd").toFile(),
        ROOT.resolve("VisitorConc.cd").toFile(),
        Set.of("ref"),
        ROOT.resolve("adapter/Visitor"),
        ROOT.resolve("concrete"),
        output,
        true,
        true);

    String visitor = readFileContent(output, "NodeVisitor.java");
    assertTrue(visitor.contains("visit(Node node)"));
    assertTrue(visitor.contains("visit(RootNode rootNode)"));
    assertTrue(visitor.contains("visit(InnerNode innerNode)"));
    assertTrue(visitor.contains("visit(LeafNode leafNode)"));
    assertGeneratedJavaCompiles(generatedJavaFiles(output));
  }
}
