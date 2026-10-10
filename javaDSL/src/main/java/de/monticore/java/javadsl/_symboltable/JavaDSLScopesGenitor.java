package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl._ast.*;
import de.monticore.javalight._ast.ASTClassBodyDeclaration;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
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
    addImplicitModifiers(node);
  }
  
  @Override
  public void visit(ASTRecordDeclaration node) {
    super.visit(node);
    
    TypeDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsRecord(true);
    
    tryToUpdateScopeName(node, node.getJavaModifierList());
    updateModifiers(symbol, node.getJavaModifierList());
    addImplicitModifiers(node);
  }
  
  @Override
  public void visit(ASTInterfaceDeclaration node) {
    super.visit(node);
    
    TypeDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsInterface(true);
    
    tryToUpdateScopeName(node, node.getJavaModifierList());
    updateModifiers(symbol, node.getJavaModifierList());
    addImplicitModifiers(node);
  }
  
  @Override
  public void visit(ASTEnumDeclaration node) {
    super.visit(node);
    
    TypeDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsEnum(true);
    
    tryToUpdateScopeName(node, node.getJavaModifierList());
    updateModifiers(symbol, node.getJavaModifierList());
    addImplicitModifiers(node);
  }
  
  @Override
  public void endVisit(ASTClassDeclaration node) {
    List<ASTConstructorDeclaration> constructors =
        getConstructors(node.getClassBody().getClassBodyDeclarationList());
    if (constructors.isEmpty()) {
      // JLS 8.8.9: the default constructor has the access modifier of the class
      JavaMethodSymbol constructor = ImplicitMembers.addConstructor(node.getSymbol());
      constructor.setIsPublic(node.getSymbol().isIsPublic());
      constructor.setIsProtected(node.getSymbol().isIsProtected());
      constructor.setIsPrivate(node.getSymbol().isIsPrivate());
    }
    super.endVisit(node);
  }
  
  @Override
  public void endVisit(ASTEnumDeclaration node) {
    List<ASTConstructorDeclaration> constructors = getConstructors(node.isPresentEnumBody()
        ? node.getEnumBody().getClassBodyDeclarationList() : List.of());
    if (constructors.isEmpty()) {
      // JLS 8.9.2: the default constructor of an enum is private
      ImplicitMembers.addConstructor(node.getSymbol()).setIsPrivate(true);
    }
    super.endVisit(node);
  }
  
  @Override
  public void endVisit(ASTRecordDeclaration node) {
    getConstructors(node.getRecordBody().getClassBodyDeclarationList());
    super.endVisit(node);
  }
  
  /**
   * Returns the constructors among the members and marks their symbols as constructors.
   */
  private List<ASTConstructorDeclaration> getConstructors(
      List<? extends ASTClassBodyDeclaration> members) {
    List<ASTConstructorDeclaration> constructors = members.stream()
        .filter(ASTConstructorDeclaration.class::isInstance)
        .map(ASTConstructorDeclaration.class::cast)
        .toList();
    constructors.forEach(constructor -> constructor.getSymbol().setIsConstructor(true));
    return constructors;
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
    addImplicitModifiers(node);
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
      if (enclosingScope instanceof IJavaDSLArtifactScope) {
        enclosingScope.setName(node.getName());
      }
    }
  }
  
  /**
   * Adds the modifiers that types have implicitly, depending on their kind and location.
   */
  private void addImplicitModifiers(ASTTypeDeclaration node) {
    TypeDeclarationSymbol symbol = node.getSymbol();
    boolean isInterface = symbol.isIsInterface() || symbol.isIsAnnotation();
    IJavaDSLScope enclosingScope = node.getEnclosingScope();
    boolean isTopLevel = enclosingScope instanceof IJavaDSLArtifactScope;
    if (isInterface) {
      // JLS 9.1.1.1
      symbol.setIsAbstract(true);
    }
    if (!isTopLevel && (isInterface || symbol.isIsEnum() || symbol.isIsRecord())) {
      // JLS 8.9, 8.10, 9.1.1.3 (member types), and 14.3 (local types)
      symbol.setIsStatic(true);
    }
    if (symbol.isIsRecord()) {
      // JLS 8.10
      symbol.setIsFinal(true);
    }
    if (node instanceof ASTEnumDeclaration enumDeclaration && enumDeclaration
        .getEnumConstantDeclarationList().stream().noneMatch(c -> c.isPresentClassBody())) {
      // JLS 8.9: an enum is final, unless a constant has a class body (then it is sealed)
      symbol.setIsFinal(true);
    }
    if (enclosingScope.isPresentAstNode()
        && (enclosingScope.getAstNode() instanceof ASTInterfaceDeclaration
        || enclosingScope.getAstNode() instanceof ASTAnnotationTypeDeclaration)) {
      // JLS 9.5: member types of interfaces are public and static
      symbol.setIsPublic(true);
      symbol.setIsStatic(true);
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
