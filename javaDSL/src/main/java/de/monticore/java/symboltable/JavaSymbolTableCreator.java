/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.symboltable;

import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.symboltable.*;
import de.monticore.types.MCTypesHelper;
import de.monticore.types.MCTypesJTypeSymbolsHelper;
import de.monticore.types.MCTypesJTypeSymbolsHelper.JTypeReferenceFactory;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCArrayType;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCTypeParameters;
import de.monticore.types.mcfullgenerictypes._ast.MCFullGenericTypesMill;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.Names;

import java.util.*;

import static de.monticore.java.javadsl._ast.ASTConstantsJavaDSL.*;
import static java.util.Objects.requireNonNull;

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
          final Scope enclosingScope) {
    super(resolvingConfig, enclosingScope);
  }

  public JavaSymbolTableCreator(
          final ResolvingConfiguration resolvingConfig,
          final Deque<Scope> scopeStack) {
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
    if (astCompilationUnit.isPresentPackageDeclaration()) {
      packageName = Names.getQualifiedName(astCompilationUnit.getPackageDeclaration()
              .getMCQualifiedName().getPartList());

      // ... ImportDeclaration* ...
      List<ImportStatement> importStatements = new ArrayList<>();
      // always import java.lang.*;
      importStatements.add(new ImportStatement("java.lang", true));
      for (ASTImportDeclaration astImportDeclaration : astCompilationUnit.getImportDeclarationList()) {
        String qualifiedName = Names.getQualifiedName(astImportDeclaration.getMCQualifiedName()
                .getPartList());
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
    addModifiersToType(javaClassTypeSymbol, astClassDeclaration.getModifierList());

    // ... TypeParameters? ...
    addTypeParametersToType(javaClassTypeSymbol,
            astClassDeclaration.getMCTypeParametersOpt());

    // ... ("extends" superClass:Type)? ...
    if (astClassDeclaration.isPresentSuperClass()) {
      JavaTypeSymbolReference superClassReference = new JavaTypeSymbolReference(
              Joiners.DOT.join(astClassDeclaration.getSuperClass().getNameList()),
              currentScope().get(), 0);
      javaClassTypeSymbol.setSuperClass(superClassReference);
      MCTypesJTypeSymbolsHelper.addTypeArgumentsToTypeSymbol(superClassReference, astClassDeclaration.getSuperClass(),
              javaClassTypeSymbol.getSpannedScope(), typeRefFactory);
    }
    else if(!("Object").equals(astClassDeclaration.getName())){
      JavaTypeSymbolReference superClassReference = new JavaTypeSymbolReference(
              "Object", currentScope().get(), 0);
      javaClassTypeSymbol.setSuperClass(superClassReference);
    }

    addToScopeAndLinkWithNode(javaClassTypeSymbol, astClassDeclaration);

    // ... ("implements" implementedInterfaces:(Type || ",")+)? ...
    addInterfacesToType(javaClassTypeSymbol, astClassDeclaration.getImplementedInterfaceList());

    // ... ClassBody (handled by other visit methods)

    blockNodesStack.add(astClassDeclaration);
  }

  @Override
  public void endVisit(final ASTClassDeclaration astClassDeclaration) {
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  @Override
  public void visit(final ASTInterfaceDeclaration astInterfaceDeclaration) {
    // InterfaceDeclaration implements TypeDeclaration = Modifier* "interface"
    // Name ...
    JavaTypeSymbol javaTypeDeclarationSymbol = symbolFactory.createInterfaceSymbol(
            astInterfaceDeclaration.getName());
    addModifiersToType(javaTypeDeclarationSymbol, astInterfaceDeclaration.getModifierList());

    // ... TypeParameters? ...
    addTypeParametersToType(javaTypeDeclarationSymbol,
            astInterfaceDeclaration.getMCTypeParametersOpt());

    addToScopeAndLinkWithNode(javaTypeDeclarationSymbol, astInterfaceDeclaration);

    // ... ("extends" extendedInterfaces:(Type || ",")+)? ...
    addInterfacesToType(javaTypeDeclarationSymbol, astInterfaceDeclaration.getExtendedInterfaceList());

    // ... InterfaceBody (handled by other visit methods)

    blockNodesStack.add(astInterfaceDeclaration);
  }

  @Override
  public void endVisit(final ASTInterfaceDeclaration astInterfaceDeclaration) {
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  private String enumTypeName = "";

  @Override
  public void visit(final ASTEnumDeclaration astEnumDeclaration) {
    // EnumDeclaration implements TypeDeclaration = Modifier* "enum" Name ...
    JavaTypeSymbol javaEnumTypeSymbol = symbolFactory.createEnumSymbol(astEnumDeclaration
            .getName());

    addModifiersToType(javaEnumTypeSymbol, astEnumDeclaration.getModifierList());

    // ... ("implements" (Type || ",")+)? ...
    addInterfacesToType(javaEnumTypeSymbol, astEnumDeclaration.getImplementedInterfaceList());

    // ... "{" (EnumConstantDeclaration || ",")* ","? ...

    // ... EnumBody? "}" (handled by other visit methods (ClassBody))

    // enums are implicitly subtypes from "Enum"
    JavaTypeSymbolReference superClassReference = new JavaTypeSymbolReference(
            "java.lang.Enum", currentScope().get(), 0);
    javaEnumTypeSymbol.setSuperClass(superClassReference);


    // no more nonterminals to process from here
    enumTypeName = astEnumDeclaration.getName();
    addToScopeAndLinkWithNode(javaEnumTypeSymbol, astEnumDeclaration);
    blockNodesStack.add(astEnumDeclaration);
  }

  @Override
  public void endVisit(final ASTEnumDeclaration astEnumDeclaration) {
    removeCurrentScope();
    blockNodesStack.removeLast();
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
    for (ASTAnnotation astAnnotation : astEnumConstantDeclaration.getAnnotationList()) {
      String qualifiedName = Names.getQualifiedName(astAnnotation.getAnnotationName().getPartList());
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
    addModifiersToType(javaAnnotationTypeSymbol, astAnnotationTypeDeclaration.getModifierList());

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
    addModifiersToMethod(javaAnnotationMethodSymbol, astAnnotationMethod.getModifierList());

    // ... Type ...
    JavaTypeSymbolReference javaTypeSymbolReference = null;

    final String returnTypeName = Joiners.DOT.join(astAnnotationMethod.getMCType().getNameList());
    javaTypeSymbolReference = new JavaTypeSymbolReference(returnTypeName, currentScope().get(),
            MCTypesHelper.getArrayDimensionIfArrayOrZero(astAnnotationMethod.getMCType()));

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
    for (ASTVariableDeclarator variableDeclarator : astFieldDeclaration.getVariableDeclaratorList()) {
      JavaFieldSymbol javaFieldSymbol = symbolFactory.createFieldSymbol(
              variableDeclarator.getDeclaratorId().getName(), null);

      // FieldDeclaration = Modifier* ...
      addModifiersToField(javaFieldSymbol, astFieldDeclaration.getModifierList());

      // ... Type ...
      initializeJavaAttributeSymbol(javaFieldSymbol, astFieldDeclaration.getMCType(),
              variableDeclarator.getDeclaratorId().getDimList().size());

      // ... (VariableDeclarator || ",")+
      // VariableDeclarator is handled by the for loop

      // no more nonterminals to process from here
      addToScopeAndLinkWithNode(javaFieldSymbol, astFieldDeclaration);
    }
  }

  @Override
  public void visit(final ASTConstDeclaration astConstDeclaration) {
    for (ASTConstantDeclarator astConstantDeclarator : astConstDeclaration
            .getConstantDeclaratorList()) {
      JavaFieldSymbol javaFieldSymbol = symbolFactory.createFieldSymbol(
              astConstantDeclarator.getName(), null);

      // ConstDeclaration implements InterfaceMemberDeclaration = Modifier* ...
      addModifiersToField(javaFieldSymbol, astConstDeclaration.getModifierList());

      // ... Type ...
      initializeJavaAttributeSymbol(javaFieldSymbol, astConstDeclaration.getMCType(),
              astConstantDeclarator.getDimList().size());

      // ... (ConstantDeclarator || ",")+ ";"
      // ConstantDeclaratorList is handled by the for loop

      // no more nonterminals to process from here
      addToScopeAndLinkWithNode(javaFieldSymbol, astConstDeclaration);
    }
  }

  protected void initializeJavaAttributeSymbol(JavaFieldSymbol javaFieldSymbol,
                                               ASTMCType astType, int additionalDimensions) {
    MCTypesJTypeSymbolsHelper.initializeJAttributeSymbol(javaFieldSymbol, astType, additionalDimensions,
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
    addModifiersToMethod(javaConstructorSymbol, astConstructorDeclaration.getModifierList());

    // ... TypeParameters? ...
    List<JavaTypeSymbol> javaTypeParameters = addTypeParametersToMethod(javaConstructorSymbol,
            astConstructorDeclaration.getMCTypeParametersOpt());

    // ... FormalParameters ...
    addFormalParametersToMethod(
            javaConstructorSymbol, astConstructorDeclaration.getFormalParameters()
                    .getFormalParameterListingOpt());

    // ... ("throws" Throws)? ...
    addThrowsToMethod(javaConstructorSymbol, astConstructorDeclaration.getThrowsOpt());

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
            .getVariableDeclaratorList()) {
      JavaFieldSymbol javaFieldSymbol = symbolFactory.createLocalVariableSymbol(
              variableDeclarator.getDeclaratorId().getName(), null);

      // FieldDeclaration = Modifier* ...
      addModifiersToField(javaFieldSymbol, astLocalVariableDeclaration.getPrimitiveModifierList());

      // ... Type ...
      initializeJavaAttributeSymbol(javaFieldSymbol, astLocalVariableDeclaration.getMCType(),
              variableDeclarator.getDeclaratorId().getDimList().size());

      // ... (VariableDeclarator || ",")+
      // VariableDeclarator is handled by the for loop

      // no more nonterminals to process from here
      addToScopeAndLinkWithNode(javaFieldSymbol, astLocalVariableDeclaration);
    }
  }

  @Override
  public void visit(final ASTJavaBlock astCodeBlock) {
    if (!isCurrentNodeAStatementOrMethodOrConstructor()) {
      boolean isShadowingScope = isCurrentNodeAClassOrInterfaceOrEnum();
      CommonScope commonScope = new CommonScope(isShadowingScope);
      putOnStack(commonScope);
      blockNodesStack.add(astCodeBlock);
    }
  }

  @Override
  public void endVisit(final ASTJavaBlock astCodeBlock) {
    if (!isCurrentNodeAStatementOrMethodOrConstructor()) {
      CommonScope commonScope = (CommonScope) currentScope().get();
      setLinkBetweenSpannedScopeAndNode(commonScope, astCodeBlock);
      removeCurrentScope();
      blockNodesStack.removeLast();
    }
  }

  @Override
  public void visit(final ASTTryStatement astTryClause) {
    CommonScope commonScope = new CommonScope(false);
    putOnStack(commonScope);
    setLinkBetweenSpannedScopeAndNode(commonScope, astTryClause);
    blockNodesStack.add(astTryClause);
  }

  @Override
  public void endVisit(final ASTTryStatement astTryClause) {
    astTryClause.getJavaBlock().setEnclosingScope(currentScope().get());
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  @Override
  public void visit(final ASTTryStatementWithResources astTryClause) {
    CommonScope commonScope = new CommonScope(false);
    putOnStack(commonScope);
    setLinkBetweenSpannedScopeAndNode(commonScope, astTryClause);
    for (ASTResource astResource : astTryClause.getResourceList()) {
      JavaFieldSymbol javaFieldSymbol = symbolFactory.createLocalVariableSymbol(
              astResource.getDeclaratorId().getName(), null);

      // Resource = VariableModifier* ...
      addModifiersToField(javaFieldSymbol, astResource.getPrimitiveModifierList());

      // ... ClassOrInterfaceType ...
      MCTypesJTypeSymbolsHelper.initializeJAttributeSymbol(javaFieldSymbol, astResource.getMCType(), astResource.getDeclaratorId().getDimList().size(),
              currentScope().get(), typeRefFactory);

      // no more nonterminals to process from here
      addToScopeAndLinkWithNode(javaFieldSymbol, astTryClause);
    }
    blockNodesStack.add(astTryClause);
  }

  @Override
  public void endVisit(final ASTTryStatementWithResources astTryClause) {
    astTryClause.getJavaBlock().setEnclosingScope(currentScope().get());
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  @Override
  public void visit(final ASTCatchClause astCatchClause) {
    CommonScope commonScope = new CommonScope(false);
    putOnStack(commonScope);
    setLinkBetweenSpannedScopeAndNode(commonScope, astCatchClause);
    List<ASTMCQualifiedName> qualifiedNames = astCatchClause.getCatchType().getMCQualifiedNameList();

    String qualifiedName = null;
    // if there is only one type use that type ...
    if (qualifiedNames.size() == 1) {
      qualifiedName = Names.getQualifiedName(qualifiedNames.get(0).getPartList());
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
    blockNodesStack.add(astCatchClause);
  }

  @Override
  public void endVisit(final ASTCatchClause astCatchClause) {
    astCatchClause.getJavaBlock().setEnclosingScope(currentScope().get());
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  @Override
  public void traverse(final ASTIfStatement astIfStatement) {
    if (null != astIfStatement.getCondition()) {
      astIfStatement.getCondition().accept(getRealThis());
    }
    if (null != astIfStatement.getThenStatement()) {
      CommonScope commonScope = new CommonScope(false);
      putOnStack(commonScope);
      blockNodesStack.add(astIfStatement);
      setLinkBetweenSpannedScopeAndNode(commonScope, astIfStatement);
      astIfStatement.getThenStatement().accept(getRealThis());
      astIfStatement.getThenStatement().setEnclosingScope(currentScope().get());
      removeCurrentScope();
      blockNodesStack.removeLast();
    }
    if (astIfStatement.isPresentElseStatement()) {
      CommonScope commonScope = new CommonScope(false);
      putOnStack(commonScope);
      blockNodesStack.add(astIfStatement);
      setLinkBetweenSpannedScopeAndNode(commonScope, astIfStatement);
      astIfStatement.getElseStatement().accept(getRealThis());
      astIfStatement.getElseStatement().setEnclosingScope(currentScope().get());
      removeCurrentScope();
      blockNodesStack.removeLast();
    }
  }

  @Override
  public void visit(final ASTForStatement astForStatement) {
    CommonScope commonScope = new CommonScope(false);
    putOnStack(commonScope);
    setLinkBetweenSpannedScopeAndNode(commonScope, astForStatement);
    blockNodesStack.add(astForStatement);
  }

  @Override
  public void endVisit(final ASTForStatement astForStatement) {
    astForStatement.getForControl().setEnclosingScope(currentScope().get());
    astForStatement.getStatement().setEnclosingScope(currentScope().get());
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  @Override
  public void visit(final ASTEnhancedForControl astEnhancedForControl) {
    ASTFormalParameter astFormalParameter = astEnhancedForControl.getFormalParameter();
    JavaFieldSymbol javaFieldSymbol = symbolFactory.createLocalVariableSymbol(
            astFormalParameter.getDeclaratorId().getName(), null);

    // FormalParameter = PrimitiveModifier* ...
    addModifiersToField(javaFieldSymbol, astFormalParameter.getPrimitiveModifierList());

    // ... Type ...
    initializeJavaAttributeSymbol(javaFieldSymbol, astFormalParameter.getMCType(),
            astFormalParameter.getDeclaratorId().getDimList().size());

    //no more nonterminals to process from here
    addToScopeAndLinkWithNode(javaFieldSymbol, astFormalParameter);
  }

  @Override
  public void visit(final ASTWhileStatement astWhileStatement) {
    CommonScope commonScope = new CommonScope(false);
    putOnStack(commonScope);
    setLinkBetweenSpannedScopeAndNode(commonScope, astWhileStatement);
    blockNodesStack.add(astWhileStatement);
  }

  @Override
  public void endVisit(final ASTWhileStatement astWhileStatement) {
    astWhileStatement.getCondition().setEnclosingScope(currentScope().get());
    astWhileStatement.getStatement().setEnclosingScope(currentScope().get());
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  @Override
  public void visit(final ASTDoWhileStatement astDoWhileStatement) {
    CommonScope commonScope = new CommonScope(false);
    putOnStack(commonScope);
    setLinkBetweenSpannedScopeAndNode(commonScope, astDoWhileStatement);
    blockNodesStack.add(astDoWhileStatement);
  }

  @Override
  public void endVisit(final ASTDoWhileStatement astDoWhileStatement) {
    astDoWhileStatement.getCondition().setEnclosingScope(currentScope().get());
    astDoWhileStatement.getStatement().setEnclosingScope(currentScope().get());
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  @Override
  public void visit(final ASTSwitchStatement astSwitchStatement) {
    CommonScope commonScope = new CommonScope(false);
    putOnStack(commonScope);
    setLinkBetweenSpannedScopeAndNode(commonScope, astSwitchStatement);
    blockNodesStack.add(astSwitchStatement);
  }

  @Override
  public void endVisit(final ASTSwitchStatement astSwitchStatement) {
    for(ASTSwitchBlockStatementGroup astSwitchBlock : astSwitchStatement.getSwitchBlockStatementGroupList()) {
      astSwitchBlock.setEnclosingScope(currentScope().get());
    }
    removeCurrentScope();
    blockNodesStack.removeLast();
  }

  protected boolean isCurrentNodeAStatementOrMethodOrConstructor() {
    if (!blockNodesStack.isEmpty()) {
      ASTNode currentNode = blockNodesStack.getLast();

      return (currentNode instanceof ASTIfStatement)
              || (currentNode instanceof ASTForStatement)
              || (currentNode instanceof ASTWhileStatement)
              || (currentNode instanceof ASTDoWhileStatement)
              || (currentNode instanceof ASTSwitchStatement)
              || (currentNode instanceof ASTTryStatement)
              || (currentNode instanceof ASTTryStatementWithResources)
              || (currentNode instanceof ASTCatchClause)
              || (currentNode instanceof ASTMethodDeclaration)
              || (currentNode instanceof ASTConstructorDeclaration);
    }

    return false;
  }

  protected boolean isCurrentNodeAClassOrInterfaceOrEnum() {
    if (!blockNodesStack.isEmpty()) {
      ASTNode currentNode = blockNodesStack.getLast();

      return (currentNode instanceof ASTClassDeclaration)
              || (currentNode instanceof ASTInterfaceDeclaration)
              || (currentNode instanceof ASTEnumDeclaration);
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
                Names.getQualifiedName(astAnnotation.getAnnotationName().getPartList()),
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
    addModifiersToMethod(javaMethodSymbol, methodSignature.getModifierList());

    // ... TypeParameters? ...
    List<JavaTypeSymbol> javaTypeParameters = addTypeParametersToMethod(javaMethodSymbol,
            methodSignature.getMCTypeParametersOpt());

    // ... ReturnType ...
    // ASTReturnType is either ASTVoidType or ASTType
    JavaTypeSymbolReference javaTypeSymbolReference = null;

    if (methodSignature.getMCReturnType().isPresentMCType()) {
      ASTMCType nonVoidReturnType = methodSignature.getMCReturnType().getMCType();
      final String returnTypeName = Joiners.DOT.join(nonVoidReturnType.getNameList());
      final int additionalDimensions = methodSignature.getDimList().size();
      javaTypeSymbolReference = new JavaTypeSymbolReference(returnTypeName, currentScope().get(),
              MCTypesHelper.getArrayDimensionIfArrayOrZero(nonVoidReturnType) + additionalDimensions);
      addTypeArgumentsToTypeSymbol(javaTypeSymbolReference, nonVoidReturnType);
    }
    else {
      // The return type is ASTVoidType here
      int additionalDimensions = methodSignature.getDimList().size();
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
            .getFormalParameters().getFormalParameterListingOpt();
    List<JavaFieldSymbol> javaFormalParameterSymbols = addFormalParametersToMethod(
            javaMethodSymbol, optionalFormalParameterListing);

    // ... Ellipsis
    if (optionalFormalParameterListing.isPresent()) {
      javaMethodSymbol.setEllipsisParameterMethod(
              optionalFormalParameterListing.get().isPresentLastFormalParameter());
    }

    // ... ("throws" Throws)?
    addThrowsToMethod(javaMethodSymbol, methodSignature.getThrowsOpt());

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
        String qualifiedName = Names.getQualifiedName(astAnnotation.getAnnotationName().getPartList());
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
              .getFormalParameterList()) {
        JavaFieldSymbol javaFormalParameterSymbol = addOneFormalParameterToMethod(
                javaMethodSymbol, astFormalParameter.getPrimitiveModifierList(),
                astFormalParameter.getMCType(), astFormalParameter.getDeclaratorId());

        javaFormalParameterSymbols.add(javaFormalParameterSymbol);
        setLinkBetweenSymbolAndNode(javaFormalParameterSymbol, astFormalParameter);
      }
      // add ASTLastFormalParameter (varargs)
      Optional<ASTLastFormalParameter> optionalLastFormalParameter = astFormalParameterListing
              .getLastFormalParameterOpt();
      if (optionalLastFormalParameter.isPresent()) {
        ASTLastFormalParameter astLastFormalParameter = optionalLastFormalParameter.get();

        // A vararg is an array, so convert type to an appropriate array
        ASTMCArrayType arrayType;
        ASTMCType type = astLastFormalParameter.getMCType();
        if (type instanceof ASTMCArrayType) {
          arrayType = (ASTMCArrayType) type;
          arrayType.setDimensions(arrayType.getDimensions() + 1);
        }
        else {
          arrayType = MCFullGenericTypesMill.mCArrayTypeBuilder()
                  .setMCType(type).setDimensions(1).build();
        }

        JavaFieldSymbol javaFormalParameterSymbol = addOneFormalParameterToMethod(
                javaMethodSymbol,
                astLastFormalParameter.getPrimitiveModifierList(),
                arrayType, astLastFormalParameter.getDeclaratorId());

        javaFormalParameterSymbols.add(javaFormalParameterSymbol);
        setLinkBetweenSymbolAndNode(javaFormalParameterSymbol, astLastFormalParameter);
      }
    }
    return javaFormalParameterSymbols;
  }

  protected JavaFieldSymbol addOneFormalParameterToMethod(JavaMethodSymbol javaMethodSymbol,
                                                          Iterable<? extends ASTModifier> modifiers, ASTMCType astType, ASTDeclaratorId astDeclaratorId) {
    // new JavaFieldSymbol
    final String typeName = Joiners.DOT.join(astType.getNameList());
    final int additionalDimensions = astDeclaratorId.getDimList().size();
    JavaTypeSymbolReference javaTypeSymbolReference = new JavaTypeSymbolReference(typeName,
            currentScope().get(), MCTypesHelper.getArrayDimensionIfArrayOrZero(astType)
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
                .getQualifiedName(astAnnotation.getAnnotationName().getPartList());
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
      for (ASTMCQualifiedName astQualifiedName : astThrows.getMCQualifiedNameList()) {
        String qualifiedName = Names.getQualifiedName(astQualifiedName.getPartList());

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
        String qualifiedName = Names.getQualifiedName(astAnnotation.getAnnotationName().getPartList());
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
                                                           Optional<ASTMCTypeParameters> typeParameters) {
    return MCTypesJTypeSymbolsHelper.addTypeParametersToMethod(javaMethodSymbol, typeParameters,
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
                                                         Optional<ASTMCTypeParameters> optionalTypeParameters) {
    return MCTypesJTypeSymbolsHelper.addTypeParametersToType(javaTypeSymbol, optionalTypeParameters,
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
                                     List<ASTMCType> astInterfaceTypeList) {
    MCTypesJTypeSymbolsHelper.addInterfacesToType(javaTypeSymbol, astInterfaceTypeList,
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
                                              ASTMCType astType) {
    MCTypesJTypeSymbolsHelper.addTypeArgumentsToTypeSymbol(javaTypeSymbolReference, astType,
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
