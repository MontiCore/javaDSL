/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.java.symboltable;

import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.ABSTRACT;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.FINAL;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.NATIVE;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.PRIVATE;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.PROTECTED;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.PUBLIC;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.STATIC;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.STRICTFP;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.SYNCHRONIZED;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.TRANSIENT;
import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.VOLATILE;
import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl._ast.ASTAnnotation;
import de.monticore.java.javadsl._ast.ASTAnnotationMethod;
import de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration;
import de.monticore.java.javadsl._ast.ASTCatchClause;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTCommonForControl;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTConstDeclaration;
import de.monticore.java.javadsl._ast.ASTConstantDeclarator;
import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTDeclaratorId;
import de.monticore.java.javadsl._ast.ASTDoWhileStatement;
import de.monticore.java.javadsl._ast.ASTEnhancedForControl;
import de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration;
import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTForStatement;
import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTFormalParameterListing;
import de.monticore.java.javadsl._ast.ASTIfStatement;
import de.monticore.java.javadsl._ast.ASTImportDeclaration;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTInterfaceMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTJavaBlock;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._ast.ASTLastFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTMethodSignature;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._ast.ASTResource;
import de.monticore.java.javadsl._ast.ASTSwitchBlockStatementGroup;
import de.monticore.java.javadsl._ast.ASTSwitchStatement;
import de.monticore.java.javadsl._ast.ASTThrows;
import de.monticore.java.javadsl._ast.ASTTryStatement;
import de.monticore.java.javadsl._ast.ASTTryStatementWithResources;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTWhileStatement;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.symboltable.ArtifactScope;
import de.monticore.symboltable.CommonScope;
import de.monticore.symboltable.CommonSymbolTableCreator;
import de.monticore.symboltable.ImportStatement;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.SymbolTableCreator;
import de.monticore.types.JTypeSymbolsHelper;
import de.monticore.types.JTypeSymbolsHelper.JTypeReferenceFactory;
import de.monticore.types.TypesHelper;
import de.monticore.types.TypesPrinter;
import de.monticore.types.types._ast.ASTArrayType;
import de.monticore.types.types._ast.ASTComplexArrayType;
import de.monticore.types.types._ast.ASTComplexReferenceType;
import de.monticore.types.types._ast.ASTPrimitiveArrayType;
import de.monticore.types.types._ast.ASTPrimitiveType;
import de.monticore.types.types._ast.ASTQualifiedName;
import de.monticore.types.types._ast.ASTReturnType;
import de.monticore.types.types._ast.ASTSimpleReferenceType;
import de.monticore.types.types._ast.ASTType;
import de.monticore.types.types._ast.ASTTypeParameters;
import de.se_rwth.commons.Names;

public class JavaSymbolTableCreator extends CommonSymbolTableCreator implements JavaDSLVisitor,
    SymbolTableCreator {
  
  public static final String THROWABLE = "Throwable";
  
  public static final String VOID = "void";
  
  protected JavaSymbolFactory symbolFactory = new JavaSymbolFactory();
  
  protected JTypeReferenceFactory<JavaTypeSymbolReference> typeRefFactory = (name, scope,
      dim) -> new JavaTypeSymbolReference(name, scope, dim);
  
  protected String artifactName;
  
  /**
   * Stack for managing block nodes as well as method and constructor nodes. It
   * is required to prevent that two scopes are created for a method or a
   * constructor declaration since their signature and body are separated into
   * two nodes.
   */
  private Deque<ASTNode> blockNodesStack = new ArrayDeque<>();
  
  public JavaSymbolTableCreator(
      final ResolvingConfiguration resolvingConfig,
      final MutableScope enclosingScope) {
    super(resolvingConfig, enclosingScope);
  }
  
  public JavaSymbolTableCreator(
      final ResolvingConfiguration resolvingConfig,
      final Deque<MutableScope> scopeStack) {
    super(resolvingConfig, scopeStack);
  }
  
  public void setArtifactName(String artifactName) {
    this.artifactName = artifactName;
  }
  
  /**
   * Creates the symbol table starting from the <code>rootNode</code> and
   * returns the first scope that was created.
   *
   * @param rootNode the root node
   * @return the first scope that was created
   */
  public Scope createFromAST(ASTJavaDSLNode rootNode) {
    requireNonNull(rootNode);
    rootNode.accept(this);
    return getFirstCreatedScope();
  }
  
  @Override
  public void visit(final ASTCompilationUnit astCompilationUnit) {
    // CompilationUnit = PackageDeclaration? ...
    String packageName = "";
    if (astCompilationUnit.getPackageDeclaration().isPresent()) {
      packageName = Names.getQualifiedName(astCompilationUnit.getPackageDeclaration().get()
          .getQualifiedName().getParts());
      
      // ... ImportDeclaration* ...
      List<ImportStatement> importStatements = new ArrayList<>();
      // always import java.lang.*;
      importStatements.add(new ImportStatement("java.lang", true));
      for (ASTImportDeclaration astImportDeclaration : astCompilationUnit.getImportDeclarations()) {
        String qualifiedName = Names.getQualifiedName(astImportDeclaration.getQualifiedName()
            .getParts());
        boolean isStar = astImportDeclaration.isSTAR();
        // TODO Process static imports when supported by ImportStatement.
        boolean isStatic = astImportDeclaration.isStatic();
        importStatements.add(new ImportStatement(qualifiedName, isStar));
      }
      
      // ... TypeDeclaration* (handled by other visit methods)
      
      // no more nonterminals to process from here
      final ArtifactScope artifactScope = new ArtifactScope(Optional.empty(), packageName,
          importStatements);
      artifactScope.setName(artifactName);
      artifactScope.setAstNode(astCompilationUnit);
      astCompilationUnit.setEnclosingScope(artifactScope);
      putOnStack(artifactScope);
    }
  }
  
  @Override
  public void endVisit(final ASTCompilationUnit astCompilationUnit) {
    removeCurrentScope();
    setEnclosingScopeOfNodes(astCompilationUnit);
  }
  
  @Override
  public void visit(final ASTClassDeclaration astClassDeclaration) {
    // ClassDeclaration implements TypeDeclaration = Modifier* "class" Name ...
    JavaTypeSymbol javaClassTypeSymbol = symbolFactory
        .createClassSymbol(astClassDeclaration.getName());
    addModifiersToType(javaClassTypeSymbol, astClassDeclaration.getModifiers());
    
    // ... TypeParameters? ...
    List<JavaTypeSymbol> typeParameters = addTypeParametersToType(javaClassTypeSymbol,
        astClassDeclaration.getTypeParameters());
    
    // ... ("extends" superClass:Type)? ...
    if (astClassDeclaration.getSuperClass().isPresent()) {
      JavaTypeSymbolReference superClassReference = new JavaTypeSymbolReference(
          TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(astClassDeclaration
              .getSuperClass().get()),
          currentScope().get(), 0);
      javaClassTypeSymbol.setSuperClass(superClassReference);
    }
    else if(!("Object").equals(astClassDeclaration.getName())){
      JavaTypeSymbolReference superClassReference = new JavaTypeSymbolReference(
          "Object", currentScope().get(), 0);
      javaClassTypeSymbol.setSuperClass(superClassReference);
    }
    
    
    // ... ("implements" implementedInterfaces:(Type || ",")+)? ...
    addInterfacesToType(javaClassTypeSymbol, astClassDeclaration.getImplementedInterfaces());
    
    // ... ClassBody (handled by other visit methods)
    
    // no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaClassTypeSymbol, astClassDeclaration);
    for (JavaTypeSymbol typeParameter : typeParameters) {
      currentScope().get().add(typeParameter);
    }
  }
  
  @Override
  public void endVisit(final ASTClassDeclaration astClassDeclaration) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTInterfaceDeclaration astInterfaceDeclaration) {
    // InterfaceDeclaration implements TypeDeclaration = Modifier* "interface"
    // Name ...
    JavaTypeSymbol javaTypeDeclarationSymbol = symbolFactory.createInterfaceSymbol(
        astInterfaceDeclaration.getName());
    addModifiersToType(javaTypeDeclarationSymbol, astInterfaceDeclaration.getModifiers());
    
    // ... TypeParameters? ...
    List<JavaTypeSymbol> typeParameters = addTypeParametersToType(javaTypeDeclarationSymbol,
        astInterfaceDeclaration.getTypeParameters());
    
    // ... ("extends" extendedInterfaces:(Type || ",")+)? ...
    addInterfacesToType(javaTypeDeclarationSymbol, astInterfaceDeclaration.getExtendedInterfaces());
    
    // ... InterfaceBody (handled by other visit methods)
    
    // no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaTypeDeclarationSymbol, astInterfaceDeclaration);
    for (JavaTypeSymbol typeParameter : typeParameters) {
      currentScope().get().add(typeParameter);
    }
  }
  
  @Override
  public void endVisit(final ASTInterfaceDeclaration astInterfaceDeclaration) {
    removeCurrentScope();
  }

  private String enumTypeName = "";

  @Override
  public void visit(final ASTEnumDeclaration astEnumDeclaration) {
    // EnumDeclaration implements TypeDeclaration = Modifier* "enum" Name ...
    JavaTypeSymbol javaEnumTypeSymbol = symbolFactory.createEnumSymbol(astEnumDeclaration
        .getName());
    
    addModifiersToType(javaEnumTypeSymbol, astEnumDeclaration.getModifiers());
    
    // ... ("implements" (Type || ",")+)? ...
    addInterfacesToType(javaEnumTypeSymbol, astEnumDeclaration.getImplementedInterfaces());
    
    // ... "{" (EnumConstantDeclaration || ",")* ","? ...
    
    // ... EnumBody? "}" (handled by other visit methods (ClassBody))
    
    // no more nonterminals to process from here
    enumTypeName = astEnumDeclaration.getName();
    addToScopeAndLinkWithNode(javaEnumTypeSymbol, astEnumDeclaration);
  }
  
  @Override
  public void endVisit(final ASTEnumDeclaration astEnumDeclaration) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTEnumConstantDeclaration astEnumConstantDeclaration) {
    /* A enum constant can be seen as a singleton object of an anonymous
     * sub-/implementing class of the enum (even though an enum is always
     * final). Create two symbols here: 1. Implementing Symbol (a type) 2.
     * Constant (an attribute) */
    JavaTypeSymbol javaEnumImplementationTypeSymbol = symbolFactory
        .createEnumSymbol(astEnumConstantDeclaration.getName());
    
    JavaTypeSymbolReference enumReference = new JavaTypeSymbolReference(
        enumTypeName, currentScope().get(), 0);

    JavaFieldSymbol javaEnumSingletonConstantSymbol = new JavaFieldSymbol(
        astEnumConstantDeclaration.getName(), JavaFieldSymbol.KIND, enumReference);

    // EnumConstantDeclaration = Annotation* ...
    for (ASTAnnotation astAnnotation : astEnumConstantDeclaration.getAnnotations()) {
      String qualifiedName = Names.getQualifiedName(astAnnotation.getAnnotationName().getParts());
      JavaTypeSymbolReference javaTypeSymbolReference = new JavaTypeSymbolReference(qualifiedName,
          currentScope().get(), 0);
      javaEnumImplementationTypeSymbol.addAnnotation(javaTypeSymbolReference);
    }
    
    // ... Name Arguments? ...
    // Name is processed first (needed for instantiation).
    
    // ... ClassBody? (handled by other visit methods)
    
    // no more nonterminals to process from here
    
    // Put constant first into scope since type is a scope spanning symbol
    addToScopeAndLinkWithNode(javaEnumSingletonConstantSymbol, astEnumConstantDeclaration);
    addToScopeAndLinkWithNode(javaEnumImplementationTypeSymbol, astEnumConstantDeclaration);
  }
  
  @Override
  public void endVisit(final ASTEnumConstantDeclaration astEnumConstantDeclaration) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTAnnotationTypeDeclaration astAnnotationTypeDeclaration) {
    // AnnotationTypeDeclaration implements TypeDeclaration
    // = Modifier* "@" "interface" Name
    JavaTypeSymbol javaAnnotationTypeSymbol = symbolFactory.createAnnotation(
        astAnnotationTypeDeclaration.getName());
    addModifiersToType(javaAnnotationTypeSymbol, astAnnotationTypeDeclaration.getModifiers());
    
    // AnnotationTypeBody (handled by other visit methods)
    
    // no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaAnnotationTypeSymbol, astAnnotationTypeDeclaration);
  }
  
  @Override
  public void endVisit(final ASTAnnotationTypeDeclaration astAnnotationTypeDeclaration) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTAnnotationMethod astAnnotationMethod) {
    JavaMethodSymbol javaAnnotationMethodSymbol = new JavaMethodSymbol(
        astAnnotationMethod.getName(), JavaMethodSymbol.KIND);
    // AnnotationMethod = Modifier* = ...
    addModifiersToMethod(javaAnnotationMethodSymbol, astAnnotationMethod.getModifiers());
    
    // ... Type ...
    JavaTypeSymbolReference javaTypeSymbolReference = null;
    
    final String returnTypeName = TypesPrinter
        .printTypeWithoutTypeArgumentsAndDimension(astAnnotationMethod
            .getType());
    javaTypeSymbolReference = new JavaTypeSymbolReference(returnTypeName, currentScope().get(),
        TypesHelper.getArrayDimensionIfArrayOrZero(astAnnotationMethod.getType()));
     
    javaAnnotationMethodSymbol.setReturnType(javaTypeSymbolReference);
    
    // ... Name "(" ")" ...
    // processed at the beginning of this method
    
    // ... DefaultValue? ";"
    // DefaultValue is an expression not a Symbol.
    
    // no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaAnnotationMethodSymbol, astAnnotationMethod);
  }
  
  @Override
  public void endVisit(final ASTAnnotationMethod astAnnotationMethod) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTFieldDeclaration astFieldDeclaration) {
    for (ASTVariableDeclarator variableDeclarator : astFieldDeclaration.getVariableDeclarators()) {
      JavaFieldSymbol javaFieldSymbol = symbolFactory.createFieldSymbol(
          variableDeclarator.getDeclaratorId().getName(), null);
      
      // FieldDeclaration = Modifier* ...
      addModifiersToField(javaFieldSymbol, astFieldDeclaration.getModifiers());
      
      // ... Type ...
      initializeJavaAttributeSymbol(javaFieldSymbol, astFieldDeclaration.getType(),
          variableDeclarator.getDeclaratorId().getDim().size());
      
      // ... (VariableDeclarator || ",")+
      // VariableDeclarator is handled by the for loop
      
      // no more nonterminals to process from here
      addToScopeAndLinkWithNode(javaFieldSymbol, astFieldDeclaration);
    }
  }
  
  @Override
  public void visit(final ASTConstDeclaration astConstDeclaration) {
    for (ASTConstantDeclarator astConstantDeclarator : astConstDeclaration
        .getConstantDeclarators()) {
      JavaFieldSymbol javaFieldSymbol = symbolFactory.createFieldSymbol(
          astConstantDeclarator.getName(), null);
      
      // ConstDeclaration implements InterfaceMemberDeclaration = Modifier* ...
      addModifiersToField(javaFieldSymbol, astConstDeclaration.getModifiers());
      
      // ... Type ...
      initializeJavaAttributeSymbol(javaFieldSymbol, astConstDeclaration.getType(),
          astConstantDeclarator.getDim().size());
      
      // ... (ConstantDeclarator || ",")+ ";"
      // ConstantDeclaratorList is handled by the for loop
      
      // no more nonterminals to process from here
      addToScopeAndLinkWithNode(javaFieldSymbol, astConstDeclaration);
    }
  }
  
  protected void initializeJavaAttributeSymbol(JavaFieldSymbol javaFieldSymbol,
      ASTType astType, int additionalDimensions) {
    JTypeSymbolsHelper.initializeJAttributeSymbol(javaFieldSymbol, astType, additionalDimensions,
        currentScope().get(), typeRefFactory);
  }
  
  @Override
  public void visit(final ASTMethodDeclaration astMethodDeclaration) {
    ASTMethodSignature methodSignature = astMethodDeclaration.getMethodSignature();
    JavaMethodSymbol javaMethodSymbol = new JavaMethodSymbol(methodSignature.getName(),
        JavaMethodSymbol.KIND);
    
    Pair<List<JavaTypeSymbol>, List<JavaFieldSymbol>> addMethodSignatureToMethod = addMethodSignatureToMethod(
        javaMethodSymbol, methodSignature);
    
    // no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaMethodSymbol, astMethodDeclaration);
    for (JavaTypeSymbol javaTypeParameterSymbol : addMethodSignatureToMethod.l) {
      currentScope().get().add(javaTypeParameterSymbol);
    }

    blockNodesStack.add(astMethodDeclaration);
  }
  
  @Override
  public void endVisit(final ASTMethodDeclaration astMethodDeclaration) {
    removeCurrentScope();
    blockNodesStack.removeLast();
  }
  
  @Override
  public void visit(final ASTConstructorDeclaration astConstructorDeclaration) {
    JavaMethodSymbol javaConstructorSymbol = symbolFactory
        .createConstructor(astConstructorDeclaration.getName());
    javaConstructorSymbol.setConstructor(true);
    
    // ConstructorDeclaration implements ClassMemberDeclaration = Modifier* ...
    addModifiersToMethod(javaConstructorSymbol, astConstructorDeclaration.getModifiers());
    
    // ... TypeParameters? ...
    List<JavaTypeSymbol> javaTypeParameters = addTypeParametersToMethod(javaConstructorSymbol,
        astConstructorDeclaration.getTypeParameters());
    
    // ... FormalParameters ...
    addFormalParametersToMethod(
        javaConstructorSymbol, astConstructorDeclaration.getFormalParameters()
            .getFormalParameterListing());
    
    // ... ("throws" Throws)? ...
    addThrowsToMethod(javaConstructorSymbol, astConstructorDeclaration.getThrows());
    
    // ... ConstructorBody (handled by other visit methods)
    
    // no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaConstructorSymbol, astConstructorDeclaration);
    for (JavaTypeSymbol javaTypeParameterSymbol : javaTypeParameters) {
      currentScope().get().add(javaTypeParameterSymbol);
    }
   
    blockNodesStack.add(astConstructorDeclaration);
  }
  
  @Override
  public void endVisit(final ASTConstructorDeclaration astConstructorDeclaration) {
    removeCurrentScope();
    blockNodesStack.removeLast();
  }
  
  @Override
  public void visit(final ASTInterfaceMethodDeclaration astInterfaceMethodDeclaration) {
    ASTMethodSignature methodSignature = astInterfaceMethodDeclaration.getMethodSignature();
    JavaMethodSymbol javaMethodSymbol = new JavaMethodSymbol(methodSignature.getName(),
        JavaMethodSymbol.KIND);
    
    addMethodSignatureToMethod(javaMethodSymbol, methodSignature);
    
    // no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaMethodSymbol, astInterfaceMethodDeclaration);
  }
  
  @Override
  public void endVisit(final ASTInterfaceMethodDeclaration astInterfaceMethodDeclaration) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTLocalVariableDeclaration astLocalVariableDeclaration) {
    for (ASTVariableDeclarator variableDeclarator : astLocalVariableDeclaration
        .getVariableDeclarators()) {
      JavaFieldSymbol javaFieldSymbol = symbolFactory.createLocalVariableSymbol(
          variableDeclarator.getDeclaratorId().getName(), null);
      
      // FieldDeclaration = Modifier* ...
      addModifiersToField(javaFieldSymbol, astLocalVariableDeclaration.getPrimitiveModifiers());
      
      // ... Type ...
      initializeJavaAttributeSymbol(javaFieldSymbol, astLocalVariableDeclaration.getType(),
          variableDeclarator.getDeclaratorId().getDim().size());
      
      // ... (VariableDeclarator || ",")+
      // VariableDeclarator is handled by the for loop
      
      // no more nonterminals to process from here
      addToScopeAndLinkWithNode(javaFieldSymbol, astLocalVariableDeclaration);
    }
  }

  @Override
  public void visit(final ASTEnhancedForControl astEnhancedForControl) {
    ASTFormalParameter astFormalParameter = astEnhancedForControl.getFormalParameter();
    JavaFieldSymbol javaFieldSymbol = symbolFactory.createLocalVariableSymbol(
            astFormalParameter.getDeclaratorId().getName(), null);

    // FormalParameter = PrimitiveModifier* ...
    addModifiersToField(javaFieldSymbol, astFormalParameter.getPrimitiveModifiers());

    // ... Type ...
    initializeJavaAttributeSymbol(javaFieldSymbol, astFormalParameter.getType(),
            astFormalParameter.getDeclaratorId().getDim().size());

    //no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaFieldSymbol, astFormalParameter);
  }


  
  @Override
  public void visit(final ASTTryStatement astTryClause) {
    CommonScope tryBlock = new CommonScope(false);
    putOnStack(tryBlock);
  }
  
  @Override
  public void endVisit(final ASTTryStatement astTryClause) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTTryStatementWithResources astTryClause) {
    CommonScope tryBlock = new CommonScope(false);
    putOnStack(tryBlock);
    for (ASTResource astResource : astTryClause.getResources()) {
      JavaFieldSymbol javaFieldSymbol = symbolFactory.createLocalVariableSymbol(
          astResource.getDeclaratorId().getName(), null);
      
      // Resource = VariableModifier* ...
      addModifiersToField(javaFieldSymbol, astResource.getPrimitiveModifiers());
      
      // ... ClassOrInterfaceType ...
      JTypeSymbolsHelper.initializeJAttributeSymbol(javaFieldSymbol, astResource.getType(), astResource.getDeclaratorId().getDim().size(),
          currentScope().get(), typeRefFactory);
      
      // no more nonterminals to process from here
      addToScopeAndLinkWithNode(javaFieldSymbol, astTryClause);
    }
  }
  
  @Override
  public void endVisit(final ASTTryStatementWithResources astTryClause) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTCatchClause astCatchClause) {
    CommonScope tryBlock = new CommonScope(true);
    putOnStack(tryBlock);
    List<ASTQualifiedName> qualifiedNames = astCatchClause.getCatchType().getQualifiedNames();
    
    String qualifiedName = null;
    // if there is only one type use that type ...
    if (qualifiedNames.size() == 1) {
      qualifiedName = Names.getQualifiedName(qualifiedNames.get(0).getParts());
    }
    else // ... otherwise its a multicatch so use Throwable the super class of
    {    // all exception
      qualifiedName = THROWABLE;
    }
    JavaTypeSymbolReference javaTypeSymbolReference = new JavaTypeSymbolReference(qualifiedName,
        currentScope().get(), 0);
    JavaFieldSymbol javaFieldSymbol = symbolFactory.createLocalVariableSymbol(
        astCatchClause.getName(),
        javaTypeSymbolReference);
    
    // no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaFieldSymbol, astCatchClause);
  }
  
  @Override
  public void endVisit(final ASTCatchClause astCatchClause) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(final ASTJavaBlock astCodeBlock) {
    if (!isCurrentNodeAMethodOrConstructor()) {
      CommonScope commonScope = new CommonScope(true);
      putOnStack(commonScope);
    }
  }

  @Override
  public void endVisit(final ASTJavaBlock astCodeBlock) {
    if (!isCurrentNodeAMethodOrConstructor()) {
      removeCurrentScope();
    }
  }

  @Override
  public void traverse(final ASTIfStatement astIfStatement) {
    if (null != astIfStatement.getCondition()) {
      astIfStatement.getCondition().accept(getRealThis());
    }
    if (null != astIfStatement.getThenStatement()) {
      CommonScope commonScope = new CommonScope(false);
      putOnStack(commonScope);
      setLinkBetweenSpannedScopeAndNode(commonScope, astIfStatement);
      astIfStatement.getThenStatement().accept(getRealThis());
      astIfStatement.getThenStatement().setEnclosingScope(currentScope().get());
      removeCurrentScope();
    }
    if (astIfStatement.getElseStatement().isPresent()) {
      CommonScope commonScope = new CommonScope(false);
      putOnStack(commonScope);
      setLinkBetweenSpannedScopeAndNode(commonScope, astIfStatement);
      astIfStatement.getElseStatement().get().accept(getRealThis());
      astIfStatement.getElseStatement().get().setEnclosingScope(currentScope().get());
      removeCurrentScope();
    }
  }
  
  @Override
  public void visit(final ASTForStatement astForStatement) {
    if(isCurrentNodeAMethodOrConstructor()) {
      CommonScope commonScope = new CommonScope(false);
      putOnStack(commonScope);
      setLinkBetweenSpannedScopeAndNode(commonScope, astForStatement);
    }
  }

  @Override
  public void endVisit(final ASTForStatement astForStatement) {
    if(isCurrentNodeAMethodOrConstructor()) {
      astForStatement.getForControl().setEnclosingScope(currentScope().get());
      astForStatement.getStatement().setEnclosingScope(currentScope().get());
      removeCurrentScope();
    }
  }
  
  @Override
  public void visit(final ASTWhileStatement astWhileStatement) {
    if (isCurrentNodeAMethodOrConstructor()) {
      CommonScope commonScope = new CommonScope(false);
      putOnStack(commonScope);
      setLinkBetweenSpannedScopeAndNode(commonScope, astWhileStatement);
    }
  }
  
  @Override
  public void endVisit(final ASTWhileStatement astWhileStatement) {
    if (isCurrentNodeAMethodOrConstructor()) {
      astWhileStatement.getCondition().setEnclosingScope(currentScope().get());
      astWhileStatement.getStatement().setEnclosingScope(currentScope().get());
      removeCurrentScope();
    }
  }
  
  @Override
  public void visit(final ASTDoWhileStatement astDoWhileStatement) {
    if (isCurrentNodeAMethodOrConstructor()) {
      CommonScope commonScope = new CommonScope(false);
      putOnStack(commonScope);
      setLinkBetweenSpannedScopeAndNode(commonScope, astDoWhileStatement);
    }
  }
  
  @Override
  public void endVisit(final ASTDoWhileStatement astDoWhileStatement) {
    if (isCurrentNodeAMethodOrConstructor()) {
      astDoWhileStatement.getCondition().setEnclosingScope(currentScope().get());
      astDoWhileStatement.getStatement().setEnclosingScope(currentScope().get());
      removeCurrentScope();
    }
  }
  
  @Override
  public void visit(final ASTSwitchStatement astSwitchStatement) {
    if (isCurrentNodeAMethodOrConstructor()) {
      CommonScope commonScope = new CommonScope(false);
      putOnStack(commonScope);
      setLinkBetweenSpannedScopeAndNode(commonScope, astSwitchStatement);
    }
  }
  
  @Override
  public void endVisit(final ASTSwitchStatement astSwitchStatement) {
    if (isCurrentNodeAMethodOrConstructor()) {
      for(ASTSwitchBlockStatementGroup astSwitchBlock : astSwitchStatement.getSwitchBlockStatementGroups()) {
        astSwitchBlock.setEnclosingScope(currentScope().get());
      }
      removeCurrentScope();
    }
  }
  
  protected boolean isCurrentNodeAMethodOrConstructor() {
    if (!blockNodesStack.isEmpty()) {
      ASTNode currentNode = blockNodesStack.getLast();
      
      return (currentNode instanceof ASTMethodDeclaration)
          || (currentNode instanceof ASTConstructorDeclaration);
    }
    
    return false;
  }
  
  protected void addModifiersToField(JavaFieldSymbol javaFieldSymbol,
      Iterable<? extends ASTModifier> astModifierList) {
    for (ASTModifier modifier : astModifierList) {
      if (modifier instanceof ASTPrimitiveModifier) {
        addPrimitiveModifierToField(javaFieldSymbol, (ASTPrimitiveModifier) modifier);
      }
      else if (modifier instanceof ASTAnnotation) {
        ASTAnnotation astAnnotation = (ASTAnnotation) modifier;
        JavaTypeSymbolReference javaAnnotationTypeSymbolReference = new JavaTypeSymbolReference(
            Names.getQualifiedName(astAnnotation.getAnnotationName().getParts()),
            currentScope().get(), 0);
        javaFieldSymbol.addAnnotation(javaAnnotationTypeSymbolReference);
      }
    }
  }
  
  protected void addPrimitiveModifierToField(JavaFieldSymbol javaFieldSymbol,
      ASTPrimitiveModifier modifier) {
    // visibility
    switch (modifier.getModifier()) {
      case PUBLIC:
        javaFieldSymbol.setPublic();
        break;
      case PROTECTED:
        javaFieldSymbol.setProtected();
        break;
      case PRIVATE:
        javaFieldSymbol.setPrivate();
        // other variable modifiers as in jls7 8.3.1 Field Modifiers
        break;
      case STATIC:
        javaFieldSymbol.setStatic(true);
        break;
      case FINAL:
        javaFieldSymbol.setFinal(true);
        break;
      case TRANSIENT:
        javaFieldSymbol.setTransient(true);
        break;
      case VOLATILE:
        javaFieldSymbol.setVolatile(true);
        break;
      default:
        break;
    }
  }
  
  protected Pair<List<JavaTypeSymbol>, List<JavaFieldSymbol>> addMethodSignatureToMethod(
      JavaMethodSymbol javaMethodSymbol, ASTMethodSignature methodSignature) {
    // MethodSignature = Modifier* ...
    addModifiersToMethod(javaMethodSymbol, methodSignature.getModifiers());
    
    // ... TypeParameters? ...
    List<JavaTypeSymbol> javaTypeParameters = addTypeParametersToMethod(javaMethodSymbol,
        methodSignature.getTypeParameters());
    
    // ... ReturnType ...
    // ASTReturnType is either ASTVoidType or ASTType
    JavaTypeSymbolReference javaTypeSymbolReference = null;
    
    if (methodSignature.getReturnType() instanceof ASTType) {
      ASTType nonVoidReturnType = (ASTType) methodSignature.getReturnType();
      final String returnTypeName = TypesPrinter
          .printTypeWithoutTypeArgumentsAndDimension(nonVoidReturnType);
      final int additionalDimensions = methodSignature.getDim().size();
      javaTypeSymbolReference = new JavaTypeSymbolReference(returnTypeName, currentScope().get(),
          TypesHelper.getArrayDimensionIfArrayOrZero(nonVoidReturnType) + additionalDimensions);
      addTypeArgumentsToTypeSymbol(javaTypeSymbolReference, nonVoidReturnType);
    }
    else {
      // The return type is ASTVoidType here
      int additionalDimensions = methodSignature.getDim().size();
      javaTypeSymbolReference = new JavaTypeSymbolReference(VOID,
          currentScope().get(), additionalDimensions);
      // Grammar allows "void method()[][] ;" so we process the trailing
      // brackets even though we know its an invalid model. Validity check is
      // task of a coco.
    }
    
    javaMethodSymbol.setReturnType(javaTypeSymbolReference);
    
    // ... Name ...
    // processed at the beginning of this method
    
    // ... FormalParameters ...
    Optional<ASTFormalParameterListing> optionalFormalParameterListing = methodSignature
        .getFormalParameters().getFormalParameterListing();
    List<JavaFieldSymbol> javaFormalParameterSymbols = addFormalParametersToMethod(
        javaMethodSymbol, optionalFormalParameterListing);
    
    // ... Ellipsis
    if (optionalFormalParameterListing.isPresent()) {
      javaMethodSymbol.setEllipsisParameterMethod(
          optionalFormalParameterListing.get().getLastFormalParameter().isPresent());
    }
    
    // ... ("throws" Throws)?
    addThrowsToMethod(javaMethodSymbol, methodSignature.getThrows());
    
    // no more nonterminals to process from here
    return new Pair<>(javaTypeParameters, javaFormalParameterSymbols);
  }
  
  protected void addModifiersToMethod(JavaMethodSymbol javaMethodSymbol,
      Iterable<? extends ASTModifier> astModifierList) {
    for (ASTModifier modifier : astModifierList) {
      if (modifier instanceof ASTPrimitiveModifier) {
        addPrimitiveModifierToMethod(javaMethodSymbol, (ASTPrimitiveModifier) modifier);
      }
      else if (modifier instanceof ASTAnnotation) {
        ASTAnnotation astAnnotation = (ASTAnnotation) modifier;
        String qualifiedName = Names.getQualifiedName(astAnnotation.getAnnotationName().getParts());
        JavaTypeSymbolReference javaTypeSymbolReference = new JavaTypeSymbolReference(
            qualifiedName, currentScope().get(), 0);
        javaMethodSymbol.addAnnotation(javaTypeSymbolReference);
      }
    }
  }
  
  protected void addPrimitiveModifierToMethod(JavaMethodSymbol javaMethodSymbol,
      ASTPrimitiveModifier modifier) {
    // visibility
    switch (modifier.getModifier()) {
      case PUBLIC:
        javaMethodSymbol.setPublic();
        break;
      case PROTECTED:
        javaMethodSymbol.setProtected();
        break;
      case PRIVATE:
        javaMethodSymbol.setPrivate();
        // other variable modifiers as in jls7 8.3.1 Field Modifiers
        break;
      case ABSTRACT:
        javaMethodSymbol.setAbstract(true);
        break;
      case STATIC:
        javaMethodSymbol.setStatic(true);
        break;
      case FINAL:
        javaMethodSymbol.setFinal(true);
        break;
      case NATIVE:
        javaMethodSymbol.setNative(true);
        break;
      case STRICTFP:
        javaMethodSymbol.setStrictfp(true);
        break;
      case SYNCHRONIZED:
        javaMethodSymbol.setSynchronized(true);
        break;
      default:
        break;
    }
  }
  
  protected List<JavaFieldSymbol> addFormalParametersToMethod(JavaMethodSymbol javaMethodSymbol,
      Optional<ASTFormalParameterListing> optionalFormalParameterListing) {
    List<JavaFieldSymbol> javaFormalParameterSymbols = new ArrayList<>();
    if (optionalFormalParameterListing.isPresent()) {
      // add the leading formal parameters, so no varargs here
      ASTFormalParameterListing astFormalParameterListing = optionalFormalParameterListing.get();
      for (ASTFormalParameter astFormalParameter : astFormalParameterListing
          .getFormalParameters()) {
        JavaFieldSymbol javaFormalParameterSymbol = addOneFormalParameterToMethod(
            javaMethodSymbol, astFormalParameter.getPrimitiveModifiers(),
            astFormalParameter.getType(), astFormalParameter.getDeclaratorId());
        
        javaFormalParameterSymbols.add(javaFormalParameterSymbol);
        setLinkBetweenSymbolAndNode(javaFormalParameterSymbol, astFormalParameter);
      }
      // add ASTLastFormalParameter (varargs)
      Optional<ASTLastFormalParameter> optionalLastFormalParameter = astFormalParameterListing
          .getLastFormalParameter();
      if (optionalLastFormalParameter.isPresent()) {
        ASTLastFormalParameter astLastFormalParameter = optionalLastFormalParameter.get();
        
        // A vararg is an array, so convert type to an appropriate array
        ASTArrayType arrayType;
        ASTType type = astLastFormalParameter.getType();
        if (type instanceof ASTPrimitiveType) {
          arrayType = ASTPrimitiveArrayType.getBuilder()
              .componentType(type).dimensions(1).build();
        }
        else if ((type instanceof ASTComplexReferenceType)
            || (type instanceof ASTSimpleReferenceType)) {
          arrayType = ASTComplexArrayType.getBuilder()
              .componentType(type).dimensions(1).build();
        }
        else if (type instanceof ASTArrayType) {
          arrayType = (ASTArrayType) type;
          arrayType.setDimensions(arrayType.getDimensions() + 1);
        }
        else {
          // In this case check the implementation of ASTType
          throw new IllegalArgumentException();
        }
        
        JavaFieldSymbol javaFormalParameterSymbol = addOneFormalParameterToMethod(
            javaMethodSymbol,
            astLastFormalParameter.getPrimitiveModifiers(),
            arrayType, astLastFormalParameter.getDeclaratorId());
        
        javaFormalParameterSymbols.add(javaFormalParameterSymbol);
        setLinkBetweenSymbolAndNode(javaFormalParameterSymbol, astLastFormalParameter);
      }
    }
    return javaFormalParameterSymbols;
  }
  
  protected JavaFieldSymbol addOneFormalParameterToMethod(JavaMethodSymbol javaMethodSymbol,
      Iterable<? extends ASTModifier> modifiers, ASTType astType, ASTDeclaratorId astDeclaratorId) {
    // new JavaFieldSymbol
    final String typeName = TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(astType);
    final int additionalDimensions = astDeclaratorId.getDim().size();
    JavaTypeSymbolReference javaTypeSymbolReference = new JavaTypeSymbolReference(typeName,
        currentScope().get(), TypesHelper.getArrayDimensionIfArrayOrZero(astType)
            + additionalDimensions);
    addTypeArgumentsToTypeSymbol(javaTypeSymbolReference, astType);
    
    JavaFieldSymbol javaFormalParameterSymbol = symbolFactory
        .createFormalParameterSymbol(astDeclaratorId.getName(), javaTypeSymbolReference);
    
    // init JavaFieldSymbol
    for (ASTModifier modifier : modifiers) {
      if (modifier instanceof ASTPrimitiveModifier) {
        ASTPrimitiveModifier primitiveModifier = (ASTPrimitiveModifier) modifier;
        if (primitiveModifier.getModifier() == FINAL) {
          javaFormalParameterSymbol.setFinal(true);
        }
      }
      else if (modifier instanceof ASTAnnotation) {
        ASTAnnotation astAnnotation = (ASTAnnotation) modifier;
        String annotationName = Names
            .getQualifiedName(astAnnotation.getAnnotationName().getParts());
        javaFormalParameterSymbol.addAnnotation(new JavaTypeSymbolReference(
            annotationName, currentScope().get(), 0));
      }
    }
    
    // add JavaFieldSymbol
    javaMethodSymbol.addParameter(javaFormalParameterSymbol);
    return javaFormalParameterSymbol;
  }
  
  protected void addThrowsToMethod(JavaMethodSymbol javaMethodSymbol, Optional<ASTThrows> throws1) {
    if (throws1.isPresent()) {
      ASTThrows astThrows = throws1.get();
      for (ASTQualifiedName astQualifiedName : astThrows.getQualifiedNames()) {
        String qualifiedName = Names.getQualifiedName(astQualifiedName.getParts());
        
        // Grammar has no trailingArrayBrackets here, so dimension=0
        javaMethodSymbol.addException(new JavaTypeSymbolReference(qualifiedName, currentScope()
            .get(), 0));
      }
    }
  }
  
  protected void addModifiersToType(JavaTypeSymbol javaTypeSymbol,
      Iterable<? extends ASTModifier> astModifierList) {
    for (ASTModifier modifier : astModifierList) {
      if (modifier instanceof ASTPrimitiveModifier) {
        addPrimitiveModifierToType(javaTypeSymbol, (ASTPrimitiveModifier) modifier);
      }
      else if (modifier instanceof ASTAnnotation) {
        ASTAnnotation astAnnotation = (ASTAnnotation) modifier;
        String qualifiedName = Names.getQualifiedName(astAnnotation.getAnnotationName().getParts());
        JavaTypeSymbolReference javaAnnotationTypeSymbolReference = new JavaTypeSymbolReference(
            qualifiedName, currentScope().get(), 0);
        javaTypeSymbol.addAnnotation(javaAnnotationTypeSymbolReference);
      }
    }
  }
  
  protected void addPrimitiveModifierToType(JavaTypeSymbol javaTypeSymbol,
      ASTPrimitiveModifier modifier) {
    // visibility
    switch (modifier.getModifier()) {
      case PUBLIC:
        javaTypeSymbol.setPublic();
        break;
      case PROTECTED:
        javaTypeSymbol.setProtected();
        break;
      case PRIVATE:
        javaTypeSymbol.setPrivate();
        // other variable modifiers as in jls7 8.3.1 Field Modifiers
        break;
      case ABSTRACT:
        javaTypeSymbol.setAbstract(true);
        break;
      case STATIC:
        javaTypeSymbol.setStatic(true);
        break;
      case FINAL:
        javaTypeSymbol.setFinal(true);
        break;
      case STRICTFP:
        javaTypeSymbol.setStrictfp(true);
        break;
      default:
        break;
    }
  }
  
  protected List<JavaTypeSymbol> addTypeParametersToMethod(JavaMethodSymbol javaMethodSymbol,
      Optional<ASTTypeParameters> typeParameters) {
    return JTypeSymbolsHelper.addTypeParametersToMethod(javaMethodSymbol, typeParameters,
        currentScope().get(), symbolFactory, typeRefFactory);
  }
  
  /**
   * Adds the TypeParameters to the JavaTypeSymbol if the class or interface
   * declares TypeVariables. Example:
   * <p>
   * class Bla<T, S extends SomeClass<T> & SomeInterface>
   * </p>
   * T and S are added to Bla.
   *
   * @param javaTypeSymbol
   * @param optionalTypeParameters
   * @return JavaTypeSymbol list to be added to the scope
   */
  protected List<JavaTypeSymbol> addTypeParametersToType(JavaTypeSymbol javaTypeSymbol,
      Optional<ASTTypeParameters> optionalTypeParameters) {
    return JTypeSymbolsHelper.addTypeParametersToType(javaTypeSymbol, optionalTypeParameters,
        currentScope().get(), symbolFactory, typeRefFactory);
  }
  
  /**
   * Adds the given ASTTypes as interfaces to the JavaTypeSymbol. The
   * JavaTypeSymbol can be a type variable. Interfaces may follow after the
   * first extended Type. We treat the first Type also as interface even though
   * it may be a class.
   * <p>
   * class Bla implements SomeInterface, AnotherInterface, ... <br>
   * class Bla&ltT extends SomeClassOrInterface & SomeInterface & ...&gt
   * </p>
   * See also JLS7.
   *
   * @param astInterfaceTypeList
   */
  protected void addInterfacesToType(JavaTypeSymbol javaTypeSymbol,
      List<ASTType> astInterfaceTypeList) {
    JTypeSymbolsHelper.addInterfacesToType(javaTypeSymbol, astInterfaceTypeList,
        currentScope().get(),
        typeRefFactory);
  }
  
  /**
   * Adds the ASTTypeArguments to the JavaTypeSymbol.
   *
   * @param javaTypeSymbolReference
   * @param astType
   */
  protected void addTypeArgumentsToTypeSymbol(JavaTypeSymbolReference javaTypeSymbolReference,
      ASTReturnType astType) {
    JTypeSymbolsHelper.addTypeArgumentsToTypeSymbol(javaTypeSymbolReference, astType,
        currentScope().get(), typeRefFactory);
  }
  
  private JavaDSLVisitor realThis = this;
  
  public JavaDSLVisitor getRealThis() {
    return realThis;
  }
  
  @Override
  public void setRealThis(JavaDSLVisitor realThis) {
    if (!this.realThis.equals(realThis)) {
      this.realThis = realThis;
    }
  }
  
}
