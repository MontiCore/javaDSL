package de.monticore.codeAdaption.utils;

import de.monticore.CD4CodeTool;
import java.io.File;
import java.nio.file.Path;

public class Generator {

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

    // delete hook-points
    JavaLoader.readJavaFile(Path.of("target")).forEach(AdapterUtils::removeComments);
  }
}
