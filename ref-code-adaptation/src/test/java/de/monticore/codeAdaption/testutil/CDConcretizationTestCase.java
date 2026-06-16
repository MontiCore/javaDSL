package de.monticore.codeAdaption.testutil;

import java.nio.file.Path;
import java.util.Set;

/** Describes one cdconcretization adaptation test fixture. */
public record CDConcretizationTestCase(
    String id,
    String displayName,
    Path refCd,
    Path concCd,
    Path adapterPath,
    Path concretePath,
    Path outputPath,
    boolean strictParameterOrder,
    boolean enabled) {

  public static final String RESOURCE_ROOT =
      "src/test/resources/de/monticore/codeAdaption/cdconcretization/";

  public static final String OUTPUT_ROOT = "target/adapter/cdconcretization/";

  public Set<String> mappings() {
    return Set.of("ref");
  }
}
