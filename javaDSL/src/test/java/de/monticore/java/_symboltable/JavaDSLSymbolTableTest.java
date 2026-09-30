/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._symboltable;

import de.monticore.expressions.uglyexpressions._ast.ASTCreatorExpression;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLScope;
import de.monticore.java.javadsl._symboltable.JavaDSLScope;
import de.monticore.java.javadsl._symboltable.TypeDeclarationSymbol;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.java.util.TestModels;
import de.monticore.java.utils.JavaDSLSymbolTableUtil;
import de.monticore.javalight._ast.ASTConstDeclaration;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTSimpleInit;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;
import static de.monticore.java.JavaDSLAssertions.assertPresent;
import static org.junit.jupiter.api.Assertions.*;

@TestWithMCLanguage(JavaDSLMill.class)
public class JavaDSLSymbolTableTest {

  @BeforeEach
  void setUp() {
    JavaDSLSymbolTableUtil.prepareMill(true);
  }

  @JavaSourceTest(basePath = TestModels.SYMBOL_TABLE)
  public void testSymbolTableCreation(Path path) {
    parseAndCreateST(path);
  }

  // package simpleTestClasses.types.*
  @Test
  public void test_simpleTestClasses_types_SimpleAnnotationTestModel() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/types/SimpleAnnotationTestModel.java");

    TypeDeclarationSymbol annotationSymbol =
        assertPresent(scope.resolveTypeDeclaration("SimpleAnnotationTestModel"), "annotation");
    assertTrue(annotationSymbol.isIsAnnotation());

    IJavaDSLScope annotationScope = onlySubScope(scope);
    MethodSymbol someMethod = resolveMethod(annotationScope, "someMethod");
    assertTrue(SymTypeRelations.isStringOrSubType(someMethod.getType()));

    MethodSymbol someMethodWithDefault = resolveMethod(annotationScope, "someMethodWithDefault");
    assertTrue(SymTypeRelations.isStringOrSubType(someMethodWithDefault.getType()));
  }

  @Test
  public void test_simpleTestClasses_types_SimpleClassTestModel() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/types/SimpleClassTestModel.java");

    TypeSymbol classSymbol =
        assertPresent(scope.resolveTypeLocally("SimpleClassTestModel"), "class");
    assertInstanceOf(ASTClassDeclaration.class, classSymbol.getAstNode());

    IJavaDSLScope classScope = onlySubScope(scope);
    assertTrue(SymTypeRelations.isInt(resolveField(classScope, "someField").getType()));
    assertTrue(resolveMethod(classScope, "someMethod").getFunctionType().getType().isVoidType());
  }

  @Test
  public void test_simpleTestClasses_types_SimpleInterfaceTestModel() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/types/SimpleInterfaceTestModel.java");

    IJavaDSLScope interfaceScope = onlySubScope(scope);
    assertTrue(SymTypeRelations.isStringOrSubType(resolveField(interfaceScope, "const1").getType()));
    assertTrue(SymTypeRelations.isStringOrSubType(resolveField(interfaceScope, "const2").getType()));
    assertTrue(resolveMethod(interfaceScope, "interfaceMethod").getFunctionType().getType().isVoidType());
  }

  // package simpleTestClasses.*
  @Test
  public void test_simpleTestClasses_EmptyClass() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/EmptyClass.java");

    IJavaDSLScope classScope = onlySubScope(scope);
    assertEquals(0, classScope.getSubScopes().size());
  }

  @Test
  public void test_simpleTestClasses_ExtendsObject() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/ExtendsObject.java");
    assertEquals(2, scope.getSubScopes().size());

    TypeSymbol extendsObject = assertPresent(scope.resolveType("ExtendsObject"), "ExtendsObject");
    assertTrue(extendsObject.isPresentSuperClass());
    assertTrue(extendsObject.getSuperClass().isGenericType());
    assertFullName("java.util.ArrayList", extendsObject.getSuperClass());

    TypeSymbol doesntExtendObject =
        assertPresent(scope.resolveType("DoesntExtendObject"), "DoesntExtendObject");
    assertFalse(doesntExtendObject.isPresentSuperClass());
  }

  @Test
  public void test_simpleTestClasses_ImportJavaLang() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/ImportJavaLang.java");

    IJavaDSLScope classScope = onlySubScope(scope);
    assertEquals(0, classScope.getSymbolsSize());
    assertEquals("java.util.ArrayList", scope.getImportsList().get(0).getStatement());
    assertFalse(scope.getImportsList().get(0).isStar());
    assertEquals("java.lang", scope.getImportsList().get(1).getStatement());
    assertTrue(scope.getImportsList().get(1).isStar());
  }

  @Test
  public void test_simpleTestClasses_HelloWorld() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/HelloWorld.java");

    IJavaDSLScope methodScope = onlySubScope(onlySubScope(scope));
    assertEquals(1, methodScope.getSymbolsSize());
    assertEquals(1, methodScope.getLocalFieldSymbols().size());

    FieldSymbol args = resolveField(methodScope, "args");
    assertEquals(methodScope.getLocalFieldSymbols().get(0), args);
    assertArrayType(1, args.getType());
  }

  @Test
  public void test_simpleTestClasses_MethodWithEllipsis() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/MethodWithEllipsis.java");

    IJavaDSLScope classBodyScope = onlySubScope(onlySubScope(scope));
    assertEquals(1, classBodyScope.getSymbolsSize());

    MethodSymbol method = resolveMethod(classBodyScope, "m");
    assertEquals(1, method.getParameterList().size());
    assertTrue(method.isIsElliptic());
  }

  @Test
  public void test_simpleTestClasses_OneFieldClass() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/OneFieldClass.java");

    IJavaDSLScope classScope = onlySubScope(scope);
    assertEquals(1, classScope.getSymbolsSize());
    assertTrue(SymTypeRelations.isInt(resolveField(classScope, "x").getType()));
  }

  @Test
  public void test_simpleTestClasses_QualifiedNameTestClass() {
    IJavaDSLArtifactScope scope = parseAndCreateST("simpleTestClasses/QualifiedNameTestClass.java");

    IJavaDSLScope classScope = onlySubScope(scope);
    assertEquals(5, classScope.getSymbolsSize());

    assertEquals("QualifiedNameTestClass",
        resolveField(classScope, "someReference").getType().print());

    SymTypeExpression innerClassType =
        resolveField(classScope, "referenceWithAnInnerClassType").getType();
    assertEquals("InnerClass", innerClassType.print());
    assertEquals("simpleTestClasses.QualifiedNameTestClass.InnerClass",
        assertPresent(innerClassType.getSourceInfo().getSourceSymbol(), "source symbol")
            .getFullName());

    SymTypeExpression genericType =
        resolveField(classScope, "referenceWithAnInnerClassTypeAndGenericType").getType();
    assertFullName("simpleTestClasses.QualifiedNameTestClass.GenericInnerClass", genericType);
    assertTrue(genericType.isGenericType());
    assertEquals(1, genericType.asGenericType().sizeArguments());
    SymTypeExpression typeArgument = genericType.asGenericType().getArgument(0);
    assertTrue(typeArgument.isObjectType());
    assertFullName("simpleTestClasses.QualifiedNameTestClass.InnerClass", typeArgument);
  }

  // package symbolTable.enums.*
  @Test
  public void test_symbolTable_enums_EnumViaJavaEnum() {
    IJavaDSLArtifactScope scope = parseAndCreateST("symbolTable/enums/EnumViaJavaEnum.java");

    OOTypeSymbol enumSymbol = assertPresent(scope.resolveOOType("EnumViaJavaEnum"), "enum");
    IOOSymbolsScope enumScope = enumSymbol.getSpannedScope();
    assertEquals(3, enumScope.getSubScopes().size());
    assertEquals(3, enumScope.getSymbolsSize());

    for (String constant : List.of("CONSTANT1", "CONSTANT2")) {
      ASTEnumConstantDeclaration declaration = assertInstanceOf(
          ASTEnumConstantDeclaration.class, resolveField(enumScope, constant).getAstNode());
      assertEquals(1, declaration.getSpannedScope().getSymbolsSize(),
          "symbols in class body of " + constant);
    }
    resolveMethod(enumScope, "method");
  }

  @Test
  public void test_symbolTable_enums_EnumViaJavaInterface() {
    IJavaDSLArtifactScope scope = parseAndCreateST("symbolTable/enums/EnumViaJavaInterface.java");

    TypeSymbol interfaceSymbol =
        assertPresent(scope.resolveType("EnumViaJavaInterface"), "interface");
    IJavaDSLScope interfaceScope =
        assertInstanceOf(JavaDSLScope.class, interfaceSymbol.getSpannedScope());
    assertEquals(3, interfaceScope.getSubScopes().size());

    resolveField(interfaceScope, "CONSTANT1");
    resolveField(interfaceScope, "CONSTANT2");
    MethodSymbol method = resolveMethod(interfaceScope, "method");
    assertEquals(1, interfaceScope.resolveMethodMany("method").size());

    // CONSTANT1 is initialized with an anonymous class that overrides method()
    ASTInterfaceBody interfaceBody =
        ((ASTInterfaceDeclaration) interfaceScope.getAstNode()).getInterfaceBody();
    ASTConstDeclaration constant1Decl =
        (ASTConstDeclaration) interfaceBody.getInterfaceBodyDeclaration(0);
    ASTSimpleInit constant1Init = (ASTSimpleInit) constant1Decl.getLocalVariableDeclaration()
        .getVariableDeclarator(0).getVariableInit();
    ASTAnonymousClass constant1Class =
        (ASTAnonymousClass) ((ASTCreatorExpression) constant1Init.getExpression()).getCreator();

    MethodSymbol overridingMethod = resolveMethod(constant1Class.getSpannedScope(), "method");
    assertNotEquals(method, overridingMethod);
  }

  // package symbolTable.resolve.*
  @Test
  public void test_symbolTable_resolve_GeneralResolveTestClass() {
    IJavaDSLArtifactScope scope = parseAndCreateST("symbolTable/resolve/GeneralResolveTestClass.java");

    IJavaDSLScope superClassScope = scope.getSubScopes().get(0);
    assertEquals("SuperClass", superClassScope.getSpanningSymbol().getName());
    IJavaDSLScope classScope = scope.getSubScopes().get(1);
    assertEquals("GeneralResolveTestClass", classScope.getSpanningSymbol().getName());
    IJavaDSLScope typeVariableScope = classScope.getSubScopes().get(0);
    assertEquals("TYPE_VARIABLE", typeVariableScope.getSpanningSymbol().getName());

    TypeSymbol typeVariable = assertPresent(classScope.resolveType("TYPE_VARIABLE"), "TYPE_VARIABLE");
    assertEquals(classScope, typeVariable.getEnclosingScope());
    assertEquals("TYPE_VARIABLE", typeVariable.getName());

    assertEquals(classScope, resolveField(classScope, "x").getEnclosingScope());

    FieldSymbol someReference = resolveField(classScope, "someReference");
    assertEquals(classScope, someReference.getEnclosingScope());
    assertTrue(someReference.getType().isTypeVariable());
    assertEquals("symbolTable.resolve.GeneralResolveTestClass.TYPE_VARIABLE",
        someReference.getType().asTypeVariable().getTypeVarSymbol().getFullName());

    MethodSymbol constructor = resolveMethod(classScope, "GeneralResolveTestClass");
    assertEquals(classScope, constructor.getEnclosingScope());
    assertTrue(SymTypeRelations.isInt(
        resolveField(constructor.getSpannedScope(), "initialX").getType()));

    MethodSymbol shadowingMethod = resolveMethod(classScope, "variableShadowingMethod");
    assertEquals(classScope, shadowingMethod.getEnclosingScope());
    assertEquals(1, shadowingMethod.getSpannedScope().getSubScopes().size());
    FieldSymbol shadowingX =
        resolveField(shadowingMethod.getSpannedScope().getSubScopes().get(0), "x");
    assertTrue(SymTypeRelations.isStringOrSubType(shadowingX.getType()));
    assertEquals(shadowingMethod.getSpannedScope(),
        shadowingX.getEnclosingScope().getEnclosingScope());

    assertTrue(classScope.resolveMethod("privateSuperMethod", BasicAccessModifier.PROTECTED)
        .isEmpty(), "private method of super class must not be visible");
    assertPresent(classScope.resolveMethod("protectedSuperMethod", BasicAccessModifier.PROTECTED),
        "protected method of super class");
  }

  @Test
  public void test_symbolTable_resolve_TypeVariableShadowingTestClass() {
    IJavaDSLArtifactScope scope =
        parseAndCreateST("symbolTable/resolve/TypeVariableShadowingTestClass.java");

    IJavaDSLScope outerMostTClassScope = scope.getSubScopes().get(0);
    assertEquals("symbolTable.resolve.T", outerMostTClassScope.getSpanningSymbol().getFullName());

    IJavaDSLScope classScope = scope.getSubScopes().get(1);
    assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass",
        classScope.getSpanningSymbol().getFullName());

    // the type variable T and the inner class T
    List<TypeSymbol> symbols = List.copyOf(classScope.resolveTypeMany("T"));
    assertEquals(2, symbols.size());
    TypeSymbol symbol1 = symbols.get(0);
    TypeSymbol symbol2 = symbols.get(1);
    assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass.T", symbol1.getFullName());
    assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass.T", symbol2.getFullName());
    assertNotEquals(symbol1, symbol2);

    SymTypeExpression innerTInstanceType =
        resolveField(classScope, "thisIsAnInstanceOfTheInnerClassT").getType();
    assertTrue(innerTInstanceType.isTypeVariable());
    assertFullName("symbolTable.resolve.TypeVariableShadowingTestClass.T", innerTInstanceType);
  }

  // package symbolTable.typeArgumentsAndParameters.*
  @Test
  public void test_symbolTable_typeArgumentsAndParameters_TypeArgumentTestClass() {
    IJavaDSLArtifactScope scope =
        parseAndCreateST("symbolTable/typeArgumentsAndParameters/TypeArgumentTestClass.java");
    final String typeVariableT = "symbolTable.typeArgumentsAndParameters.TypeArgumentTestClass.T";

    IJavaDSLScope classScope = scope.getSubScopes().get(0);
    assertEquals(12, classScope.getSymbolsSize());
    assertEquals(1, classScope.getTypeVarSymbols().size());
    assertEquals(
        List.of("wildcardOnly", "typeVariableOnly", "superTypeVariable", "extendsTypeVariable",
            "superReferenceType", "extendsReferenceType", "superIntegerArrayType",
            "extendsIntegerArrayType", "superIntArrayType", "extendsIntArrayType",
            "multipleAndRecursiveTypeArguments"),
        classScope.getFieldSymbols().values().stream().map(FieldSymbol::getName).toList());

    // Consumer<?>
    SymTypeExpression wildcardOnly = resolveField(classScope, "wildcardOnly").getType();
    assertTrue(wildcardOnly.isGenericType());
    assertTrue(wildcardOnly.asGenericType().getArgument(0).isWildcard());

    // Consumer<T>
    SymTypeExpression typeVariableOnly = resolveField(classScope, "typeVariableOnly").getType();
    assertTrue(typeVariableOnly.isGenericType());
    assertEquals(1, typeVariableOnly.asGenericType().sizeArguments());
    assertFalse(typeVariableOnly.asGenericType().getArgument(0).isWildcard());
    assertFullName(typeVariableT, typeVariableOnly.asGenericType().getArgument(0));

    // Consumer<? super T>, Consumer<? extends T>
    assertFullName(typeVariableT, assertWildcardBound(false, resolveField(classScope, "superTypeVariable").getType()));
    assertFullName(typeVariableT, assertWildcardBound(true, resolveField(classScope, "extendsTypeVariable").getType()));

    // Consumer<? super String>, Consumer<? extends String>
    assertFullName("java.lang.String", assertWildcardBound(false, resolveField(classScope, "superReferenceType").getType()));
    assertFullName("java.lang.String", assertWildcardBound(true, resolveField(classScope, "extendsReferenceType").getType()));

    // Consumer<? super Integer[]>, Consumer<? extends Integer[]>
    assertFullName("java.lang.Integer", assertArrayType(1, assertWildcardBound(false, resolveField(classScope, "superIntegerArrayType").getType())));
    assertFullName("java.lang.Integer", assertArrayType(1, assertWildcardBound(true, resolveField(classScope, "extendsIntegerArrayType").getType())));

    // Consumer<? super int[]>, Consumer<? extends int[]>
    assertTrue(SymTypeRelations.isInt(assertArrayType(1, assertWildcardBound(false, resolveField(classScope, "superIntArrayType").getType()))));
    assertTrue(SymTypeRelations.isInt(assertArrayType(1, assertWildcardBound(true, resolveField(classScope, "extendsIntArrayType").getType()))));

    // Function<Collection<?>, Set<?>>
    SymTypeExpression function =
        resolveField(classScope, "multipleAndRecursiveTypeArguments").getType();
    assertTrue(function.isGenericType());
    assertEquals(2, function.asGenericType().sizeArguments());
    SymTypeExpression collection = function.asGenericType().getArgument(0);
    assertTrue(collection.isGenericType());
    assertFullName("java.util.Collection", collection);
    assertTrue(collection.asGenericType().getArgument(0).isWildcard());
    assertFalse(collection.asGenericType().getArgument(0).asWildcard().isUpper());
    SymTypeExpression set = function.asGenericType().getArgument(1);
    assertTrue(set.isGenericType());
    assertFullName("java.util.Set", set);
    assertTrue(set.asGenericType().getArgument(0).isWildcard());
    assertFalse(set.asGenericType().getArgument(0).asWildcard().isUpper());
  }

  @Test
  @Disabled("assertions for type parameters are not implemented yet")
  public void test_symbolTable_typeArgumentsAndParameters_TypeParameterTestClass() {
    IJavaDSLArtifactScope scope =
        parseAndCreateST("symbolTable/typeArgumentsAndParameters/TypeParameterTestClass.java");

    assertPresent(scope.resolveType("TypeParameterTestClass"), "class");
    // TODO
  }

  // package symbolTable.*
  @Test
  public void test_symbolTable_ScopesSymbolTableTestClass() {
    IJavaDSLArtifactScope scope = parseAndCreateST("symbolTable/ScopesSymbolTableTestClass.java");

    assertEquals(3, scope.getSubScopes().size());
    IJavaDSLScope someInterfaceScope = scope.getSubScopes().get(0);
    assertEquals(0, someInterfaceScope.getSymbolsSize());
    IJavaDSLScope someReturnTypeScope = scope.getSubScopes().get(1);
    assertEquals(0, someReturnTypeScope.getSymbolsSize());
    IJavaDSLScope classScope = scope.getSubScopes().get(2);
    assertEquals(3, classScope.getSymbolsSize());
    IJavaDSLScope classInitializerScope = classScope.getSubScopes().get(0);
    assertEquals(1, classInitializerScope.getSymbolsSize());

    FieldSymbol classField = resolveField(classScope, "field");
    assertEquals(classScope.getFieldSymbols().values().get(0), classField);

    FieldSymbol initializerField = resolveField(classInitializerScope, "field");
    assertEquals(classInitializerScope.getFieldSymbols().values().get(0), initializerField);

    assertNotEquals(classField, initializerField);
  }

  @Test
  public void test_symbolTable_VariablesTestClass() {
    IJavaDSLArtifactScope scope = parseAndCreateST("symbolTable/VariablesTestClass.java");
    final String variablesTestClass = "symbolTable.VariablesTestClass";

    assertEquals(2, scope.getSubScopes().size());
    IJavaDSLScope classScope = scope.getSubScopes().get(0);

    // modifiers
    FieldSymbol publicValue = resolveField(classScope, "publicValue");
    assertTrue(publicValue.isIsPublic());
    assertFalse(publicValue.isIsProtected());
    assertFalse(publicValue.isIsPrivate());

    FieldSymbol protectedValue = resolveField(classScope, "protectedValue");
    assertFalse(protectedValue.isIsPublic());
    assertTrue(protectedValue.isIsProtected());
    assertFalse(protectedValue.isIsPrivate());

    FieldSymbol privateValue = resolveField(classScope, "privateValue");
    assertFalse(privateValue.isIsPublic());
    assertFalse(privateValue.isIsProtected());
    assertTrue(privateValue.isIsPrivate());

    assertTrue(resolveField(classScope, "staticValue").isIsStatic());
    assertTrue(resolveField(classScope, "finalValue").isIsFinal());

    // primitive types and arrays (leading/trailing array brackets)
    assertTrue(SymTypeRelations.isInt(resolveField(classScope, "integerValue").getType()));
    assertTrue(SymTypeRelations.isInt(
        assertArrayType(1, resolveField(classScope, "integerArray").getType())));
    assertTrue(SymTypeRelations.isInt(
        assertArrayType(1, resolveField(classScope, "integerArrayWithTrailingBrackets").getType())));
    assertTrue(SymTypeRelations.isInt(assertArrayType(2,
        resolveField(classScope, "integerMatrixWithLeadingAndTrailingBrackets").getType())));

    // reference types and arrays (leading/trailing array brackets)
    assertObjectType(variablesTestClass, resolveField(classScope, "someReference").getType());
    assertObjectType(variablesTestClass,
        assertArrayType(1, resolveField(classScope, "someReferenceArray").getType()));
    assertObjectType(variablesTestClass, assertArrayType(1,
        resolveField(classScope, "someReferenceArrayWithTrailingBrackets").getType()));
    assertObjectType(variablesTestClass, assertArrayType(2,
        resolveField(classScope, "someReferenceMatrixWithLeadingAndTrailingBrackets").getType()));

    // someMethod
    MethodSymbol someMethod = resolveMethod(classScope, "someMethod");
    assertEquals(1, someMethod.getParameterList().size());
    VariableSymbol parameter = assertPresent(
        someMethod.getSpannedScope().resolveVariable("someMethodParameter"), "someMethodParameter");
    assertEquals(someMethod.getParameterList().get(0), parameter);

    IJavaDSLScope methodBodyScope = classScope.getSubScopes().get(0).getSubScopes().get(0);
    assertTrue(SymTypeRelations.isInt(resolveField(methodBodyScope, "localVariable").getType()));
    assertTrue(methodBodyScope.resolveField("i").isEmpty(),
        "loop variable must not be visible outside of the loop");
  }

  @Test
  public void test_symbolTable_typevariableUpperbounds() {
    IJavaDSLArtifactScope scope = parseAndCreateST(TestModels.resource("generics/IComplexComponent.java"));

    TypeSymbol interfaceSymbol =
        assertPresent(scope.resolveTypeLocally("IComplexComponent"), "interface");
    assertEquals(2, interfaceSymbol.getTypeParameterList().size());

    TypeVarSymbol k = interfaceSymbol.getTypeParameterList().get(0);
    assertEquals("generics.IComplexComponent.K", k.getFullName());
    assertFalse(k.isPresentSuperClass());

    TypeVarSymbol v = interfaceSymbol.getTypeParameterList().get(1);
    assertEquals("generics.IComplexComponent.V", v.getFullName());
    assertTrue(v.isPresentSuperClass());
    assertEquals(1, v.getSuperTypesList().size());
    assertFullName("java.lang.Number", v.getSuperTypes(0));

    assertSame(k, assertPresent(interfaceSymbol.getSpannedScope().resolveTypeVar("K"), "K"));
    assertSame(v, assertPresent(interfaceSymbol.getSpannedScope().resolveTypeVar("V"), "V"));
  }

  @Test
  public void testMethodParametersAndLocalVariablesAreDefinedInSameScope() {
    IJavaDSLArtifactScope scope =
        parseAndCreateST("symbolTable/MethodParametersAndLocalVariablesAreDefinedInSameScope.java");

    TypeSymbol typeSymbol = assertPresent(
        scope.resolveType("MethodParametersAndLocalVariablesAreDefinedInSameScope"), "class");
    JavaMethodSymbol method = assertPresent(
        ((JavaDSLScope) typeSymbol.getSpannedScope()).resolveJavaMethod("testMethod"),
        "testMethod");

    assertEquals(1, method.getSpannedScope().getSymbolsSize());
    assertEquals(1, method.getSpannedScope().getSubScopes().size());
    assertEquals(2, method.getSpannedScope().getSubScopes().get(0).getSymbolsSize());
  }

  // helpers

  /** Parses a model relative to {@link TestModels#PARSABLE_AND_COMPILABLE}. */
  private static IJavaDSLArtifactScope parseAndCreateST(String model) {
    return parseAndCreateST(Path.of(TestModels.PARSABLE_AND_COMPILABLE, model));
  }

  private static IJavaDSLArtifactScope parseAndCreateST(Path model) {
    ASTCompilationUnit ast = assertParsingSuccess(model);
    return JavaDSLSymbolTableUtil.buildSymbolTable(ast);
  }

  /** Asserts that the scope has exactly one sub scope and returns it. */
  private static IJavaDSLScope onlySubScope(IJavaDSLScope scope) {
    assertEquals(1, scope.getSubScopes().size(), "number of sub scopes");
    return scope.getSubScopes().get(0);
  }

  private static FieldSymbol resolveField(IOOSymbolsScope scope, String name) {
    return assertPresent(scope.resolveField(name), "field " + name);
  }

  private static MethodSymbol resolveMethod(IOOSymbolsScope scope, String name) {
    return assertPresent(scope.resolveMethod(name), "method " + name);
  }

  private static void assertFullName(String expected, SymTypeExpression type) {
    assertEquals(expected, type.getTypeInfo().getFullName());
  }

  private static void assertObjectType(String expectedFullName, SymTypeExpression type) {
    assertTrue(type.isObjectType(), () -> type.print() + " is not an object type");
    assertFullName(expectedFullName, type);
  }

  /** Asserts that the type is an array with the given dimension and returns its element type. */
  private static SymTypeExpression assertArrayType(int expectedDim, SymTypeExpression type) {
    assertTrue(type.isArrayType(), () -> type.print() + " is not an array type");
    assertEquals(expectedDim, type.asArrayType().getDim(), "array dimension");
    return type.asArrayType().getArgument();
  }

  /**
   * Asserts that the type is a generic type with a single bounded wildcard argument (e.g.,
   * {@code Consumer<? super T>}) and returns the bound.
   *
   * @param upper {@code true} for {@code ? extends}, {@code false} for {@code ? super}
   */
  private static SymTypeExpression assertWildcardBound(boolean upper, SymTypeExpression type) {
    assertTrue(type.isGenericType(), () -> type.print() + " is not a generic type");
    assertEquals(1, type.asGenericType().sizeArguments(), "number of type arguments");
    SymTypeExpression argument = type.asGenericType().getArgument(0);
    assertTrue(argument.isWildcard(), () -> argument.print() + " is not a wildcard");
    assertEquals(upper, argument.asWildcard().isUpper(), "wildcard is upper bound");
    return argument.asWildcard().getBound();
  }
}
