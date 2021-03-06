/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java;

import java.io.IOException;
import java.util.Optional;

import org.antlr.v4.runtime.RecognitionException;

import de.monticore.io.paths.ModelPath;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.lang.JavaDSLLanguage;
import de.monticore.java.prettyprint.JavaDSLPrettyPrinter;
import de.monticore.java.symboltable.JavaSymbolTableCreator;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

/**
 * Main class for the MyDSL tool.
 *
 */
public class JavaDSLTool {
  
  /**
   * Use the single argument for specifying the single input model file.
   *
   * @param args
   */
  public static void main(String[] args) {
    Log.enableFailQuick(false);
    if (args.length != 1) {
      Log.error("Please specify only one single path to the input model.");
      return;
    }
    Log.info("JavaDSL Tool", JavaDSLTool.class.getName());
    Log.info("----------", JavaDSLTool.class.getName());
    String model = args[0];
    
    // setup the language infrastructure
    final JavaDSLLanguage lang = new JavaDSLLanguage();
    
    // parse the model and create the AST representation
    final ASTCompilationUnit ast = parse(model, lang.getParser());
    Log.info(model + " parsed successfully!", JavaDSLTool.class.getName());
    
    // setup the symbol table
    createSymbolTable(lang, ast);
    
    // execute default context conditions
    
    // execute a custom set of context conditions
    Log.info("Running customized set of context conditions",
        JavaDSLTool.class.getName());
    JavaDSLCoCoChecker customCoCos = new JavaDSLCoCoChecker();
    customCoCos.checkAll(ast);
    
    // analyze the model with a visitor
    
    // execute a pretty printer
    JavaDSLPrettyPrinter pp = new JavaDSLPrettyPrinter(new IndentPrinter());
    Log.info("Pretty printing the parsed JavaDSL into console:", JavaDSLTool.class.getName());
    System.out.println(pp.prettyprint(ast));
  }
  
  /**
   * Parse the model contained in the specified file.
   *
   * @param model - file to parse
   * @param parser
   * @return
   */
  public static ASTCompilationUnit parse(String model,
      JavaDSLParser parser) {
    try {
      Optional<ASTCompilationUnit> optModel = parser.parse(model);
      
      if (!parser.hasErrors() && optModel.isPresent()) {
        return optModel.get();
      }
      Log.error("Model could not be parsed.");
    }
    catch (RecognitionException | IOException e) {
      Log.error("Failed to parse " + model, e);
    }
    return null;
  }
  
  /**
   * Create the symbol table from the parsed AST.
   *
   * @param lang
   * @param ast
   * @return
   */
  public static Scope createSymbolTable(JavaDSLLanguage lang,
      ASTCompilationUnit ast) {
    final ResolvingConfiguration resolvingConfiguration = new ResolvingConfiguration();
    resolvingConfiguration.addDefaultFilters(lang.getResolvingFilters());
    
    GlobalScope globalScope = new GlobalScope(new ModelPath(), lang, resolvingConfiguration);
    
    Optional<JavaSymbolTableCreator> symbolTable = lang
        .getSymbolTableCreator(resolvingConfiguration, globalScope);
    return symbolTable.get().createFromAST(ast);
  }
  
}
