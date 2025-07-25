package de.monticore.java._symboltable;

import de.monticore.expressions.uglyexpressions._ast.ASTCreatorExpression;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._symboltable.*;
import de.monticore.java.utils.JavaDSLSymbolTableUtil;
import de.monticore.javalight._ast.ASTConstDeclaration;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTSimpleInit;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.IOOSymbolsScope;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symboltable.modifiers.BasicAccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.SymTypeRelations;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.*;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;
import static org.junit.jupiter.api.Assertions.*;

@TestWithMCLanguage(JavaDSLMill.class)
public class JavaDSLSymbolTableTest {
  
  @BeforeEach
  void setUp() {
    JavaDSLSymbolTableUtil.prepareMill(true);
  }
  
  @ParameterizedTest
  @ValueSource(strings = {
      // Symbol table
      "src/test/resources/parsableAndCompilableModels/symbolTable/enums/EnumViaJavaEnum.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/enums/EnumViaJavaInterface.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/resolve/GeneralResolveTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/resolve/TypeVariableShadowingTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/typeArgumentsAndParameters/TypeArgumentTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/typeArgumentsAndParameters/TypeParameterTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/MethodParametersAndLocalVariablesAreDefinedInSameScope.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/ScopesSymbolTableTestClass.java",
      "src/test/resources/parsableAndCompilableModels/symbolTable/VariablesTestClass.java" })
  public void testSymbolTableCreation(String path) {
    parseAndCreateST(path);
  }
  
  protected IJavaDSLArtifactScope parseAndCreateST(String path) {
    ASTCompilationUnit ast = assertParsingSuccess(path);
    return JavaDSLSymbolTableUtil.buildSymbolTable(ast);
  }
  
  // package simpleTestClasses.types.*
  @Test
  public void test_simpleTestClasses_types_SimpleAnnotationTestModel()
      throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleAnnotationTestModel.java");
    
    Optional<TypeDeclarationSymbol> annotationSymbol =
        scope.resolveTypeDeclaration("SimpleAnnotationTestModel");
    assertTrue(annotationSymbol.isPresent());
    assertTrue(annotationSymbol.get().isIsAnnotation());
    
    assertEquals(1, scope.getSubScopes().size());
    
    IJavaDSLScope annotationScope = scope.getSubScopes().get(0);
    Optional<MethodSymbol> someMethodSymbol = annotationScope.resolveMethod("someMethod");
    assertTrue(someMethodSymbol.isPresent());
    assertTrue(SymTypeRelations.isStringOrSubType(someMethodSymbol.get().getType()));
    
    Optional<MethodSymbol> someMethodWithDefaultSymbol =
        annotationScope.resolveMethod("someMethodWithDefault");
    assertTrue(someMethodWithDefaultSymbol.isPresent());
    assertTrue(SymTypeRelations.isStringOrSubType(someMethodSymbol.get().getType()));
  }
  
  @Test
  public void test_simpleTestClasses_types_SimpleClassTestModel()
      throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleClassTestModel.java");
    
    Optional<TypeSymbol> classSymbol = scope.resolveTypeLocally("SimpleClassTestModel");
    assertTrue(classSymbol.isPresent());
    assertInstanceOf(ASTClassDeclaration.class, classSymbol.get().getAstNode());
    assertEquals(1, scope.getSubScopes().size());
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    
    Optional<FieldSymbol> someFieldSymbol = classScope.resolveField("someField");
    assertTrue(someFieldSymbol.isPresent());
    assertTrue(SymTypeRelations.isInt(someFieldSymbol.get().getType()));
    
    Optional<MethodSymbol> someMethodSymbol = classScope.resolveMethod("someMethod");
    assertTrue(someMethodSymbol.isPresent());
    assertTrue(someMethodSymbol.get().getFunctionType().getType().isVoidType());
  }
  
  @Test
  public void test_simpleTestClasses_types_SimpleInterfaceTestModel()
      throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleInterfaceTestModel.java");
    
    assertEquals(1, scope.getSubScopes().size());
    IJavaDSLScope interfaceScope = scope.getSubScopes().get(0);
    
    Optional<FieldSymbol> const1Symbol = interfaceScope.resolveField("const1");
    assertTrue(const1Symbol.isPresent());
    assertTrue(SymTypeRelations.isStringOrSubType(const1Symbol.get().getType()));
    
    Optional<FieldSymbol> const2Symbol = interfaceScope.resolveField("const2");
    assertTrue(const2Symbol.isPresent());
    assertTrue(SymTypeRelations.isStringOrSubType(const2Symbol.get().getType()));
    
    Optional<MethodSymbol> interfaceMethodSymbol = interfaceScope.resolveMethod("interfaceMethod");
    assertTrue(interfaceMethodSymbol.isPresent());
    assertTrue(interfaceMethodSymbol.get().getFunctionType().getType().isVoidType());
  }
  
  // package simpleTestClasses.*
  @Test
  public void test_simpleTestClasses_EmptyClass() throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/EmptyClass.java");
    assertEquals(1, scope.getSubScopes().size());
    
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    assertEquals(0, classScope.getSubScopes().size());
    // TODO: check
    //assertEquals(0, Scopes.getLocalSymbolsAsCollection(classScope).size());
  }
  
  @Test
  public void test_simpleTestClasses_ExtendsObject() throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/ExtendsObject.java");
    assertEquals(2, scope.getSubScopes().size());
    
    Optional<TypeSymbol> class1Symbol = scope.resolveType("ExtendsObject");
    assertTrue(class1Symbol.isPresent());
    assertTrue(class1Symbol.get().isPresentSuperClass());
    assertTrue(class1Symbol.get().getSuperClass().isGenericType());
    assertEquals("java.util.ArrayList",
        class1Symbol.get().getSuperClass().getTypeInfo().getFullName());
    Optional<TypeSymbol> class2Symbol = scope.resolveType("DoesntExtendObject");
    assertTrue(class2Symbol.isPresent());
    assertFalse(class2Symbol.get().isPresentSuperClass());
  }
  
  @Test
  public void test_simpleTestClasses_ImportJavaLang() throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/ImportJavaLang.java");
    assertEquals(1, scope.getSubScopes().size());
    
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    assertEquals(0, classScope.getSymbolsSize());
    assertEquals("java.util.ArrayList", scope.getImportsList().get(0).getStatement());
    assertFalse(scope.getImportsList().get(0).isStar());
    assertEquals("java.lang", scope.getImportsList().get(1).getStatement());
    assertTrue(scope.getImportsList().get(1).isStar());
  }
  
  @Test
  public void test_simpleTestClasses_HelloWorld() throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/HelloWorld.java");
    assertEquals(1, scope.getSubScopes().size());
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    assertEquals(1, classScope.getSubScopes().size());
    IJavaDSLScope methodScope = classScope.getSubScopes().get(0);
    assertEquals(1, methodScope.getSymbolsSize());
    
    assertEquals(1, methodScope.getLocalFieldSymbols().size());
    FieldSymbol argsSymbol = methodScope.getLocalFieldSymbols().get(0);
    
    Optional<FieldSymbol> resolvedArgsSymbol = methodScope.resolveField("args");
    assertTrue(resolvedArgsSymbol.isPresent());
    assertEquals(argsSymbol, resolvedArgsSymbol.get());
    assertTrue(resolvedArgsSymbol.get().getType().isArrayType());
    assertEquals(1, resolvedArgsSymbol.get().getType().asArrayType().getDim());
  }
  
  @Test
  public void test_simpleTestClasses_MethodWithEllipsis() throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/MethodWithEllipsis.java");
    assertEquals(1, scope.getSubScopes().size());
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    assertEquals(1, classScope.getSubScopes().size());
    IJavaDSLScope classBodyScope = classScope.getSubScopes().get(0);
    assertEquals(1, classBodyScope.getSymbolsSize());
    
    Optional<MethodSymbol> methodSymbol = classBodyScope.resolveMethod("m");
    assertTrue(methodSymbol.isPresent());
    assertEquals(1, methodSymbol.get().getParameterList().size());
    assertTrue(methodSymbol.get().isIsElliptic());
  }
  
  @Test
  public void test_simpleTestClasses_OneFieldClass() throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/OneFieldClass.java");
    assertEquals(1, scope.getSubScopes().size());
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    assertEquals(1, classScope.getSymbolsSize());
    
    Optional<FieldSymbol> resolvedIntegerSymbol = classScope.resolveField("x");
    assertTrue(resolvedIntegerSymbol.isPresent());
    assertTrue(SymTypeRelations.isInt(resolvedIntegerSymbol.get().getType()));
  }
  
  @Test
  public void test_simpleTestClasses_QualifiedNameTestClass() throws RecognitionException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/simpleTestClasses/QualifiedNameTestClass.java");
    assertEquals(1, scope.getSubScopes().size());
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    assertEquals(5, classScope.getSymbolsSize());
    
    Optional<FieldSymbol> symbol = classScope.resolveField("someReference");
    assertTrue(symbol.isPresent());
    assertEquals("QualifiedNameTestClass", symbol.get().getType().print());
    
    Optional<FieldSymbol> symbol2 = classScope.resolveField("referenceWithAnInnerClassType");
    assertTrue(symbol2.isPresent());
    assertEquals("InnerClass", symbol2.get().getType().print());
    assertEquals("simpleTestClasses.QualifiedNameTestClass.InnerClass",
        symbol2.get().getType().getSourceInfo().getSourceSymbol().get().getFullName());
    
    Optional<FieldSymbol> symbol3 =
        classScope.resolveField("referenceWithAnInnerClassTypeAndGenericType");
    assertTrue(symbol3.isPresent());
    assertEquals("simpleTestClasses.QualifiedNameTestClass.GenericInnerClass",
        symbol3.get().getType().getTypeInfo().getFullName());
    assertTrue(symbol3.get().getType().isGenericType());
    assertEquals(1, symbol3.get().getType().asGenericType().sizeArguments());
    assertTrue(symbol3.get().getType().asGenericType().getArgument(0).isObjectType());
    assertEquals("simpleTestClasses.QualifiedNameTestClass.InnerClass",
        symbol3.get().getType().asGenericType().getArgument(0).getTypeInfo().getFullName());
  }
  
  @Test
  public void test_symbolTable_enums_EnumViaJavaEnum() {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/enums/EnumViaJavaEnum.java");
    Optional<OOTypeSymbol> enumViaJavaEnumSymbol = scope.resolveOOType("EnumViaJavaEnum");
    assertTrue(enumViaJavaEnumSymbol.isPresent());
    
    IOOSymbolsScope enumViaJavaEnumSymbolScope = enumViaJavaEnumSymbol.get().getSpannedScope();
    assertEquals(3, enumViaJavaEnumSymbolScope.getSubScopes().size());
    assertEquals(3, enumViaJavaEnumSymbolScope.getSymbolsSize());
    
    Optional<FieldSymbol> constant1Symbol = enumViaJavaEnumSymbolScope.resolveField("CONSTANT1");
    assertTrue(constant1Symbol.isPresent());
    assertInstanceOf(ASTEnumConstantDeclaration.class, constant1Symbol.get().getAstNode());
    ASTEnumConstantDeclaration constant1Declaration =
        (ASTEnumConstantDeclaration) constant1Symbol.get().getAstNode();
    
    IBasicSymbolsScope constant1ClassScope = constant1Declaration.getSpannedScope();
    assertEquals(1, constant1ClassScope.getSymbolsSize());
    
    Optional<FieldSymbol> constant2Symbol = enumViaJavaEnumSymbolScope.resolveField("CONSTANT2");
    assertTrue(constant2Symbol.isPresent());
    ASTEnumConstantDeclaration constant2Declaration =
        (ASTEnumConstantDeclaration) constant2Symbol.get().getAstNode();
    IBasicSymbolsScope constant2ClassScope = constant2Declaration.getSpannedScope();
    assertEquals(1, constant2ClassScope.getSymbolsSize());
    Optional<MethodSymbol> methodSymbol = enumViaJavaEnumSymbolScope.resolveMethod("method");
    assertTrue(methodSymbol.isPresent());
  }
  
  @Test
  public void test_symbolTable_enums_EnumViaJavaInterface() throws RecognitionException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/enums/EnumViaJavaInterface.java");
    Optional<TypeSymbol> enumViaJavaInterfaceSymbol = scope.resolveType("EnumViaJavaInterface");
    assertTrue(enumViaJavaInterfaceSymbol.isPresent());
    assertInstanceOf(JavaDSLScope.class, enumViaJavaInterfaceSymbol.get().getSpannedScope());
    IJavaDSLScope interfaceScope =
        (IJavaDSLScope) enumViaJavaInterfaceSymbol.get().getSpannedScope();
    assertEquals(3, interfaceScope.getSubScopes().size());
    
    Optional<FieldSymbol> constant1Symbol = interfaceScope.resolveField("CONSTANT1");
    assertTrue(constant1Symbol.isPresent());
    Optional<FieldSymbol> constant2Symbol = interfaceScope.resolveField("CONSTANT2");
    assertTrue(constant2Symbol.isPresent());
    Optional<MethodSymbol> methodSymbol = interfaceScope.resolveMethod("method");
    assertTrue(methodSymbol.isPresent());
    Collection<MethodSymbol> methodSymbols = interfaceScope.resolveMethodMany("method");
    assertEquals(1, methodSymbols.size());
    
    ASTInterfaceBody interfaceBody =
        ((ASTInterfaceDeclaration) interfaceScope.getAstNode()).getInterfaceBody();
    ASTConstDeclaration constant1Decl =
        (ASTConstDeclaration) interfaceBody.getInterfaceBodyDeclaration(0);
    ASTSimpleInit constant1Init =
        (ASTSimpleInit) constant1Decl.getLocalVariableDeclaration().getVariableDeclarator(0)
            .getVariableInit();
    ASTAnonymousClass constant1Class =
        (ASTAnonymousClass) ((ASTCreatorExpression) constant1Init.getExpression()).getCreator();
    IJavaDSLScope constant1ClassScope = constant1Class.getSpannedScope();
    Optional<MethodSymbol> constant1MethodSymbol = constant1ClassScope.resolveMethod("method");
    assertTrue(constant1MethodSymbol.isPresent());
    assertNotEquals(methodSymbol, constant1MethodSymbol.get());
  }
  
  @Test //TODO: was disabled
  public void test_symbolTable_resolve_GeneralResolveTestClass()
      throws RecognitionException, IOException {
    // There should be four scopes (not counting global scope)
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/resolve/GeneralResolveTestClass.java");
    IJavaDSLScope superClassScope = scope.getSubScopes().get(0);
    assertEquals("SuperClass", superClassScope.getSpanningSymbol().getName());
    IJavaDSLScope classScope = scope.getSubScopes().get(1);
    assertEquals("GeneralResolveTestClass", classScope.getSpanningSymbol().getName());
    IJavaDSLScope typeVariableScope = classScope.getSubScopes().get(0);
    assertEquals("TYPE_VARIABLE", typeVariableScope.getSpanningSymbol().getName());
    
    // resolve tests
    // resolve: TYPE_VARIABLE
    Optional<TypeSymbol> typeVariableSymbol = classScope.resolveType("TYPE_VARIABLE");
    assertTrue(typeVariableSymbol.isPresent());
    assertEquals(typeVariableSymbol.get().getEnclosingScope(), classScope);
    assertEquals("TYPE_VARIABLE", typeVariableSymbol.get().getName());
    
    // resolve: x
    Optional<FieldSymbol> integerAttribute = classScope.resolveField("x");
    assertTrue(integerAttribute.isPresent());
    assertEquals(integerAttribute.get().getEnclosingScope(), classScope);
    
    // resolve: someReference
    Optional<FieldSymbol> someReferenceSymbol = classScope.resolveField("someReference");
    assertTrue(someReferenceSymbol.isPresent());
    assertEquals(someReferenceSymbol.get().getEnclosingScope(), classScope);
    assertTrue(someReferenceSymbol.get().getType().isTypeVariable());
    assertEquals("symbolTable.resolve.GeneralResolveTestClass.TYPE_VARIABLE",
        someReferenceSymbol.get().getType().asTypeVariable().getTypeVarSymbol().getFullName());
    
    // resolve: GeneralResolveTestClass
    Optional<MethodSymbol> constructorSymbol = classScope.resolveMethod("GeneralResolveTestClass");
    assertTrue(constructorSymbol.isPresent());
    assertEquals(constructorSymbol.get().getEnclosingScope(), classScope);
    Optional<FieldSymbol> initialXSymbol =
        constructorSymbol.get().getSpannedScope().resolveField("initialX");
    assertTrue(initialXSymbol.isPresent());
    assertTrue(SymTypeRelations.isInt(initialXSymbol.get().getType()));
    
    // resolve: variableShadowingMethod
    Optional<MethodSymbol> variableShadowingMethodSymbol =
        classScope.resolveMethod("variableShadowingMethod");
    assertTrue(variableShadowingMethodSymbol.isPresent());
    assertEquals(variableShadowingMethodSymbol.get().getEnclosingScope(), classScope);
    assertEquals(1, variableShadowingMethodSymbol.get().getSpannedScope().getSubScopes().size());
    Optional<FieldSymbol> shadowingXSymbol =
        variableShadowingMethodSymbol.get().getSpannedScope().getSubScopes().get(0)
            .resolveField("x");
    assertTrue(shadowingXSymbol.isPresent());
    assertTrue(SymTypeRelations.isStringOrSubType(shadowingXSymbol.get().getType()));
    assertEquals(variableShadowingMethodSymbol.get().getSpannedScope(),
        shadowingXSymbol.get().getEnclosingScope().getEnclosingScope());
    
    // resolve: privateSuperMethod
    Optional<MethodSymbol> optionalPrivateSuperMethod =
        classScope.resolveMethod("privateSuperMethod", BasicAccessModifier.PROTECTED);
    assertFalse(optionalPrivateSuperMethod.isPresent());
    
    // resolve: protectedSuperMethod
    Optional<MethodSymbol> protectedSuperMethod =
        classScope.resolveMethod("protectedSuperMethod", BasicAccessModifier.PROTECTED);
    assertTrue(protectedSuperMethod.isPresent());
  }
  
  @Test
  public void test_symbolTable_resolve_TypeVariableShadowingTestClass()
      throws RecognitionException, IOException {
    // There should be nine scopes (not counting global scope)
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/resolve/TypeVariableShadowingTestClass.java");
    IJavaDSLScope outerMostTClassScope = scope.getSubScopes().get(0);
    assertEquals("symbolTable.resolve.T", outerMostTClassScope.getSpanningSymbol().getFullName());
    
    IJavaDSLScope typeVariableShadowingTestClassScope = scope.getSubScopes().get(1);
    assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass",
        typeVariableShadowingTestClassScope.getSpanningSymbol().getFullName());
    Collection<TypeSymbol> symbols = typeVariableShadowingTestClassScope.resolveTypeMany("T");
    assertEquals(2, symbols.size());
    Iterator<TypeSymbol> iterator = symbols.iterator();
    TypeSymbol symbol1 = iterator.next();
    assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass.T", symbol1.getFullName());
    TypeSymbol symbol2 = iterator.next();
    assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass.T", symbol2.getFullName());
    assertNotEquals(symbol1, symbol2);
    
    Optional<FieldSymbol> classTInstanceSymbol =
        typeVariableShadowingTestClassScope.resolveField("thisIsAnInstanceOfTheInnerClassT");
    assertTrue(classTInstanceSymbol.isPresent());
    assertTrue(classTInstanceSymbol.get().getType().isTypeVariable());
    assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass.T",
        classTInstanceSymbol.get().getType().getTypeInfo().getFullName());
  }
  
  // package symbolTable.typeArgumentsAndParameters.*
  @Test
  public void test_symbolTable_typeArgumentsAndParameters_TypeArgumentTestClass()
      throws RecognitionException, IOException {
    IJavaDSLArtifactScope artifactScope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/typeArgumentsAndParameters/TypeArgumentTestClass.java");
    IJavaDSLScope globalScope = artifactScope.getEnclosingScope();
    IJavaDSLScope scope = globalScope.getSubScopes().get(0);
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    assertEquals(12, classScope.getSymbolsSize());
    
    List<TypeVarSymbol> typeVarSymbols = classScope.getTypeVarSymbols().values();
    assertEquals(1, typeVarSymbols.size());
    
    List<FieldSymbol> fieldSymbols = classScope.getFieldSymbols().values();
    assertEquals(11, fieldSymbols.size());
    
    FieldSymbol wildcardOnlySymbol = fieldSymbols.get(0);
    Optional<FieldSymbol> symbol = classScope.resolveField("wildcardOnly");
    assertTrue(symbol.isPresent());
    assertEquals(wildcardOnlySymbol, symbol.get());
    assertTrue(wildcardOnlySymbol.getType().isGenericType());
    assertTrue(wildcardOnlySymbol.getType().asGenericType().getArgument(0).isWildcard());
    
    FieldSymbol typeVariableOnlySymbol = fieldSymbols.get(1);
    Optional<FieldSymbol> symbol2 = classScope.resolveField("typeVariableOnly");
    assertTrue(symbol2.isPresent());
    assertEquals(typeVariableOnlySymbol, symbol2.get());
    SymTypeExpression type2 = typeVariableOnlySymbol.getType();
    assertTrue(type2.isGenericType());
    assertEquals(1, type2.asGenericType().sizeArguments());
    assertFalse(type2.asGenericType().getArgument(0).isWildcard());
    assertEquals("symbolTable.typeArgumentsAndParameters.TypeArgumentTestClass.T",
        type2.asGenericType().getArgument(0).getTypeInfo().getFullName());
    
    FieldSymbol superTypeVariableSymbol = fieldSymbols.get(2);
    Optional<FieldSymbol> symbol3 = classScope.resolveField("superTypeVariable");
    assertTrue(symbol3.isPresent());
    assertEquals(superTypeVariableSymbol, symbol3.get());
    SymTypeExpression type3 = superTypeVariableSymbol.getType();
    assertTrue(type3.isGenericType());
    assertEquals(1, type3.asGenericType().sizeArguments());
    assertTrue(type3.asGenericType().getArgument(0).isWildcard());
    assertFalse(type3.asGenericType().getArgument(0).asWildcard().isUpper());
    assertEquals("symbolTable.typeArgumentsAndParameters.TypeArgumentTestClass.T",
        type3.asGenericType().getArgument(0).asWildcard().getBound().getTypeInfo().getFullName());
    
    FieldSymbol extendsTypeVariableSymbol = fieldSymbols.get(3);
    Optional<FieldSymbol> symbol4 = classScope.resolveField("extendsTypeVariable");
    assertTrue(symbol4.isPresent());
    assertEquals(extendsTypeVariableSymbol, symbol4.get());
    SymTypeExpression type4 = extendsTypeVariableSymbol.getType();
    assertTrue(type4.isGenericType());
    assertEquals(1, type4.asGenericType().sizeArguments());
    assertTrue(type4.asGenericType().getArgument(0).isWildcard());
    assertTrue(type4.asGenericType().getArgument(0).asWildcard().isUpper());
    assertEquals("symbolTable.typeArgumentsAndParameters.TypeArgumentTestClass.T",
        type4.asGenericType().getArgument(0).asWildcard().getBound().getTypeInfo().getFullName());
    
    FieldSymbol superReferenceTypeSymbol = fieldSymbols.get(4);
    Optional<FieldSymbol> symbol5 = classScope.resolveField("superReferenceType");
    assertTrue(symbol5.isPresent());
    assertEquals(superReferenceTypeSymbol, symbol5.get());
    SymTypeExpression type5 = superReferenceTypeSymbol.getType();
    assertTrue(type5.isGenericType());
    assertEquals(1, type5.asGenericType().sizeArguments());
    assertTrue(type5.asGenericType().getArgument(0).isWildcard());
    assertFalse(type5.asGenericType().getArgument(0).asWildcard().isUpper());
    assertEquals("java.lang.String",
        type5.asGenericType().getArgument(0).asWildcard().getBound().getTypeInfo().getFullName());
    
    FieldSymbol extendsReferenceTypeSymbol = fieldSymbols.get(5);
    Optional<FieldSymbol> symbol6 = classScope.resolveField("extendsReferenceType");
    assertTrue(symbol6.isPresent());
    assertEquals(extendsReferenceTypeSymbol, symbol6.get());
    SymTypeExpression type6 = extendsReferenceTypeSymbol.getType();
    assertTrue(type6.isGenericType());
    assertEquals(1, type6.asGenericType().sizeArguments());
    assertTrue(type6.asGenericType().getArgument(0).isWildcard());
    assertTrue(type6.asGenericType().getArgument(0).asWildcard().isUpper());
    assertEquals("java.lang.String",
        type6.asGenericType().getArgument(0).asWildcard().getBound().getTypeInfo().getFullName());
    
    FieldSymbol superIntegerArrayTypeSymbol = fieldSymbols.get(6);
    Optional<FieldSymbol> symbol7 = classScope.resolveField("superIntegerArrayType");
    assertTrue(symbol7.isPresent());
    assertEquals(superIntegerArrayTypeSymbol, symbol7.get());
    SymTypeExpression type7 = superIntegerArrayTypeSymbol.getType();
    assertTrue(type7.isGenericType());
    assertEquals(1, type7.asGenericType().sizeArguments());
    assertTrue(type7.asGenericType().getArgument(0).isWildcard());
    assertFalse(type7.asGenericType().getArgument(0).asWildcard().isUpper());
    assertTrue(type7.asGenericType().getArgument(0).asWildcard().getBound().isArrayType());
    assertEquals("java.lang.Integer",
        type7.asGenericType().getArgument(0).asWildcard().getBound().asArrayType().getArgument()
            .getTypeInfo().getFullName());
    assertEquals(1,
        type7.asGenericType().getArgument(0).asWildcard().getBound().asArrayType().getDim());
    
    FieldSymbol extendsIntegerArrayTypeSymbol = fieldSymbols.get(7);
    Optional<FieldSymbol> symbol8 = classScope.resolveField("extendsIntegerArrayType");
    assertTrue(symbol8.isPresent());
    assertEquals(extendsIntegerArrayTypeSymbol, symbol8.get());
    SymTypeExpression type8 = extendsIntegerArrayTypeSymbol.getType();
    assertTrue(type8.isGenericType());
    assertEquals(1, type8.asGenericType().sizeArguments());
    assertTrue(type8.asGenericType().getArgument(0).isWildcard());
    assertTrue(type8.asGenericType().getArgument(0).asWildcard().isUpper());
    assertTrue(type8.asGenericType().getArgument(0).asWildcard().getBound().isArrayType());
    assertEquals("java.lang.Integer",
        type8.asGenericType().getArgument(0).asWildcard().getBound().asArrayType().getArgument()
            .getTypeInfo().getFullName());
    assertEquals(1,
        type8.asGenericType().getArgument(0).asWildcard().getBound().asArrayType().getDim());
    
    FieldSymbol superIntArrayTypeSymbol = fieldSymbols.get(8);
    Optional<FieldSymbol> symbol9 = classScope.resolveField("superIntArrayType");
    assertTrue(symbol9.isPresent());
    assertEquals(superIntArrayTypeSymbol, symbol9.get());
    SymTypeExpression type9 = superIntArrayTypeSymbol.getType();
    assertTrue(type9.isGenericType());
    assertEquals(1, type9.asGenericType().sizeArguments());
    assertTrue(type9.asGenericType().getArgument(0).isWildcard());
    assertFalse(type9.asGenericType().getArgument(0).asWildcard().isUpper());
    assertTrue(type9.asGenericType().getArgument(0).asWildcard().getBound().isArrayType());
    assertTrue(SymTypeRelations.isInt(
        type9.asGenericType().getArgument(0).asWildcard().getBound().asArrayType().getArgument()));
    assertEquals(1,
        type9.asGenericType().getArgument(0).asWildcard().getBound().asArrayType().getDim());
    
    FieldSymbol extendsIntArrayTypeSymbol = fieldSymbols.get(9);
    Optional<FieldSymbol> symbol10 = classScope.resolveField("extendsIntArrayType");
    assertTrue(symbol10.isPresent());
    assertEquals(extendsIntArrayTypeSymbol, symbol10.get());
    SymTypeExpression type10 = extendsIntArrayTypeSymbol.getType();
    assertTrue(type10.isGenericType());
    assertEquals(1, type10.asGenericType().sizeArguments());
    assertTrue(type10.asGenericType().getArgument(0).isWildcard());
    assertTrue(type10.asGenericType().getArgument(0).asWildcard().isUpper());
    assertTrue(type10.asGenericType().getArgument(0).asWildcard().getBound().isArrayType());
    assertTrue(SymTypeRelations.isInt(
        type10.asGenericType().getArgument(0).asWildcard().getBound().asArrayType().getArgument()));
    assertEquals(1,
        type10.asGenericType().getArgument(0).asWildcard().getBound().asArrayType().getDim());
    
    FieldSymbol multipleAndRecursiveTypeArgumentsSymbol = fieldSymbols.get(10);
    Optional<FieldSymbol> symbol11 = classScope.resolveField("multipleAndRecursiveTypeArguments");
    assertTrue(symbol11.isPresent());
    assertEquals(multipleAndRecursiveTypeArgumentsSymbol, symbol11.get());
    SymTypeExpression type11 = multipleAndRecursiveTypeArgumentsSymbol.getType();
    assertTrue(type11.isGenericType());
    assertEquals(2, type11.asGenericType().sizeArguments());
    assertTrue(type11.asGenericType().getArgument(0).isGenericType());
    assertEquals("java.util.Collection",
        type11.asGenericType().getArgument(0).asGenericType().getTypeInfo().getFullName());
    assertTrue(type11.asGenericType().getArgument(0).asGenericType().getArgument(0).isWildcard());
    assertFalse(type11.asGenericType().getArgument(0).asGenericType().getArgument(0).asWildcard()
        .isUpper());
    
    assertTrue(type11.asGenericType().getArgument(1).isGenericType());
    assertEquals("java.util.Set",
        type11.asGenericType().getArgument(1).asGenericType().getTypeInfo().getFullName());
    assertTrue(type11.asGenericType().getArgument(1).asGenericType().getArgument(0).isWildcard());
    assertFalse(type11.asGenericType().getArgument(1).asGenericType().getArgument(0).asWildcard()
        .isUpper());
  }
  
  @Test
  @Disabled
  public void test_symbolTable_typeArgumentsAndParameters_TypeParameterTestClass()
      throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/typeArgumentsAndParameters/TypeParameterTestClass");
    
    Optional<TypeSymbol> resolve = scope.resolveType("TypeParameterTestClass");
    assertTrue(resolve.isPresent());
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    // TODO
  }
  
  // package symbolTable.*
  @Test
  public void test_symbolTable_ScopesSymbolTableTestClass()
      throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/ScopesSymbolTableTestClass.java");
    assertEquals(3, scope.getSubScopes().size());
    IJavaDSLScope someInterfaceScope = scope.getSubScopes().get(0);
    assertEquals(0, someInterfaceScope.getSymbolsSize());
    IJavaDSLScope someReturnTypeScope = scope.getSubScopes().get(1);
    assertEquals(0, someReturnTypeScope.getSymbolsSize());
    IJavaDSLScope generalSymbolTableTestClassScope = scope.getSubScopes().get(2);
    assertEquals(3, generalSymbolTableTestClassScope.getSymbolsSize());
    IJavaDSLScope classInitializerScope = generalSymbolTableTestClassScope.getSubScopes().get(0);
    assertEquals(1, classInitializerScope.getSymbolsSize());
    
    Optional<FieldSymbol> resolvedClassField =
        generalSymbolTableTestClassScope.resolveField("field");
    assertTrue(resolvedClassField.isPresent());
    assertEquals(generalSymbolTableTestClassScope.getFieldSymbols().values().get(0),
        resolvedClassField.get());
    
    Optional<FieldSymbol> resolvedInitializerField = classInitializerScope.resolveField("field");
    assertTrue(resolvedInitializerField.isPresent());
    assertEquals(classInitializerScope.getFieldSymbols().values().get(0),
        resolvedInitializerField.get());
    
    assertNotEquals(resolvedClassField.get(), resolvedInitializerField.get());
  }
  
  @Test
  public void test_symbolTable_VariablesTestClass() throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/VariablesTestClass.java");
    assertEquals(2, scope.getSubScopes().size());
    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    
    Optional<FieldSymbol> publicValueSymbol = classScope.resolveField("publicValue");
    assertTrue(publicValueSymbol.isPresent());
    assertTrue(publicValueSymbol.get().isIsPublic());
    assertFalse(publicValueSymbol.get().isIsProtected());
    assertFalse(publicValueSymbol.get().isIsPrivate());
    
    Optional<FieldSymbol> protectedValueSymbol = classScope.resolveField("protectedValue");
    assertTrue(protectedValueSymbol.isPresent());
    assertFalse(protectedValueSymbol.get().isIsPublic());
    assertTrue(protectedValueSymbol.get().isIsProtected());
    assertFalse(protectedValueSymbol.get().isIsPrivate());
    
    Optional<FieldSymbol> privateValueSymbol = classScope.resolveField("privateValue");
    assertTrue(privateValueSymbol.isPresent());
    assertFalse(privateValueSymbol.get().isIsPublic());
    assertFalse(privateValueSymbol.get().isIsProtected());
    assertTrue(privateValueSymbol.get().isIsPrivate());
    
    Optional<FieldSymbol> staticValueSymbol = classScope.resolveField("staticValue");
    assertTrue(staticValueSymbol.isPresent());
    assertTrue(staticValueSymbol.get().isIsStatic());
    
    Optional<FieldSymbol> finalValueSymbol = classScope.resolveField("finalValue");
    assertTrue(finalValueSymbol.isPresent());
    assertTrue(finalValueSymbol.get().isIsFinal());
    
    // various primitive types and primitive array combination (leading/trailing
    // array brackets)
    
    Optional<FieldSymbol> symbol = classScope.resolveField("integerValue");
    assertTrue(symbol.isPresent());
    assertTrue(SymTypeRelations.isInt(symbol.get().getType()));
    
    Optional<FieldSymbol> symbol2 = classScope.resolveField("integerArray");
    assertTrue(symbol2.isPresent());
    assertTrue(symbol2.get().getType().isArrayType());
    assertTrue(SymTypeRelations.isInt(symbol2.get().getType().asArrayType().getArgument()));
    assertEquals(1, symbol2.get().getType().asArrayType().getDim());
    
    Optional<FieldSymbol> symbol3 = classScope.resolveField("integerArrayWithTrailingBrackets");
    assertTrue(symbol3.isPresent());
    assertTrue(symbol3.get().getType().isArrayType());
    assertTrue(SymTypeRelations.isInt(symbol3.get().getType().asArrayType().getArgument()));
    assertEquals(1, symbol3.get().getType().asArrayType().getDim());
    
    Optional<FieldSymbol> symbol4 =
        classScope.resolveField("integerMatrixWithLeadingAndTrailingBrackets");
    assertTrue(symbol4.isPresent());
    assertTrue(symbol4.get().getType().isArrayType());
    assertTrue(SymTypeRelations.isInt(symbol4.get().getType().asArrayType().getArgument()));
    assertEquals(2, symbol4.get().getType().asArrayType().getDim());
    
    // various reference types and complex array combination (leading/trailing
    // array brackets)
    
    Optional<FieldSymbol> symbol5 = classScope.resolveField("someReference");
    assertTrue(symbol5.isPresent());
    assertTrue(symbol5.get().getType().isObjectType());
    assertEquals("symbolTable.VariablesTestClass",
        symbol5.get().getType().asObjectType().getTypeInfo().getFullName());
    
    Optional<FieldSymbol> symbol6 = classScope.resolveField("someReferenceArray");
    assertTrue(symbol6.isPresent());
    assertTrue(symbol6.get().getType().isArrayType());
    assertTrue(symbol6.get().getType().asArrayType().getArgument().isObjectType());
    assertEquals("symbolTable.VariablesTestClass",
        symbol6.get().getType().asArrayType().getArgument().asObjectType().getTypeInfo()
            .getFullName());
    assertEquals(1, symbol6.get().getType().asArrayType().getDim());
    
    Optional<FieldSymbol> symbol7 =
        classScope.resolveField("someReferenceArrayWithTrailingBrackets");
    assertTrue(symbol7.isPresent());
    assertTrue(symbol7.get().getType().isArrayType());
    assertTrue(symbol7.get().getType().asArrayType().getArgument().isObjectType());
    assertEquals("symbolTable.VariablesTestClass",
        symbol7.get().getType().asArrayType().getArgument().asObjectType().getTypeInfo()
            .getFullName());
    assertEquals(1, symbol7.get().getType().asArrayType().getDim());
    
    Optional<FieldSymbol> symbol8 =
        classScope.resolveField("someReferenceMatrixWithLeadingAndTrailingBrackets");
    assertTrue(symbol8.isPresent());
    assertTrue(symbol8.get().getType().isArrayType());
    assertTrue(symbol8.get().getType().asArrayType().getArgument().isObjectType());
    assertEquals("symbolTable.VariablesTestClass",
        symbol8.get().getType().asArrayType().getArgument().asObjectType().getTypeInfo()
            .getFullName());
    assertEquals(2, symbol8.get().getType().asArrayType().getDim());
    
    // someMethod
    
    Optional<MethodSymbol> methodSymbol = classScope.resolveMethod("someMethod");
    assertTrue(methodSymbol.isPresent());
    assertEquals(1, methodSymbol.get().getParameterList().size());
    VariableSymbol javaParameterSymbol = methodSymbol.get().getParameterList().get(0);
    Optional<VariableSymbol> resolvedJavaParameterSymbol =
        methodSymbol.get().getSpannedScope().resolveVariable("someMethodParameter");
    assertTrue(resolvedJavaParameterSymbol.isPresent());
    assertEquals(javaParameterSymbol, resolvedJavaParameterSymbol.get());
    IJavaDSLScope methodScope = classScope.getSubScopes().get(0).getSubScopes().get(0);
    Optional<FieldSymbol> localVariableSymbol = methodScope.resolveField("localVariable");
    assertTrue(localVariableSymbol.isPresent());
    assertTrue(SymTypeRelations.isInt(localVariableSymbol.get().getType()));
    
    assertTrue(methodScope.resolveField("i").isEmpty());
  }
  
  @Test
  public void test_symbolTable_typevariableUpperbounds() throws RecognitionException, IOException {
    IJavaDSLArtifactScope scope =
        parseAndCreateST("src/test/resources/generics/IComplexComponent.java");
    
    Optional<TypeSymbol> interf = scope.resolveTypeLocally("IComplexComponent");
    assertTrue(interf.isPresent());
    
    assertEquals(2, interf.get().getTypeParameterList().size());
    final TypeVarSymbol kTypeSymbol = interf.get().getTypeParameterList().get(0);
    assertEquals("generics.IComplexComponent.K", kTypeSymbol.getFullName());
    assertFalse(kTypeSymbol.isPresentSuperClass());
    final TypeVarSymbol vTypeSymbol = interf.get().getTypeParameterList().get(1);
    assertEquals("generics.IComplexComponent.V", vTypeSymbol.getFullName());
    assertTrue(vTypeSymbol.isPresentSuperClass());
    assertEquals(1, vTypeSymbol.getSuperTypesList().size());
    assertEquals("java.lang.Number", vTypeSymbol.getSuperTypes(0).getTypeInfo().getFullName());
    
    Optional<TypeVarSymbol> resolvedK = interf.get().getSpannedScope().resolveTypeVar("K");
    Optional<TypeVarSymbol> resolvedV = interf.get().getSpannedScope().resolveTypeVar("V");
    assertTrue(resolvedK.isPresent());
    assertTrue(resolvedV.isPresent());
    assertSame(kTypeSymbol, resolvedK.get());
    assertSame(vTypeSymbol, resolvedV.get());
  }
  
  @Test
  public void testMethodParametersAndLocalVariablesAreDefinedInSameScope() throws IOException {
    IJavaDSLArtifactScope scope = parseAndCreateST(
        "src/test/resources/parsableAndCompilableModels/symbolTable/MethodParametersAndLocalVariablesAreDefinedInSameScope.java");
    
    Optional<TypeSymbol> typeSymbol =
        scope.resolveType("MethodParametersAndLocalVariablesAreDefinedInSameScope");
    assertTrue(typeSymbol.isPresent());
    
    Optional<JavaMethodSymbol> method =
        ((JavaDSLScope) typeSymbol.get().getSpannedScope()).resolveJavaMethod("testMethod");
    assertTrue(method.isPresent());
    
    assertEquals(1, method.get().getSpannedScope().getSymbolsSize());
    assertEquals(1, method.get().getSpannedScope().getSubScopes().size());
    assertEquals(2, method.get().getSpannedScope().getSubScopes().get(0).getSymbolsSize());
  }
}
