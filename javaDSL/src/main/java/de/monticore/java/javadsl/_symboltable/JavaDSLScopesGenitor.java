package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.statements.mccommonstatements._ast.*;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symboltable.ImportStatement;

import java.util.List;

public final class JavaDSLScopesGenitor extends JavaDSLScopesGenitorTOP
    implements JavaLightVisitor2 {
  
  @Override
  public IJavaDSLArtifactScope createFromAST(ASTCompilationUnit rootNode) {
    IJavaDSLArtifactScope artifactScope = super.createFromAST(rootNode);
    
    // Java allows unnamed packages, so we set a name if needed
    if (!artifactScope.isPresentName()) {
      artifactScope.setName("");
    }
    
    if (rootNode instanceof ASTOrdinaryCompilationUnit ordinaryCompilationUnit) {
      
      if (ordinaryCompilationUnit.isPresentPackageDeclaration()) {
        ASTPackageDeclaration packageDeclaration = ordinaryCompilationUnit.getPackageDeclaration();
        artifactScope.setPackageName(packageDeclaration.getMCQualifiedName().getQName());
      }
      
      for (ASTImportDeclaration importDeclaration : ordinaryCompilationUnit.getImportDeclarationList()) {
        artifactScope.addImports(
            new ImportStatement(importDeclaration.getMCQualifiedName().getQName(),
                importDeclaration.isSTAR()));
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
    
    TypeDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsClass(true);
    
    tryToUpdateScopeName(node, node.getJavaModifierList());
    updateModifiers(symbol, node.getJavaModifierList());
  }
  
  @Override
  public void visit(ASTRecordDeclaration node) {
    super.visit(node);
    
    TypeDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsRecord(true);
    
    tryToUpdateScopeName(node, node.getJavaModifierList());
    updateModifiers(symbol, node.getJavaModifierList());
  }
  
  @Override
  public void visit(ASTInterfaceDeclaration node) {
    super.visit(node);
    
    TypeDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsInterface(true);
    
    tryToUpdateScopeName(node, node.getJavaModifierList());
    updateModifiers(symbol, node.getJavaModifierList());
  }
  
  @Override
  public void visit(ASTEnumDeclaration node) {
    super.visit(node);
    
    TypeDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsEnum(true);
    
    tryToUpdateScopeName(node, node.getJavaModifierList());
    updateModifiers(symbol, node.getJavaModifierList());
  }
  
  @Override
  public void visit(ASTModuleDeclaration node) {
    super.visit(node);
    IJavaDSLScope enclosingScope = node.getEnclosingScope();
    if (enclosingScope instanceof IJavaDSLArtifactScope) {
      enclosingScope.setName(node.getName());
    }
  }
  
  @Override
  public void visit(ASTAnnotationTypeDeclaration node) {
    super.visit(node);
    
    TypeDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsAnnotation(true);
    
    tryToUpdateScopeName(node, node.getJavaModifierList());
    updateModifiers(symbol, node.getJavaModifierList());
  }
  
  /**
   * Try to set the name of the ArtifactScope
   * The name of the artifact scope should equal the name of
   * a public type.
   *
   * @param node Candidate for public type
   * @param modifiers List of node's modifiers
   */
  private void tryToUpdateScopeName(ASTTypeDeclaration node, List<ASTJavaModifier> modifiers) {
    if (modifiers.stream().anyMatch(x -> x instanceof ASTModifierPublic)) {
      IJavaDSLScope enclosingScope = node.getEnclosingScope();
      if (JavaDSLMill.typeDispatcher().isJavaDSLIJavaDSLArtifactScope(enclosingScope)) {
        enclosingScope.setName(node.getName());
      }
    }
  }
  
  private void updateModifiers(OOTypeSymbol symbol, List<ASTJavaModifier> modifiers) {
    modifiers.forEach(javaModifier -> {
      switch (javaModifier) {
        case ASTModifierPublic m -> symbol.setIsPublic(true);
        case ASTModifierProtected m -> symbol.setIsProtected(true);
        case ASTModifierPrivate m -> symbol.setIsPrivate(true);
        case ASTModifierAbstract m -> symbol.setIsAbstract(true);
        case ASTModifierStatic m -> symbol.setIsStatic(true);
        case ASTModifierFinal m -> symbol.setIsFinal(true);
        default -> {}
      }
    });
  }
}
