package de.monticore.java.utils;

import com.google.common.base.Stopwatch;
import de.monticore.class2mc.Class2MCResolver;
import de.monticore.class2mc.OOClass2MCResolver;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._symboltable.*;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JavaDSLSymbolTableUtil {
  
  public static void prepareMill(boolean enableC2MC) {
    JavaDSLMill.globalScope().clear();
    
    BasicSymbolsMill.initializePrimitives();
    
    if (enableC2MC) {
      OOClass2MCResolver resolver = new OOClass2MCResolver();
      JavaDSLMill.globalScope().addAdaptedTypeSymbolResolver(resolver);
      JavaDSLMill.globalScope().addAdaptedOOTypeSymbolResolver(resolver);
    }
  }
  
  /**
   * Builds the symbol table of a single model. Use {@link #buildSymbolTable(List)} for models
   * that refer to each other, as each phase has to be completed for all models before the next
   * one starts.
   */
  public static IJavaDSLArtifactScope buildSymbolTable(ASTCompilationUnit ast) {
    return buildSymbolTable(List.of(ast)).get(0);
  }

  /** Builds the symbol tables of the models in three phases. */
  public static List<IJavaDSLArtifactScope> buildSymbolTable(List<ASTCompilationUnit> asts) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    Log.info("Build - Phase 1", "SymbolTableConstruction");
    List<IJavaDSLArtifactScope> as =
        asts.stream().map(JavaDSLSymbolTableUtil::runSymTabGenitor).collect(Collectors.toList());
    Log.info("Build - Phase 1 finished in " + stopwatch.elapsed(TimeUnit.MILLISECONDS)+"ms", "SymbolTableConstruction");
    stopwatch.reset().start();
    Log.info("Build - Phase 2", "SymbolTableConstruction");
    asts.forEach(JavaDSLSymbolTableUtil::runSymTabCompleter);
    Log.info("Build - Phase 2 finished in " + stopwatch.elapsed(TimeUnit.MILLISECONDS)+"ms", "SymbolTableConstruction");
    stopwatch.reset().start();
    Log.info("Build - Phase 3", "SymbolTableConstruction");
    asts.forEach(JavaDSLSymbolTableUtil::runSymTabFinalization);
    Log.info("Build - Phase 3 finished in " + stopwatch.elapsed(TimeUnit.MILLISECONDS)+"ms", "SymbolTableConstruction");
    return as;
  }
  
  public static IJavaDSLArtifactScope runSymTabGenitor(ASTCompilationUnit ast) {
    JavaDSLScopesGenitorDelegator genitor = JavaDSLMill.scopesGenitorDelegator();
    return genitor.createFromAST(ast);
  }
  
  public static void runSymTabCompleter(ASTCompilationUnit ast) {
    JavaDSLScopesGenitorP2Delegator p2Genitor = JavaDSLMill.scopesGenitorP2Delegator();
    p2Genitor.createFromAST(ast);
  }
  
  public static void runSymTabFinalization(ASTCompilationUnit ast) {
    JavaDSLScopesGenitorP3Delegator p3Genitor = JavaDSLMill.scopesGenitorP3Delegator();
    p3Genitor.createFromAST(ast);
  }
}
