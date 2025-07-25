package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.statements.mccommonstatements._ast.ASTConstantsMCCommonStatements;
import de.monticore.symboltable.ImportStatement;

public final class JavaDSLScopesGenitor extends JavaDSLScopesGenitorTOP {
  
  @Override
  public IJavaDSLArtifactScope createFromAST(ASTCompilationUnit rootNode) {
    IJavaDSLArtifactScope artifactScope = super.createFromAST(rootNode);
    
    if (rootNode instanceof ASTOrdinaryCompilationUnit) {
      ASTOrdinaryCompilationUnit ordinaryCompilationUnit = (ASTOrdinaryCompilationUnit) rootNode;
      
      if (ordinaryCompilationUnit.isPresentPackageDeclaration()) {
        ASTPackageDeclaration packageDeclaration = ordinaryCompilationUnit.getPackageDeclaration();
        artifactScope.setPackageName(packageDeclaration.getMCQualifiedName().getQName());
      }
      
      for (ASTImportDeclaration importDeclaration : ordinaryCompilationUnit.getImportDeclarationList()) {
        artifactScope.addImports(new ImportStatement(importDeclaration.getMCQualifiedName().getQName(), importDeclaration.isSTAR()));
      }
    }
    
    // add java.lang import as java imports that package per default
    artifactScope.addImports(new ImportStatement("java.lang", true));
    
    // TODO figure out how to map static imports
    
    return artifactScope;
  }
  
  @Override
  public void visit(ASTClassDeclaration node) {
    super.visit(node);
    if (node.getJavaModifierList().stream()
        .anyMatch(x -> x.getModifier() == ASTConstantsMCCommonStatements.PUBLIC)) {
      IJavaDSLScope enclosingScope = node.getEnclosingScope();
      if (JavaDSLMill.typeDispatcher().isJavaDSLIJavaDSLArtifactScope(enclosingScope)) {
        enclosingScope.setName(node.getName());
      }
    }
    
  }
  
  @Override
  public void visit(ASTRecordDeclaration node) {
    super.visit(node);
    if (node.getJavaModifierList().stream()
        .anyMatch(x -> x.getModifier() == ASTConstantsMCCommonStatements.PUBLIC)) {
      IJavaDSLScope enclosingScope = node.getEnclosingScope();
      if (JavaDSLMill.typeDispatcher().isJavaDSLIJavaDSLArtifactScope(enclosingScope)) {
        enclosingScope.setName(node.getName());
      }
    }
  }
  
  @Override
  public void visit(ASTInterfaceDeclaration node) {
    super.visit(node);
    if (node.getJavaModifierList().stream()
        .anyMatch(x -> x.getModifier() == ASTConstantsMCCommonStatements.PUBLIC)) {
      IJavaDSLScope enclosingScope = node.getEnclosingScope();
      if (JavaDSLMill.typeDispatcher().isJavaDSLIJavaDSLArtifactScope(enclosingScope)) {
        enclosingScope.setName(node.getName());
      }
    }
  }
  
  @Override
  public void visit(ASTEnumDeclaration node) {
    super.visit(node);
    if (node.getJavaModifierList().stream()
        .anyMatch(x -> x.getModifier() == ASTConstantsMCCommonStatements.PUBLIC)) {
      IJavaDSLScope enclosingScope = node.getEnclosingScope();
      if (JavaDSLMill.typeDispatcher().isJavaDSLIJavaDSLArtifactScope(enclosingScope)) {
        enclosingScope.setName(node.getName());
      }
    }
  }
  
  @Override
  public void visit(ASTModuleDeclaration node) {
    super.visit(node);
    IJavaDSLScope enclosingScope = node.getEnclosingScope();
    if (JavaDSLMill.typeDispatcher().isJavaDSLIJavaDSLArtifactScope(enclosingScope)) {
      enclosingScope.setName(node.getName());
    }
  }
}
