package de.monticore.codeAdaption;

import de.monticore.CD4CodeTool;
import de.monticore.cd4code._prettyprint.CD4CodeFullPrettyPrinter;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.prettyprint.IndentPrinter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/** Generates a concrete Java baseline from the final class-diagram snapshot. */
final class ConcreteCodeGenerationService {

  /**
   * Generates Java into an isolated workspace while treating {@code stagedHwcRoot} as authoritative
   * handwritten code.
   *
   * @return the root containing the generated Java sources
   */
  Path generate(
      ASTCDCompilationUnit finalConcreteCD, Path stagedHwcRoot, Path generationWorkspace) {
    Objects.requireNonNull(finalConcreteCD, "finalConcreteCD");
    Path normalizedHwc = requireDirectory(stagedHwcRoot, "staged handwritten code");
    Path normalizedWorkspace = normalize(generationWorkspace, "generationWorkspace");
    rejectOverlappingTrees(normalizedHwc, normalizedWorkspace);

    Path modelDirectory = normalizedWorkspace.resolve("model");
    Path generatedSourceRoot = normalizedWorkspace.resolve("generated");
    try {
      Files.createDirectories(modelDirectory);
      Files.createDirectories(generatedSourceRoot);
    } catch (IOException exception) {
      throw new CodeAdaptationException(
          "Could not initialize isolated code-generation workspace", exception);
    }

    String diagramName = finalConcreteCD.getCDDefinition().getName();
    Path modelFile = modelDirectory.resolve(diagramName + ".cd");
    String serializedModel =
        new CD4CodeFullPrettyPrinter(new IndentPrinter()).prettyprint(finalConcreteCD);
    try {
      Files.writeString(modelFile, serializedModel, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new CodeAdaptationException(
          "Could not serialize the final concrete class diagram", exception);
    }

    String[] arguments = {
      "-i",
      modelFile.toString(),
      "-ct",
      "cd2java.CD2Java",
      "--gen",
      "-hwc",
      normalizedHwc.toString(),
      "-o",
      generatedSourceRoot.toString(),
      "--fieldfromrole",
      "navigable"
    };
    runGenerator(arguments, normalizedWorkspace);
    return generatedSourceRoot;
  }

  /** Runs CD4Code in a separate JVM so its global mills and logger cannot affect the caller. */
  void runGenerator(String[] arguments, Path generationWorkspace) {
    Path classpathJar = createClasspathJar(generationWorkspace);
    List<String> command = new ArrayList<>();
    command.add(javaExecutable().toString());
    command.add("-cp");
    command.add(classpathJar.toString());
    command.add(CD4CodeGenerationMain.class.getName());
    command.addAll(List.of(arguments));

    Process process = null;
    try {
      process = new ProcessBuilder(command).redirectErrorStream(true).start();
      String output =
          new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new CodeAdaptationException(
            "CD4Code generation failed with exit code "
                + exitCode
                + ": "
                + (output.isBlank() ? "no diagnostic was provided" : output));
      }
    } catch (InterruptedException exception) {
        process.destroyForcibly();
        Thread.currentThread().interrupt();
      throw new CodeAdaptationException("CD4Code generation was interrupted", exception);
    } catch (IOException exception) {
      throw new CodeAdaptationException("Could not start isolated CD4Code generation", exception);
    }
  }

  private static Path createClasspathJar(Path generationWorkspace) {
    Set<URI> entries = new LinkedHashSet<>();
    String configuredClasspath = System.getProperty("java.class.path", "");
    for (String entry : configuredClasspath.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
      if (!entry.isBlank()) {
        entries.add(Path.of(entry).toAbsolutePath().normalize().toUri());
      }
    }
    for (ClassLoader loader = Thread.currentThread().getContextClassLoader();
        loader != null;
        loader = loader.getParent()) {
      if (loader instanceof URLClassLoader urlClassLoader) {
        for (URL url : urlClassLoader.getURLs()) {
          if ("file".equalsIgnoreCase(url.getProtocol())) {
            try {
              entries.add(url.toURI());
            } catch (java.net.URISyntaxException exception) {
              throw new CodeAdaptationException(
                  "Invalid runtime classpath entry for isolated CD4Code generation: " + url,
                  exception);
            }
          }
        }
      }
    }
    addCodeSource(entries, ConcreteCodeGenerationService.class);
    addCodeSource(entries, CD4CodeTool.class);

    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, joinUris(entries));
    Path classpathJar = generationWorkspace.resolve("cd4code-classpath.jar");
    try (JarOutputStream ignored =
        new JarOutputStream(Files.newOutputStream(classpathJar), manifest)) {
      return classpathJar;
    } catch (IOException exception) {
      throw new CodeAdaptationException(
          "Could not prepare the isolated CD4Code runtime classpath", exception);
    }
  }

  private static void addCodeSource(Set<URI> entries, Class<?> type) {
    if (type.getProtectionDomain() != null
        && type.getProtectionDomain().getCodeSource() != null) {
      try {
        entries.add(type.getProtectionDomain().getCodeSource().getLocation().toURI());
      } catch (java.net.URISyntaxException exception) {
        throw new CodeAdaptationException(
            "Invalid code source for isolated CD4Code generation: " + type.getName(), exception);
      }
    }
  }

  private static String joinUris(Set<URI> entries) {
    return entries.stream().map(URI::toASCIIString).reduce((a, b) -> a + " " + b).orElse("");
  }

  private static Path javaExecutable() {
    String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
        ? "java.exe"
        : "java";
    Path java = Path.of(System.getProperty("java.home"), "bin", executable);
    if (!Files.isRegularFile(java)) {
      throw new CodeAdaptationException("Java executable not found: " + java);
    }
    return java;
  }

  private static Path requireDirectory(Path path, String label) {
    Path normalized = normalize(path, label);
    if (!Files.isDirectory(normalized)) {
      throw new CodeAdaptationException(label + " is not a directory: " + normalized);
    }
    return normalized;
  }

  private static Path normalize(Path path, String label) {
    return Objects.requireNonNull(path, label).toAbsolutePath().normalize();
  }

  private static void rejectOverlappingTrees(Path hwcRoot, Path generationWorkspace) {
    if (hwcRoot.equals(generationWorkspace)
        || hwcRoot.startsWith(generationWorkspace)
        || generationWorkspace.startsWith(hwcRoot)) {
      throw new CodeAdaptationException(
          "Generation workspace must be isolated from handwritten code: "
              + generationWorkspace
              + " and "
              + hwcRoot);
    }
  }
}
