/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import de.monticore.cd.codegen.CDGenerator;
import de.monticore.cd.codegen.CdUtilsPrinter;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.TemplateController;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.java.java2cd.Java2CDConverter;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLGlobalScope;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitor;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.Log;

public class JavaDSLTool {

  public static void main(String[] args) {
    Log.enableFailQuick(false);
    if (args.length != 1) {
      Log.error("Please specify only one single path to the input model.");
      return;
    }
    Log.info("JavaDSL Tool", JavaDSLTool.class.getName());
    Log.info("----------", JavaDSLTool.class.getName());
    String model = args[0];

    // parse the model and create the AST representation
    final ASTCompilationUnit ast = loadArtifact(Path.of(model));
    Log.info(model + " parsed successfully!", JavaDSLTool.class.getName());

    generateCD(ast);

    // execute default context conditions

    // execute a custom set of context conditions
    // TODO reimplement CoCos
//    Log.info("Running customized set of context conditions", JavaDSLTool.class.getName());
//    JavaDSLCoCoChecker customCoCos = new JavaDSLCoCoChecker();
//    customCoCos.checkAll(ast);

    // analyze the model with a visitor

    // execute a pretty printer
    Log.info("Pretty printing the parsed JavaDSL into console:", JavaDSLTool.class.getName());
    System.out.println(JavaDSLMill.prettyPrint(ast, true));
  }

  public static ASTCompilationUnit loadArtifact(Path source) {
    IJavaDSLGlobalScope globalScope = JavaDSLMill.globalScope();

    JavaDSLParser parser = JavaDSLMill.parser();
    ASTCompilationUnit compilationUnit;

    try {
      compilationUnit = parser.parseCompilationUnit(source.toAbsolutePath().toString()).orElseThrow(NullPointerException::new);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    JavaDSLScopesGenitor genitor = JavaDSLMill.scopesGenitor();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();

    traverser.setJavaDSLHandler(genitor);
    traverser.add4JavaDSL(genitor);
    genitor.putOnStack(globalScope);

    IJavaDSLArtifactScope artifactScope = genitor.createFromAST(compilationUnit);
    globalScope.addSubScope(artifactScope);

    return compilationUnit;
  }


  public static void generateCD(ASTCompilationUnit ast) {
    String outputDir = "target/gen-test/";
    GeneratorSetup setup = new GeneratorSetup();
    GlobalExtensionManagement glex = new GlobalExtensionManagement();
    setup.setGlex(glex);
    glex.setGlobalValue("cdPrinter", new CdUtilsPrinter());

    File targetDir = new File(outputDir);
    setup.setOutputDirectory(targetDir);

    String configTemplate = "java2cd.Java2CD";
    TemplateController tc = setup.getNewTemplateController(configTemplate);
    CDGenerator generator = new CDGenerator(setup);
    TemplateHookPoint hpp = new TemplateHookPoint(configTemplate);
    List<Object> configTemplateArgs;
    // select the conversion variant:
    Java2CDConverter converter = new Java2CDConverter();
    configTemplateArgs = Arrays.asList(glex, converter, setup.getHandcodedPath(), generator);

    hpp.processValue(tc, ast, configTemplateArgs);
  }


}
