package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.utils.Constants;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.JavaSourcePostProcessor;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.compiler.VirtualFile;

/** Owns the Spoon model, its factory and the generated-source filesystem lifecycle. */
final class SpoonWorkspace {
  private final Map<String, Optional<CtTypeReference<?>>> parsedTypePrototypes =
      new LinkedHashMap<>();
  private File outputDirectory;
  private Launcher launcher;
  private CtModel model;

  void load(Path codePath) {
    try {
      JavaSourcePostProcessor.processDirectory(codePath);
    } catch (IOException e) {
      throw new IllegalStateException(
          "SpoonUpdater.setCodePath: Failed to prepare source code at '" + codePath + "'", e);
    }
    launcher = new Launcher();
    launcher.getEnvironment().setAutoImports(true);
    launcher.getEnvironment().setNoClasspath(true);
    launcher.addInputResource(codePath.toAbsolutePath().toString());
    launcher.buildModel();
    model = launcher.getModel();
    parsedTypePrototypes.clear();
  }

  CtModel model() {
    if (model == null) {
      throw new IllegalStateException("No Spoon model loaded; call setCodePath first");
    }
    return model;
  }

  Factory factory() {
    if (launcher == null) {
      throw new IllegalStateException("No Spoon workspace loaded; call setCodePath first");
    }
    return launcher.getFactory();
  }

  void setOutputDirectory(Path outputPath) {
    if (outputPath == null) {
      throw new IllegalArgumentException("Output path must not be null");
    }
    outputDirectory = outputPath.toFile();
  }

  Set<File> print() {
    if (outputDirectory == null) {
      throw new IllegalStateException("No output directory configured");
    }
    Path outputPath = outputDirectory.toPath();
    try {
      if (Files.isDirectory(outputPath)) {
        try (var paths = Files.walk(outputPath)) {
          for (Path source :
              paths.filter(Files::isRegularFile)
                  .filter(path -> path.toString().endsWith(".java"))
                  .toList()) {
            Files.delete(source);
          }
        }
      }
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Failed to clear stale Java sources before printing to " + outputPath, exception);
    }
    launcher.setSourceOutputDirectory(outputDirectory);
    launcher.prettyprint();
    return JavaLoader.readJavaFile(outputPath);
  }

  CtExpression<?> defaultExpression(String typeName) {
    String normalized = typeName == null ? "" : JavaSourceNames.normalizeType(typeName.trim());
    return switch (normalized) {
      case "boolean" -> factory().Code().createLiteral(false);
      case "byte" -> factory().Code().createLiteral((byte) 0);
      case "short" -> factory().Code().createLiteral((short) 0);
      case "int" -> factory().Code().createLiteral(0);
      case "long" -> factory().Code().createLiteral(0L);
      case "float" -> factory().Code().createLiteral(0.0f);
      case "double" -> factory().Code().createLiteral(0.0d);
      case "char" -> factory().Code().createLiteral('\0');
      default -> factory().Code().createLiteral(null);
    };
  }

  CtTypeReference<?> createTypeReference(String typeName) {
    if (typeName == null || typeName.isBlank()) {
      return factory().Type().createReference(Object.class);
    }
    String original = typeName.trim();
    Optional<CtTypeReference<?>> parsed = parseTypePrototype(original);
    if (parsed.isPresent()) {
      return parsed.get().clone();
    }
    String normalized = JavaSourceNames.normalizeType(original);
    CtTypeReference<?> primitive = primitiveTypeReference(normalized);
    if (primitive != null) {
      return primitive;
    }
    parsed = parseTypePrototype(normalized);
    if (parsed.isPresent()) {
      return parsed.get().clone();
    }
    try {
      return factory().Type().createReference(normalized);
    } catch (RuntimeException ignored) {
      return unresolvedTypeReference(normalized);
    }
  }

  void rewriteTypeReferenceName(CtTypeReference<?> reference, String newName) {
    if (reference == null || newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("Type reference and new name must be present");
    }
    String simpleName = JavaSourceNames.simpleName(newName);
    if (newName.contains(".")) {
      CtTypeReference<?> replacement = createTypeReference(newName);
      reference.setPackage(replacement.getPackage());
      reference.setDeclaringType(replacement.getDeclaringType());
    }
    // For a simple replacement, retain the original package identity. Turning entity.User into
    // an unresolved simple Professor produces an invalid import that cleanup can only remove.
    reference.setSimpleName(simpleName);
    reference.setSimplyQualified(true);
  }

  void clean(Path codePath) {
    Path sourcePath = codePath.toAbsolutePath().normalize();
    if (!Files.isDirectory(sourcePath)) {
      throw new IllegalArgumentException("Code path is not a directory: " + sourcePath);
    }
    Path parent = sourcePath.getParent();
    if (parent == null) {
      throw new IllegalArgumentException("Cannot transactionally clean a filesystem root");
    }
    Path transactionRoot = null;
    boolean preserveTransaction = false;
    try {
      transactionRoot =
          Files.createTempDirectory(parent, sourcePath.getFileName() + "_cleanup_");
      Path formattedPath = transactionRoot.resolve("formatted");
      Path stagedPath = transactionRoot.resolve("staged");
      Files.createDirectories(formattedPath);
      FileUtils.copyDirectory(sourcePath.toFile(), stagedPath.toFile());
      Map<Path, Path> originals = javaFilesByRelativePath(sourcePath);
      Launcher cleanupLauncher = cleanupLauncher(sourcePath);
      List<CtAnnotation<?>> annotations =
          new ArrayList<>(cleanupLauncher.getModel().getElements(new TypeFilter<>(CtAnnotation.class)));
      annotations.stream().filter(SpoonWorkspace::isAdaptAnnotation).forEach(CtAnnotation::delete);
      for (CtType<?> type : cleanupLauncher.getModel().getAllTypes()) {
        if (!type.isInterface()) {
          continue;
        }
        for (CtMethod<?> method : type.getMethods()) {
          if (!method.isDefaultMethod()
              && !method.hasModifier(ModifierKind.STATIC)
              && !method.hasModifier(ModifierKind.PRIVATE)) {
            method.setBody(null);
          }
        }
      }
      cleanupLauncher.setSourceOutputDirectory(formattedPath.toFile());
      cleanupLauncher.prettyprint();
      Set<Path> copiedTargets =
          copyFormattedFiles(formattedPath, stagedPath, sourcePath, originals);
      deleteRehomedOriginals(sourcePath, stagedPath, originals.values(), copiedTargets);
      JavaSourcePostProcessor.processDirectory(stagedPath);
      commitStagedDirectory(sourcePath, stagedPath, transactionRoot.resolve("backup"));
      Log.info("SpoonUpdater.cleanCode: Cleanup completed", "CodeAdapter");
    } catch (IOException | RuntimeException e) {
      if (transactionRoot != null && Files.exists(transactionRoot.resolve("backup"))) {
        preserveTransaction = true;
      }
      throw new IllegalStateException(
          "SpoonUpdater.cleanCode: Failed to clean code at '" + sourcePath + "'", e);
    } finally {
      if (transactionRoot != null && !preserveTransaction) {
        FileUtils.deleteQuietly(transactionRoot.toFile());
      }
    }
  }

  private Optional<CtTypeReference<?>> parseTypePrototype(String typeName) {
    return parsedTypePrototypes.computeIfAbsent(typeName, this::parseTypeReference);
  }

  private Optional<CtTypeReference<?>> parseTypeReference(String typeName) {
    try {
      Launcher parser = new Launcher();
      parser.getEnvironment().setNoClasspath(true);
      parser.addInputResource(
          new VirtualFile("class __TypeProbe { " + typeName + " value; }", "__TypeProbe.java"));
      parser.buildModel();
      return parser.getModel().getElements(new TypeFilter<>(CtField.class)).stream()
          .findFirst()
          .map(field -> field.getType().clone());
    } catch (RuntimeException ignored) {
      return Optional.empty();
    }
  }

  private CtTypeReference<?> primitiveTypeReference(String typeName) {
    return switch (typeName) {
      case "void" -> factory().Type().createReference(void.class);
      case "int" -> factory().Type().createReference(int.class);
      case "long" -> factory().Type().createReference(long.class);
      case "double" -> factory().Type().createReference(double.class);
      case "float" -> factory().Type().createReference(float.class);
      case "boolean" -> factory().Type().createReference(boolean.class);
      case "char" -> factory().Type().createReference(char.class);
      case "short" -> factory().Type().createReference(short.class);
      case "byte" -> factory().Type().createReference(byte.class);
      default -> null;
    };
  }

  private CtTypeReference<?> unresolvedTypeReference(String typeName) {
    CtTypeReference<?> reference = factory().Core().createTypeReference();
    String simpleName = JavaSourceNames.simpleName(typeName);
    reference.setSimpleName(simpleName.isBlank() ? "Object" : simpleName);
    reference.setPackage(null);
    reference.setDeclaringType(null);
    reference.setSimplyQualified(true);
    return reference;
  }

  private static Launcher cleanupLauncher(Path codePath) throws IOException {
    Launcher cleanupLauncher = new Launcher();
    cleanupLauncher.getEnvironment().setAutoImports(true);
    cleanupLauncher.getEnvironment().setNoClasspath(true);
    try (var paths = Files.walk(codePath)) {
      paths.filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> cleanupLauncher.addInputResource(path.toAbsolutePath().toString()));
    }
    cleanupLauncher.buildModel();
    return cleanupLauncher;
  }

  private static Map<Path, Path> javaFilesByRelativePath(Path codePath) throws IOException {
    Map<Path, Path> result = new LinkedHashMap<>();
    try (var paths = Files.walk(codePath)) {
      paths.filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> result.put(codePath.relativize(path), path));
    }
    return result;
  }

  private static Set<Path> copyFormattedFiles(
      Path formattedPath, Path stagedPath, Path sourcePath, Map<Path, Path> originals)
      throws IOException {
    Set<Path> copiedTargets = new LinkedHashSet<>();
    try (var paths = Files.walk(formattedPath)) {
      paths.filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(
              source -> {
                try {
                  Path relative = formattedPath.relativize(source);
                  Path originalTarget = originals.getOrDefault(relative, sourcePath.resolve(relative));
                  Path target = stagedPath.resolve(sourcePath.relativize(originalTarget));
                  Files.createDirectories(target.getParent());
                  copyReplacingWithRetry(source, target);
                  if (!copiedTargets.add(target.toAbsolutePath().normalize())) {
                    throw new IllegalStateException(
                        "Multiple formatted sources target the same file '" + target + "'");
                  }
                } catch (IOException e) {
                  throw new IllegalStateException("Failed to copy formatted file '" + source + "'", e);
                }
              });
    }
    return copiedTargets;
  }

  private static void deleteRehomedOriginals(
      Path sourcePath,
      Path stagedPath,
      Collection<Path> originals,
      Set<Path> copiedTargets)
      throws IOException {
    for (Path original : originals) {
      Path stagedOriginal = stagedPath.resolve(sourcePath.relativize(original));
      if (!copiedTargets.contains(stagedOriginal.toAbsolutePath().normalize())
          && Files.exists(stagedOriginal)) {
        Files.delete(stagedOriginal);
      }
    }
  }

  private static void commitStagedDirectory(Path source, Path staged, Path backup)
      throws IOException {
    moveDirectory(source, backup);
    try {
      moveDirectory(staged, source);
    } catch (IOException commitFailure) {
      try {
        moveDirectory(backup, source);
      } catch (IOException rollbackFailure) {
        commitFailure.addSuppressed(rollbackFailure);
      }
      throw commitFailure;
    }
    FileUtils.deleteDirectory(backup.toFile());
  }

  private static void moveDirectory(Path source, Path target) throws IOException {
    try {
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException ignored) {
      Files.move(source, target);
    }
  }

  private static void copyReplacingWithRetry(Path source, Path target) throws IOException {
    IOException lastException = null;
    for (int attempt = 0; attempt < 5; attempt++) {
      try {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return;
      } catch (IOException e) {
        lastException = e;
        try {
          Thread.sleep(50L * (attempt + 1));
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw e;
        }
      }
    }
    throw lastException;
  }

  private static boolean isAdaptAnnotation(CtAnnotation<?> annotation) {
    CtTypeReference<?> type = annotation.getAnnotationType();
    if (type != null) {
      String simple = type.getSimpleName();
      String qualified = type.getQualifiedName();
      if (Constants.ANNOT_PACKAGE.equals(qualified)
          || (Constants.ANNOT_NAME.equals(simple)
              && (qualified == null
                  || qualified.isBlank()
                  || Constants.ANNOT_NAME.equals(qualified)))) {
        return true;
      }
    }
    String rendered = annotation.toString().trim();
    return hasAnnotationPrefix(rendered, Constants.ANNOT_NAME)
        || hasAnnotationPrefix(rendered, Constants.ANNOT_PACKAGE);
  }

  private static boolean hasAnnotationPrefix(String rendered, String annotationName) {
    String prefix = "@" + annotationName;
    return rendered.equals(prefix)
        || rendered.startsWith(prefix + "(")
        || rendered.startsWith(prefix + "[");
  }
}
