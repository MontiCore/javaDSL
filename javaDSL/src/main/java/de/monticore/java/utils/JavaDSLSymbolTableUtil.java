package de.monticore.java.utils;

import de.monticore.class2mc.Class2MCResolver;
import de.monticore.class2mc.OOClass2MCResolver;
import de.monticore.expressions.lambdaexpressions._symboltable.LambdaExpressionsSTCompleteTypes2;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._symboltable.*;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._symboltable.JavaLightSTCompleteTypes;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.typeparameters._symboltable.TypeParametersSTCompleteTypes;

import java.util.List;
import java.util.stream.Collectors;

public class JavaDSLSymbolTableUtil {
  public static void prepareMill(boolean enableC2MC) {
    JavaDSLMill.globalScope().clear();
    
    BasicSymbolsMill.initializePrimitives();
    
    if (enableC2MC) {
      Class2MCResolver resolver = new OOClass2MCResolver();
      JavaDSLMill.globalScope().addAdaptedTypeSymbolResolver(resolver);
    }
  }
  
  public static IJavaDSLArtifactScope buildSymbolTable(ASTCompilationUnit ast) {
    IJavaDSLArtifactScope as = runSymTabGenitor(ast);
    runSymTabCompleter(ast);
    return as;
  }
  
  public static List<IJavaDSLArtifactScope> buildSymbolTable(List<ASTCompilationUnit> asts) {
    List<IJavaDSLArtifactScope> as = asts.stream().map(JavaDSLSymbolTableUtil::runSymTabGenitor).collect(Collectors.toList());
    asts.forEach(JavaDSLSymbolTableUtil::runSymTabCompleter);
    asts.forEach(JavaDSLSymbolTableUtil::runSymTabFinalization);
    return as;
  }
  
  public static IJavaDSLArtifactScope runSymTabGenitor(ASTCompilationUnit ast) {
    JavaDSLScopesGenitorDelegator genitor = JavaDSLMill.scopesGenitorDelegator();
    return genitor.createFromAST(ast);
  }
  
  public static void runSymTabFinalization(ASTCompilationUnit ast) {
    JavaDSLScopesGenitorP3Delegator p3Genitor = JavaDSLMill.scopesGenitorP3Delegator();
    p3Genitor.createFromAST(ast);
  }
  
  public static void runSymTabCompleter(ASTCompilationUnit ast) {
    JavaDSLTraverser traverser = JavaDSLMill.inheritanceTraverser();
    
    JavaDSLSymbolTableCompleter javaDslCompleter = new JavaDSLSymbolTableCompleter();
    traverser.add4JavaDSL(javaDslCompleter);
    traverser.add4JavaLight(javaDslCompleter);
    
    JavaLightSTCompleteTypes javaLightSTCompleteTypes = new JavaLightSTCompleteTypes();
    traverser.add4JavaLight(javaLightSTCompleteTypes);
    
    JavaDSLMCCommonStatementsSymTabCompletion javaDSLMCCommonStatementsSymTabCompletion = new JavaDSLMCCommonStatementsSymTabCompletion();
    traverser.add4JavaDSL(javaDSLMCCommonStatementsSymTabCompletion);
    traverser.add4MCCommonStatements(javaDSLMCCommonStatementsSymTabCompletion);
    
    LambdaExpressionsSTCompleteTypes2 lambdaExpressionsSTCompleteTypes2 = new LambdaExpressionsSTCompleteTypes2();
    traverser.add4LambdaExpressions(lambdaExpressionsSTCompleteTypes2);
    
    JavaDSLMCVarDeclarationStatementsSymTabCompletion javaDSLMCVarDeclarationStatementsSymTabCompletion = new JavaDSLMCVarDeclarationStatementsSymTabCompletion();
    traverser.add4JavaDSL(javaDSLMCVarDeclarationStatementsSymTabCompletion);
    traverser.add4MCVarDeclarationStatements(javaDSLMCVarDeclarationStatementsSymTabCompletion);
    
    TypeParametersSTCompleteTypes typeParametersSTCompleteTypes = new TypeParametersSTCompleteTypes();
    traverser.add4TypeParameters(typeParametersSTCompleteTypes);
    
    ast.accept(traverser);
  }
}
