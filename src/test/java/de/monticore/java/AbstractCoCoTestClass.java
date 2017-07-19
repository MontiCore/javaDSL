/*
 * ******************************************************************************
 * MontiCore Language Workbench
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.RecognitionException;

import de.monticore.ModelingLanguageFamily;
import de.monticore.cocos.helper.Assert;
import de.monticore.io.paths.ModelPath;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.symboltable.JavaSymbolTableCreator;
import de.monticore.java.types.JavaDSLTypeChecker;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author$$
 * @since TODO
 */
public class AbstractCoCoTestClass {
  
  JavaDSLCoCoChecker checker = new JavaDSLTypeChecker().getAllTypeChecker();

  public AbstractCoCoTestClass()
  {

  }


  protected void testModelForErrors(String parentDirectory, String modelPath,JavaDSLCoCoChecker checker, Collection<Finding> expectedErrors) {

    ASTCompilationUnit root = parse(parentDirectory, modelPath);
    checker.checkAll(root);
    Collection<Finding> errors = Log.getFindings().stream().filter(f -> f.isError()).collect(
        Collectors.toList());
    Assert.assertEqualErrorCounts(expectedErrors, errors);
    Assert.assertErrorMsg(expectedErrors, errors);
  }

  protected void testModelForWarning(String parentDirectory, String modelPath,JavaDSLCoCoChecker checker, Collection<Finding> expectedWarnings) {

    ASTCompilationUnit root = parse(parentDirectory, modelPath);
    checker.checkAll(root);
    Collection<Finding> warnings = Log.getFindings().stream().filter(f -> f.isWarning()).collect(
        Collectors.toList());
    Assert.assertEqualErrorCounts(expectedWarnings, warnings);
    Assert.assertErrorMsg(expectedWarnings, warnings);
  }

  protected void testModelNoErrors(String parentDirectory, String modelPath, JavaDSLCoCoChecker checker) {

    ASTCompilationUnit root = parse( parentDirectory,  modelPath);
    checker.checkAll(root);
    assertEquals(0,Log.getFindings().stream().filter(f->f.isError()).count());

  }
  
  protected void testModelForErrors(String parentDirectory, String modelPath, Collection<Finding> expectedErrors) {

    ASTCompilationUnit root = parse(parentDirectory, modelPath);
    checker.checkAll(root);
    Collection<Finding> errors = Log.getFindings().stream().filter(f -> f.isError()).collect(
        Collectors.toList());
    Assert.assertEqualErrorCounts(expectedErrors, errors);
    Assert.assertErrorMsg(expectedErrors, errors);
  }

  protected void testModelForWarning(String parentDirectory, String modelPath, Collection<Finding> expectedWarnings) {

    ASTCompilationUnit root = parse(parentDirectory, modelPath);
    checker.checkAll(root);
    Collection<Finding> warnings = Log.getFindings().stream().filter(f -> f.isWarning()).collect(
        Collectors.toList());
    Assert.assertEqualErrorCounts(expectedWarnings, warnings);
    Assert.assertErrorMsg(expectedWarnings, warnings);
  }

  protected void testModelNoErrors(String parentDirectory, String modelPath) {

    ASTCompilationUnit root = parse( parentDirectory,  modelPath);
    checker.checkAll(root);
    assertEquals(0,Log.getFindings().stream().filter(f->f.isError()).count());

  }

  protected ASTCompilationUnit parse(String parentDirectory, String modelPath) {
    String completePath = parentDirectory + File.separatorChar + modelPath + ".java";
    try {
      Log.debug("Parsing " + completePath, getClass().getName());
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
      e.printStackTrace();
    }
    throw new RuntimeException("Error during loading of model " + completePath + ".");

  }
}

