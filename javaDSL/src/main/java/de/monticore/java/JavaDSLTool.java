/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.IOException;
import java.nio.file.Path;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLGlobalScope;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitor;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.prettyprint.JavaDSLPrettyPrinter;
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

    // execute default context conditions

    // execute a custom set of context conditions
    // TODO reimplement CoCos
//    Log.info("Running customized set of context conditions", JavaDSLTool.class.getName());
//    JavaDSLCoCoChecker customCoCos = new JavaDSLCoCoChecker();
//    customCoCos.checkAll(ast);

    // analyze the model with a visitor

    // execute a pretty printer
    JavaDSLPrettyPrinter pp = new JavaDSLPrettyPrinter(new IndentPrinter());
    Log.info("Pretty printing the parsed JavaDSL into console:", JavaDSLTool.class.getName());
    System.out.println(pp.prettyprint(ast));
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
  
}
