package de.monticore.codeAdaption.testutil;

import de.monticore.CD4CodeTool;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import spoon.Launcher;
import spoon.reflect.code.CtComment;
import spoon.reflect.visitor.filter.TypeFilter;

/** Generates CD4Code fixtures used by evaluation tests. */
public final class CD4CodeTestGenerator {

  private CD4CodeTestGenerator() {}

  public static void generate(File cdFile, Path hwcPath, Path output) {
    String[] input = {
      "-i",
      cdFile.getAbsolutePath(),
      "-ct",
      "cd2java.CD2Java",
      "--gen",
      "-hwc",
      hwcPath.toString(),
      "-o",
      output.toString(),
      "--fieldfromrole",
      "navigable"
    };
    CD4CodeTool.main(input);

    JavaLoader.readJavaFile(output).forEach(CD4CodeTestGenerator::removeComments);
  }

  private static void removeComments(File file) {
    Path tempDir = null;
    try {
      tempDir = Files.createTempDirectory("ref-code-adaptation-comments");
      Launcher launcher = new Launcher();
      launcher.getEnvironment().setNoClasspath(true);
      launcher.addInputResource(file.getAbsolutePath());
      launcher.buildModel();
      List<CtComment> comments =
          new ArrayList<>(launcher.getModel().getElements(new TypeFilter<>(CtComment.class)));
      comments.forEach(CtComment::delete);
      launcher.setSourceOutputDirectory(tempDir.toFile());
      launcher.prettyprint();

      Optional<Path> generated;
      try (var paths = Files.walk(tempDir)) {
        generated =
            paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(file.getName()))
                .findFirst();
      }
      if (generated.isPresent()) {
        Files.copy(generated.get(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } else {
        Log.warn("Spoon did not produce a rewritten file for " + file.getAbsolutePath());
      }
    } catch (Exception exception) {
      Log.error("It was not possible to remove comments from " + file.getAbsolutePath(), exception);
    } finally {
      deleteTemporaryDirectory(tempDir);
    }
  }

  private static void deleteTemporaryDirectory(Path tempDir) {
    if (tempDir == null) {
      return;
    }
    try (var paths = Files.walk(tempDir)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(path);
      }
    } catch (IOException exception) {
      Log.warn("Could not delete temporary comment-cleanup directory " + tempDir);
    }
  }
}
