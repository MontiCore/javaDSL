/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

import de.se_rwth.commons.logging.LogStub;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Before;

import de.monticore.ModelingLanguageFamily;
import de.monticore.cocos.helper.Assert;
import de.monticore.io.paths.ModelPath;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.lang.JavaDSLLanguage;
import de.monticore.java.symboltable.JavaSymbolTableCreator;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.Slf4jLog;

/**
 * A simple test for MyDSL tool.
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 */
public abstract class AbstractTestClass {
  
  JavaDSLParser parser;
  
  // helper methods
  static void printScope(Scope scope) {
    printScope(scope, 0);
  }
  
  protected static void printScope(Scope scope, int indent) {
    String bracket = scope.getName().orElse("") + " - " + scope.getClass().getSimpleName();
    String s = "";
    for (int i = 0; i < indent; i++) {
      s += "|   ";
    }
    System.out.println(s + "/ " + bracket);
    for (Entry<String, Collection<Symbol>> entry : scope.getLocalSymbols().entrySet()) {
      for (Symbol symbol : entry.getValue()) {
        System.out.println(s + "|   " + scope.getClass().getSimpleName() + " has a Symbol: "
            + symbol.getFullName());
      }
    }
    for (Scope subScope : scope.getSubScopes()) {
      printScope(subScope, indent + 1);
    }
    System.out.println(s + "\\ " + bracket);
  }
  
  @Before
  public void setup() {
    LogStub.init();
    Log.enableFailQuick(false);
    parser = new JavaDSLParser();
  }
  
  protected void testParsabilityOfModel(String parentDirectory, String modelPath) {
    String completePath = parentDirectory + File.separatorChar + modelPath + ".java";
    Log.debug("Parsing " + completePath, getClass().getName());
    Optional<ASTCompilationUnit> optionalModel;
    try {
      optionalModel = parser.parse(completePath);
      assertTrue(!parser.hasErrors() && optionalModel.isPresent());
    }
    catch (RecognitionException | IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
  
  protected void testUnparsabilityOfModel(String model) throws RecognitionException, IOException {
    Log.debug("Parsing " + model, getClass().getName());
    Optional<ASTCompilationUnit> optionalModel;
    try {
      optionalModel = parser.parse(model);
    } catch (Exception e) {
      optionalModel = Optional.ofNullable(null);
    }
    assertFalse(!parser.hasErrors() && optionalModel.isPresent());
    Log.debug("Unparsability Test: At least one error is expected!", getClass().getName());
  }
  
  protected void testCoco(String parentDirectory, String modelPath,
      JavaDSLCoCoChecker javaCoCoChecker, String... errorMessages) throws RecognitionException,
          IOException {
    ASTCompilationUnit ast = parse(parentDirectory, modelPath);
    
    javaCoCoChecker.checkAll(ast);
    
    ArrayList<Finding> expectedErrors = new ArrayList<>();
    for (String errorMessage : errorMessages) {
      expectedErrors.add(Finding.warning(errorMessage));
    }
    Assert.assertErrors(expectedErrors, Log.getFindings());
  }
  
  protected ASTCompilationUnit parse(String parentDirectory, String modelPath)
      throws RecognitionException, IOException {
    String completePath = parentDirectory + File.separatorChar + modelPath + ".java";
    
    Log.debug("Parsing " + completePath, getClass().getName());
    Path path = FileSystems.getDefault().getPath(parentDirectory);
    ModelPath parentModelPath = new ModelPath(path);
    JavaDSLLanguage javaDSLLanguage = new JavaDSLLanguage();
    ASTCompilationUnit astCompilationUnit = javaDSLLanguage.getModelLoader()
        .loadModel(modelPath, parentModelPath).get();
        
    ModelingLanguageFamily modelingLanguageFamily = new ModelingLanguageFamily();
    modelingLanguageFamily.addModelingLanguage(javaDSLLanguage);
    GlobalScope globalScope = new GlobalScope(parentModelPath, modelingLanguageFamily);
    
    JavaSymbolTableCreator symbolTableCreator = javaDSLLanguage.getSymbolTableCreator(
        new ResolvingConfiguration(), globalScope).get();
    symbolTableCreator.setArtifactName(modelPath);
    astCompilationUnit.accept(symbolTableCreator);
    // printScope(globalScope);
    return astCompilationUnit;
  }
  
  protected Optional<Symbol> createSymbol(String parentDirectory, String modelPath)
      throws RecognitionException, IOException {
    String completePath = parentDirectory + File.separatorChar + modelPath + ".java";
    
    Log.debug("Parsing " + completePath, getClass().getName());
    Path path = FileSystems.getDefault().getPath(parentDirectory);
    ModelPath parentModelPath = new ModelPath(path);
    JavaDSLLanguage javaDSLLanguage = new JavaDSLLanguage();
    ASTCompilationUnit astCompilationUnit = javaDSLLanguage.getModelLoader()
        .loadModel(modelPath, parentModelPath).get();
        
    ResolvingConfiguration resolvingConfiguration = new ResolvingConfiguration();
    resolvingConfiguration.addDefaultFilters(javaDSLLanguage.getResolvers());
    GlobalScope globalScope = new GlobalScope(parentModelPath, javaDSLLanguage,
        resolvingConfiguration);
    Optional<JavaSymbolTableCreator> stc = javaDSLLanguage
        .getSymbolTableCreator(resolvingConfiguration, globalScope);
    if (stc.isPresent()) {
      stc.get().createFromAST(astCompilationUnit);
    }
    return globalScope.resolve(modelPath, JavaTypeSymbol.KIND);
  }
}
