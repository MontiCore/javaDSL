package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.se_rwth.commons.logging.Log;

public class JavaDSLScopesGenitorP3Delegator {
  
  protected IJavaDSLGlobalScope globalScope;
  
  protected JavaDSLTraverser traverser;
  
  public JavaDSLTraverser getTraverser() {
    return traverser;
  }
  
  public JavaDSLScopesGenitorP3Delegator() {
    this.globalScope = JavaDSLMill.globalScope();
    this.traverser = JavaDSLMill.traverser();
    this.init();
  }
  
  protected void init() {
    this.initJavaDSL();
  }
  
  protected void initJavaDSL() {
    JavaDSLScopesGenitorP3 javaDSLScopesGenitorP3 = JavaDSLMill.scopesGenitorP3();
    this.getTraverser().add4JavaDSL(javaDSLScopesGenitorP3);
  }
  
  public void createFromAST(ASTCompilationUnit astCompilationUnit) {
    Log.errorIfNull(astCompilationUnit, "0x7A015: Called createFromAST with argument null");
    astCompilationUnit.accept(traverser);
  }
}
