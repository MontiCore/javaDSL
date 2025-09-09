package de.monticore.java.javadsl._symboltable;

import de.monticore.expressions.lambdaexpressions._symboltable.LambdaExpressionsSTCompleteTypes2;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._symboltable.JavaLightSTCompleteTypes;
import de.monticore.types.typeparameters._symboltable.TypeParametersSTCompleteTypes;
import de.se_rwth.commons.logging.Log;

public class JavaDSLScopesGenitorP2Delegator {
  
  protected IJavaDSLGlobalScope globalScope;
  
  protected JavaDSLTraverser traverser;
  
  public JavaDSLTraverser getTraverser() {
    return traverser;
  }
  
  public JavaDSLScopesGenitorP2Delegator() {
    this.globalScope = JavaDSLMill.globalScope();
    this.traverser = JavaDSLMill.inheritanceTraverser();
    this.init();
    this.initInheritedCompleters();
  }
  
  protected void init() {
    this.initJavaDSL();
  }
  
  protected void initJavaDSL() {
    JavaDSLScopesGenitorP2 javaDSLScopesGenitorP2 = JavaDSLMill.scopesGenitorP2();
    this.getTraverser().add4JavaDSL(javaDSLScopesGenitorP2);
    this.getTraverser().add4JavaLight(javaDSLScopesGenitorP2);
  }
  
  protected void initInheritedCompleters() {
    JavaLightSTCompleteTypes javaLightSTCompleteTypes = new JavaLightSTCompleteTypes();
    this.getTraverser().add4JavaLight(javaLightSTCompleteTypes);
    
    JavaDSLMCCommonStatementsSymTabCompletion javaDSLMCCommonStatementsSymTabCompletion =
        new JavaDSLMCCommonStatementsSymTabCompletion();
    this.getTraverser().add4JavaDSL(javaDSLMCCommonStatementsSymTabCompletion);
    this.getTraverser().add4MCCommonStatements(javaDSLMCCommonStatementsSymTabCompletion);
    
    LambdaExpressionsSTCompleteTypes2 lambdaExpressionsSTCompleteTypes2 =
        new LambdaExpressionsSTCompleteTypes2();
    this.getTraverser().add4LambdaExpressions(lambdaExpressionsSTCompleteTypes2);
    
    JavaDSLMCVarDeclarationStatementsSymTabCompletion
        javaDSLMCVarDeclarationStatementsSymTabCompletion =
        new JavaDSLMCVarDeclarationStatementsSymTabCompletion();
    this.getTraverser().add4JavaDSL(javaDSLMCVarDeclarationStatementsSymTabCompletion);
    this.getTraverser()
        .add4MCVarDeclarationStatements(javaDSLMCVarDeclarationStatementsSymTabCompletion);
    
    TypeParametersSTCompleteTypes typeParametersSTCompleteTypes =
        new TypeParametersSTCompleteTypes();
    this.getTraverser().add4TypeParameters(typeParametersSTCompleteTypes);
  }
  
  public void createFromAST(ASTCompilationUnit astCompilationUnit) {
    Log.errorIfNull(astCompilationUnit, "0x7A016: Called createFromAST with argument null");
    astCompilationUnit.accept(traverser);
  }
}
