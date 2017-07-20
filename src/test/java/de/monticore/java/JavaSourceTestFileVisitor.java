/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.java;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.RecognitionException;

import de.monticore.ModelingLanguageFamily;
import de.monticore.io.paths.ModelPath;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.symboltable.JavaSymbolTableCreator;
import de.monticore.java.types.JavaDSLTypeChecker;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.se_rwth.commons.logging.Log;

class ParseJavaFileVisitor implements FileVisitor<Path> {
  
  private final List<Path> ignorePathes;
  
  List<Path> fails = new ArrayList<Path>();
  
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
    List<String[]> ignoreList = new ArrayList<String[]>();
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
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    return FileVisitResult.CONTINUE;
  }
  
  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    String model = file.toAbsolutePath().toString();
    if (model.endsWith(".java") && !ignorePathes.contains(file)) {
      Log.info("Parsing " + model, ParseJavaFileVisitor.class.getName());
      try {
        String parentDirectory = "src/test/resources/JDK";
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
          JavaDSLCoCoChecker checker = new JavaDSLTypeChecker().getAllTypeChecker();
          checker.checkAll(optModel);
          if (Log.getFindings().size()>0) {
            failingClasses++;
            failingCocos = failingCocos + Log.getFindings().size();
          }
        } catch(Exception e) {
          
        }
      }
    }
    return FileVisitResult.CONTINUE;
  }
  
  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }
  
  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }
  
  private ASTCompilationUnit parse(String parentDirectory, String modelPath) {
    try {
      Path path = FileSystems.getDefault().getPath(parentDirectory);
      ModelPath parentModelPath = new ModelPath(path);
      de.monticore.java.lang.JavaDSLLanguage javaDSLLanguage = new de.monticore.java.lang.JavaDSLLanguage();
      ASTCompilationUnit astCompilationUnit = javaDSLLanguage.getModelLoader()
          .loadModel(modelPath, parentModelPath).get();
      
      ModelingLanguageFamily modelingLanguageFamily = new ModelingLanguageFamily();
      modelingLanguageFamily.addModelingLanguage(javaDSLLanguage);
      GlobalScope globalScope = new GlobalScope(parentModelPath, modelingLanguageFamily);
      
      JavaSymbolTableCreator symbolTableCreator = javaDSLLanguage.getSymbolTableCreator(
          new ResolvingConfiguration(), globalScope).get();
      symbolTableCreator.setArtifactName(modelPath);
      astCompilationUnit.accept(symbolTableCreator);
      return astCompilationUnit;
    }
    catch (RecognitionException e) {
      return null;
    }
  }
}
