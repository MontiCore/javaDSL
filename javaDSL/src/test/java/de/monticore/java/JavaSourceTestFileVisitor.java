/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.monticore.java.javadsl._parser.JavaDSLParser;
import org.antlr.v4.runtime.RecognitionException;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.se_rwth.commons.logging.Log;

class ParseJavaFileVisitor implements FileVisitor<Path> {

  private final List<Path> ignorePathes;

  List<Path> fails = new ArrayList<>();

  private int numberOfTests = 0;

  private int failingCocos = 0;

  private int failingClasses = 0;

  private int successCount = 0;

  private int failCount = 0;

  private ASTCompilationUnit optModel;

  @SafeVarargs
  public ParseJavaFileVisitor(List<String[]>... ignoreLists) {
    numberOfTests = 0;
    successCount = 0;
    failCount = 0;

    // join all ignoreLists into one ignoreList
    List<String[]> ignoreList = new ArrayList<>();
    for (List<String[]> list : ignoreLists) {
      ignoreList.addAll(list);
    }

    // convert representation from String[] to path
    FileSystem fileSystem = FileSystems.getDefault();
    ignorePathes = ignoreList.stream()
        .map(it -> fileSystem.getPath("", it)).collect(Collectors.toList());
  }

  public int getNumberOfTests() {
    return this.numberOfTests;
  }

  public int getSuccessCount() {
    return this.successCount;
  }

  public int getFailCount() {
    return this.failCount;
  }

  public int getFailCocosCount() {
    return this.failingCocos;
  }

  public int getFailClassesCount() {
    return this.failingClasses;
  }

  public double getSuccessRate() {
    if (this.numberOfTests == 0) {
      return 0;
    }
    else {
      return ((double) this.successCount) / ((double) this.numberOfTests);
    }
  }

  public double getFailRate() {
    if (this.numberOfTests == 0) {
      return 0;
    }
    else {
      return ((double) this.failCount) / ((double) this.numberOfTests);
    }
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
    String model = file.toAbsolutePath().toString();
    if (model.endsWith(".java") && !ignorePathes.contains(file)) {
      Log.info("Parsing " + model, ParseJavaFileVisitor.class.getName());
      try {
        String parentDirectory = "target/generated-test-resources/JDK";
        String temp = file.toString().replace('\\', '/');
        String modelPath = temp.substring(parentDirectory.length() + 1, temp.lastIndexOf("."));
        optModel = parse(parentDirectory, modelPath);

        if (optModel != null) {
          successCount++;

        }
        else {
          Log.error("Failed to parse " + model);
          fails.add(file);
          failCount++;
        }
      }
      catch (Exception e) {
        Log.error("Failed to parse " + model);
        fails.add(file);
        failCount++;
      }
      numberOfTests++;
      if(optModel != null) {
        try {
          Log.getFindings().clear();
          // TODO renenable checks
//          JavaDSLCoCoChecker checker = new JavaDSLTypeChecker().getAllTypeChecker();
//          checker.checkAll(optModel);
          if (Log.getFindings().size()>0) {
            failingClasses++;
            failingCocos = failingCocos + Log.getFindings().size();
          }
        } catch (Exception ignored) {}
      }
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    return FileVisitResult.CONTINUE;
  }

  private ASTCompilationUnit parse(String parentDirectory, String modelPath) throws IOException {
    try {
      String pathToModel = parentDirectory + File.pathSeparator + modelPath + ".java";

      JavaDSLParser parser = new JavaDSLParser();
      Optional<ASTCompilationUnit> optCompilationUnit;
      Throwable cause = null;

      try {
        optCompilationUnit = parser.parse(pathToModel);
      } catch (IOException e) {
        optCompilationUnit = Optional.empty();
        cause = e;
      }

      if (optCompilationUnit.isEmpty() || parser.hasErrors()) {
        throw new IOException("Failed to parse model: " + pathToModel, cause);
      }

      // TODO populate symbol table
      return optCompilationUnit.get();
    }
    catch (RecognitionException e) {
      return null;
    }
  }
}
