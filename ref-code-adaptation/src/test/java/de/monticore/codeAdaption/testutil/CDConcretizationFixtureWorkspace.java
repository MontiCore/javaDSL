package de.monticore.codeAdaption.testutil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Builds isolated Java fixture directories for cdconcretization tests. */
public final class CDConcretizationFixtureWorkspace {

  private static final Path RESOURCE_ROOT = Path.of(CDConcretizationTestCase.RESOURCE_ROOT);
  private static final Path SHARED_JAVA_ROOT = RESOURCE_ROOT.resolve("_shared").resolve("java");
  private static final Path INDEX = RESOURCE_ROOT.resolve("_shared").resolve("shared-fixtures.tsv");
  private static final Path WORKSPACE_ROOT = Path.of("target/test-fixtures/cdconcretization");
  private static final Pattern PACKAGE_DECLARATION =
      Pattern.compile("^\\s*package\\s+[^;\\r\\n]+;\\s*$");
  private static final Pattern PACKAGE_LINE_IN_SOURCE =
      Pattern.compile("(?m)^\\s*package\\s+[^;\\r\\n]+;");

  private static final Map<String, List<SharedFixtureEntry>> ENTRIES_BY_CASE = loadIndex();

  private CDConcretizationFixtureWorkspace() {}

  public static CDConcretizationTestCase materialize(CDConcretizationTestCase testCase) {
    Path caseRoot = safeResolve(WORKSPACE_ROOT, testCase.id());
    Path adapterTarget = caseRoot.resolve("adapter");
    Path concreteTarget = caseRoot.resolve("concrete");

    try {
      deleteRecursively(caseRoot);
      Files.createDirectories(adapterTarget);
      Files.createDirectories(concreteTarget);

      Set<String> sharedOutputs = new LinkedHashSet<>();
      for (SharedFixtureEntry entry :
          ENTRIES_BY_CASE.getOrDefault(relativeConcreteCd(testCase), List.of())) {
        Path roleTarget = roleTarget(entry.role(), adapterTarget, concreteTarget);
        String outputKey = entry.role() + ":" + normalizeRelative(entry.output());
        if (!sharedOutputs.add(outputKey)) {
          throw new IllegalStateException(
              "Duplicate shared fixture output for " + relativeConcreteCd(testCase) + ": " + outputKey);
        }
        writeSharedFixture(entry, roleTarget);
      }

      copyLocalOverlay(testCase.adapterPath(), adapterTarget);
      copyLocalOverlay(testCase.concretePath(), concreteTarget);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to materialize cdconcretization fixture for " + testCase.displayName(), e);
    }

    return new CDConcretizationTestCase(
        testCase.id(),
        testCase.displayName(),
        testCase.refCd(),
        testCase.concCd(),
        adapterTarget,
        concreteTarget,
        testCase.outputPath(),
        testCase.mappings(),
        testCase.strictParameterOrder(),
        testCase.enabled());
  }

  private static void writeSharedFixture(SharedFixtureEntry entry, Path roleTarget)
      throws IOException {
    Path sharedSource = safeResolve(RESOURCE_ROOT, entry.shared());
    if (!sharedSource.startsWith(SHARED_JAVA_ROOT.normalize())) {
      throw new IllegalStateException("Shared fixture source is outside _shared/java: " + entry.shared());
    }
    if (!Files.isRegularFile(sharedSource)) {
      throw new IllegalStateException("Missing shared fixture source: " + entry.shared());
    }

    Path output = safeResolve(roleTarget, entry.output());
    String source = Files.readString(sharedSource, StandardCharsets.UTF_8);
    Matcher matcher = PACKAGE_LINE_IN_SOURCE.matcher(source);
    if (!matcher.find()) {
      throw new IllegalStateException("Shared fixture has no package declaration: " + entry.shared());
    }

    String rendered = matcher.replaceFirst(Matcher.quoteReplacement(entry.packageLine()));
    Files.createDirectories(output.getParent());
    Files.writeString(output, rendered, StandardCharsets.UTF_8);
  }

  private static void copyLocalOverlay(Path sourceRoot, Path targetRoot) throws IOException {
    if (!Files.exists(sourceRoot)) {
      return;
    }
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      for (Path source : paths.filter(Files::isRegularFile).sorted().toList()) {
        Path relative = sourceRoot.relativize(source);
        Path target = safeResolve(targetRoot, relative.toString());
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  private static Map<String, List<SharedFixtureEntry>> loadIndex() {
    if (!Files.exists(INDEX)) {
      return Map.of();
    }

    Map<String, List<SharedFixtureEntry>> result = new LinkedHashMap<>();
    Set<String> seenOutputs = new LinkedHashSet<>();
    try {
      List<String> lines = Files.readAllLines(INDEX, StandardCharsets.UTF_8);
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        if (line.isBlank() || line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split("\\t", 5);
        if (parts.length != 5) {
          throw new IllegalStateException("Invalid shared fixture index row " + (i + 1));
        }

        SharedFixtureEntry entry =
            new SharedFixtureEntry(parts[0], parts[1], parts[2], parts[3], parts[4]);
        validateEntry(entry, i + 1);

        String outputKey =
            entry.concreteCd()
                + ":"
                + entry.role()
                + ":"
                + normalizeRelative(entry.output());
        if (!seenOutputs.add(outputKey)) {
          throw new IllegalStateException("Duplicate shared fixture output in row " + (i + 1));
        }
        result.computeIfAbsent(entry.concreteCd(), ignored -> new ArrayList<>()).add(entry);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read shared fixture index " + INDEX, e);
    }
    result.replaceAll((ignored, entries) -> List.copyOf(entries));
    return Map.copyOf(result);
  }

  private static void validateEntry(SharedFixtureEntry entry, int row) {
    safeResolve(RESOURCE_ROOT, entry.concreteCd());
    safeResolve(RESOURCE_ROOT, entry.shared());
    safeResolve(Path.of("unused"), entry.output());

    if (!entry.role().equals("adapter") && !entry.role().equals("concrete")) {
      throw new IllegalStateException("Invalid role in shared fixture index row " + row);
    }
    if (!PACKAGE_DECLARATION.matcher(entry.packageLine()).matches()) {
      throw new IllegalStateException("Invalid package declaration in shared fixture index row " + row);
    }
  }

  private static Path roleTarget(String role, Path adapterTarget, Path concreteTarget) {
    return role.equals("adapter") ? adapterTarget : concreteTarget;
  }

  private static String relativeConcreteCd(CDConcretizationTestCase testCase) {
    return RESOURCE_ROOT.relativize(testCase.concCd()).toString().replace('\\', '/');
  }

  private static Path safeResolve(Path root, String relativePath) {
    if (relativePath == null || relativePath.isBlank()) {
      throw new IllegalStateException("Fixture path must not be blank.");
    }
    Path normalizedRoot = root.normalize();
    Path resolved = normalizedRoot.resolve(relativePath.replace('/', java.io.File.separatorChar)).normalize();
    if (!resolved.startsWith(normalizedRoot)) {
      throw new IllegalStateException("Fixture path escapes root: " + relativePath);
    }
    return resolved;
  }

  private static String normalizeRelative(String path) {
    return safeResolve(Path.of("unused"), path).toString().replace('\\', '/');
  }

  private static void deleteRecursively(Path path) throws IOException {
    Path normalizedRoot = WORKSPACE_ROOT.normalize();
    Path normalizedPath = path.normalize();
    if (!normalizedPath.startsWith(normalizedRoot)) {
      throw new IllegalArgumentException("Refusing to delete outside fixture workspace: " + path);
    }
    if (!Files.exists(normalizedPath)) {
      return;
    }
    try (Stream<Path> paths = Files.walk(normalizedPath)) {
      for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(p);
      }
    }
  }

  private record SharedFixtureEntry(
      String concreteCd, String role, String shared, String output, String packageLine) {}
}
