/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.antlr.v4.runtime.RecognitionException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.symboltable.ArtifactScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Scopes;
import de.monticore.symboltable.Symbol;
import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.types.JTypeSymbol;
import de.monticore.symboltable.types.references.ActualTypeArgument;

/**
 * A simple test for java tool.
 *
 * @author (last commit) $Author$
 */
public class JavaDSLSymbolTableTest extends AbstractTestClass {
  
  // package parserBugs.*
  @Test
  public void test_ParserBugs_ExplicitGenericInvocations()
      throws RecognitionException, IOException {
    parse("src/test/resources/parsableAndCompilableModels",
        "parserBugs/ExplicitGenericInvocations");
  }
  
  @Test
  public void test_ParserBugs_InterfaceWithStaticMethod() throws RecognitionException, IOException {
    parse("src/test/resources/parsableAndCompilableModels",
        "parserBugs/InterfaceWithStaticMethod");
  }
  
  // package simpleTestClasses.types.*
  @Test
  public void test_simpleTestClasses_types_SimpleAnnotationTestModel() throws RecognitionException,
      IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/types/SimpleAnnotationTestModel");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    
    Symbol annotationSymbol = artifactScope.resolveLocally("SimpleAnnotationTestModel",
        JavaTypeSymbol.KIND).orElse(null);
    assertTrue(annotationSymbol != null);
    assertTrue(annotationSymbol.getAstNode().get() instanceof ASTAnnotationTypeDeclaration);
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope annotationScope = artifactScope.getSubScopes().get(0);
    {
      JavaMethodSymbol someMethodSymbol = annotationScope.<JavaMethodSymbol> resolve("someMethod",
          JavaMethodSymbol.KIND).orElse(null);
      assertTrue(someMethodSymbol != null);
      assertEquals("String", someMethodSymbol.getReturnType().getName());
    }
    {
      JavaMethodSymbol someMethodWithDefaultSymbol = annotationScope.<JavaMethodSymbol> resolve(
          "someMethodWithDefault", JavaMethodSymbol.KIND).orElse(null);
      assertTrue(someMethodWithDefaultSymbol != null);
      assertEquals("String", someMethodWithDefaultSymbol.getReturnType().getName());
    }
  }
  
  @Test
  public void test_simpleTestClasses_types_SimpleClassTestModel() throws RecognitionException,
      IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/types/SimpleClassTestModel");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    
    Symbol classSymbol = artifactScope.resolveLocally("SimpleClassTestModel",
        JavaTypeSymbol.KIND).orElse(null);
    assertTrue(classSymbol != null);
    assertTrue(classSymbol.getAstNode().get() instanceof ASTClassDeclaration);
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope classScope = artifactScope.getSubScopes().get(0);
    {
      JavaFieldSymbol someFieldSymbol = classScope.<JavaFieldSymbol> resolve("someField",
          JavaFieldSymbol.KIND).orElse(null);
      assertTrue(someFieldSymbol != null);
      assertEquals("int", someFieldSymbol.getType().getName());
    }
    {
      JavaMethodSymbol someMethodSymbol = classScope.<JavaMethodSymbol> resolve("someMethod",
          JavaMethodSymbol.KIND).orElse(null);
      assertTrue(someMethodSymbol != null);
      assertEquals("void", someMethodSymbol.getReturnType().getName());
    }
  }
  
  @Test
  public void test_simpleTestClasses_types_SimpleEnumTestModels() throws RecognitionException,
      IOException {
    parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/types/SimpleEnumTestModels");
  }
  
  @Test
  public void test_simpleTestClasses_types_SimpleInterfaceTestModel()
      throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/types/SimpleInterfaceTestModel");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope interfaceScope = artifactScope.getSubScopes().get(0);
    {
      JavaFieldSymbol const1Symbol = interfaceScope.<JavaFieldSymbol> resolve("const1",
          JavaFieldSymbol.KIND).orElse(null);
      assertTrue(const1Symbol != null);
      assertEquals("String", const1Symbol.getType().getName());
    }
    {
      JavaFieldSymbol const2Symbol = interfaceScope.<JavaFieldSymbol> resolve("const2",
          JavaFieldSymbol.KIND).orElse(null);
      assertTrue(const2Symbol != null);
      assertEquals("String", const2Symbol.getType().getName());
    }
    {
      JavaMethodSymbol interfaceMethodSymbol = interfaceScope.<JavaMethodSymbol> resolve(
          "interfaceMethod", JavaMethodSymbol.KIND).orElse(null);
      assertTrue(interfaceMethodSymbol != null);
      assertEquals("void", interfaceMethodSymbol.getReturnType().getName());
    }
  }
  
  // package simpleTestClasses.*
  @Test
  public void test_simpleTestClasses_EmptyClass() throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/EmptyClass");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope classScope = artifactScope.getSubScopes().get(0);
    assertEquals(0, classScope.getSubScopes().size());
    assertEquals(0, Scopes.getLocalSymbolsAsCollection(classScope).size());
  }
  
  @Test
  public void test_simpleTestClasses_ExtendsObject() throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources", 
        "parsableAndCompilableModels/simpleTestClasses/ExtendsObject");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    assertEquals(2, artifactScope.getSubScopes().size());
    JavaTypeSymbol class1Symbol = artifactScope.<JavaTypeSymbol> resolve("ExtendsObject", 
        JavaTypeSymbol.KIND).orElse(null);
    assertTrue(class1Symbol != null);
    assertEquals("Object", class1Symbol.getSuperClass().get().getName());
    JavaTypeSymbol class2Symbol = artifactScope.<JavaTypeSymbol> resolve("DoesntExtendObject", 
        JavaTypeSymbol.KIND).orElse(null);
    assertTrue(class2Symbol != null);
    assertFalse("Object".equals(class2Symbol.getSuperClass().get().getName()));
  }
  
  @Test
  public void test_simpleTestClasses_ImportJavaLang() throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels", 
        "simpleTestClasses/ImportJavaLang");
    ArtifactScope artifactScope = (ArtifactScope) astCompilationUnit.getEnclosingScope();
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope classScope = artifactScope.getSubScopes().get(0);
    assertEquals(0, classScope.getLocalSymbols().size());
    assertEquals("java.lang", artifactScope.getImports().get(0).getStatement());
    assertTrue(artifactScope.getImports().get(0).isStar());
    assertEquals("java.util.ArrayList", artifactScope.getImports().get(1).getStatement());
    assertFalse(artifactScope.getImports().get(1).isStar());
  }
  
  @Test
  public void test_simpleTestClasses_HelloWorld() throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/HelloWorld");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope classScope = artifactScope.getSubScopes().get(0);
    assertEquals(1, classScope.getSubScopes().size());
    Scope methodScope = classScope.getSubScopes().get(0);
    assertEquals(1, methodScope.getLocalSymbols().size());
    Entry<String, Collection<Symbol>> entry = methodScope.getLocalSymbols().entrySet().iterator()
        .next();
    assertEquals(1, entry.getValue().size());
    Symbol argsSymbol = entry.getValue().iterator().next();
    JavaFieldSymbol resolvedArgsSymbol = methodScope.<JavaFieldSymbol> resolve("args",
        JavaFieldSymbol.KIND).orElse(null);
    assertTrue(resolvedArgsSymbol != null);
    assertEquals(resolvedArgsSymbol, argsSymbol);
    assertTrue(resolvedArgsSymbol.isParameter());
    assertEquals("String", resolvedArgsSymbol.getType().getName());
    assertEquals(1, resolvedArgsSymbol.getType().getDimension());
  }
  
  @Test
  public void test_simpleTestClasses_MethodWithEllipsis() throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/MethodWithEllipsis");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope classScope = artifactScope.getSubScopes().get(0);
    assertEquals(1, classScope.getSubScopes().size());
    assertEquals(1, classScope.getLocalSymbols().size());
    
    Optional<JavaMethodSymbol> methodSymbol = classScope.resolve("m",
        JavaMethodSymbol.JavaMethodSymbolKind.KIND);
    assertTrue(methodSymbol.isPresent());
    assertEquals(1, methodSymbol.get().getParameters().size());
    assertTrue(methodSymbol.get().isEllipsisParameterMethod());
  }
  
  @Test
  public void test_simpleTestClasses_OneFieldClass() throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/OneFieldClass");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope classScope = artifactScope.getSubScopes().get(0);
    assertEquals(1, classScope.getSymbolsSize());
    Collection<Collection<Symbol>> values = classScope.getLocalSymbols().values();
    Symbol integerSymbol = values.iterator().next().iterator().next();
    JavaFieldSymbol resolvedIntegerSymbol = classScope.<JavaFieldSymbol> resolve("x",
        JavaFieldSymbol.KIND).orElse(null);
    assertEquals(integerSymbol, resolvedIntegerSymbol);
    // assertEquals(0, resolvedIntegerSymbol.getType().getReferencedSymbol().getDimension());
    // assertEquals("int", resolvedIntegerSymbol.getType().getReferencedSymbol().getFullName());
    assertEquals(0, resolvedIntegerSymbol.getType().getDimension());
  }
  
  @Test
  public void test_simpleTestClasses_QualifiedNameTestClass() throws RecognitionException,
      IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/QualifiedNameTestClass");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    assertEquals(1, artifactScope.getSubScopes().size());
    Scope classScope = artifactScope.getSubScopes().get(0);
    assertEquals(5, classScope.getSymbolsSize());
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve("someReference",
          JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("QualifiedNameTestClass", symbol.getType().getName());
    }
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "referenceWithAnInnerClassType", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("InnerClass", symbol.getType().getName());
      assertEquals("simpleTestClasses.QualifiedNameTestClass.InnerClass", symbol.getType().getReferencedSymbol().getFullName());
    }
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "referenceWithAnInnerClassTypeAndGenericType", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("GenericInnerClass", symbol.getType().getName());
      Assert.assertEquals(1, symbol.getType().getActualTypeArguments().size());
    }
  }
  
  
  // package stressfulPackage.*
  
  @Test
  public void test_symbolTable_enums_EnumViaJavaEnum() throws RecognitionException,
      IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/enums/EnumViaJavaEnum");
    Scope artifactScpope = astCompilationUnit.getEnclosingScope();
    JavaTypeSymbol enumViaJavaEnumSymbol = artifactScpope.<JavaTypeSymbol> resolve(
        "EnumViaJavaEnum", JavaTypeSymbol.KIND).orElse(null);
    assertTrue(enumViaJavaEnumSymbol != null);
    Scope enumScope = enumViaJavaEnumSymbol.getSpannedScope();
    assertEquals(3, enumScope.getSubScopes().size());
//    assertEquals(5, enumScope.getLocalSymbols().size());
    assertEquals(3, enumScope.getLocalSymbols().size());
    
    JavaTypeSymbol constant1ClassSymbol = enumScope.<JavaTypeSymbol> resolve("CONSTANT1",
        JavaTypeSymbol.KIND).orElse(null);
    assertTrue(constant1ClassSymbol != null);
    JavaFieldSymbol constant1Symbol = enumScope.<JavaFieldSymbol> resolve("CONSTANT1",
        JavaFieldSymbol.KIND).orElse(null);
    assertTrue(constant1Symbol != null);
    Scope constant1Scope = constant1ClassSymbol.getSpannedScope();
    assertEquals(1, constant1Scope.getLocalSymbols().size());
    JavaTypeSymbol constant2ClassSymbol = enumScope.<JavaTypeSymbol> resolve("CONSTANT2",
        JavaTypeSymbol.KIND).orElse(null);
    assertTrue(constant2ClassSymbol != null);
    JavaFieldSymbol constant2Symbol = enumScope.<JavaFieldSymbol> resolve("CONSTANT2",
        JavaFieldSymbol.KIND).orElse(null);
    assertTrue(constant2Symbol != null);
    Scope constant2Scope = constant2ClassSymbol.getSpannedScope();
    assertEquals(1, constant2Scope.getLocalSymbols().size());
    JavaMethodSymbol methodSymbol = enumScope.<JavaMethodSymbol> resolve("method",
        JavaMethodSymbol.KIND).orElse(null);
    assertTrue(methodSymbol != null);
  }
  
  @Test
  public void test_symbolTable_enums_EnumViaJavaInterface()
      throws RecognitionException,
      IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/enums/EnumViaJavaInterface");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    JavaTypeSymbol enumViaJavaInterfaceSymbol = artifactScope.<JavaTypeSymbol> resolve(
        "EnumViaJavaInterface", JavaTypeSymbol.KIND).orElse(null);
    assertTrue(enumViaJavaInterfaceSymbol != null);
    Scope interfaceScope = enumViaJavaInterfaceSymbol.getSpannedScope();
    assertEquals(3, interfaceScope.getSubScopes().size());
    System.out.println(interfaceScope.getLocalSymbols().size());
    
    JavaFieldSymbol constant1Symbol = interfaceScope.<JavaFieldSymbol> resolve("CONSTANT1",
        JavaFieldSymbol.KIND).orElse(null);
    assertTrue(constant1Symbol != null);
    JavaFieldSymbol constant2Symbol = interfaceScope.<JavaFieldSymbol> resolve("CONSTANT2",
        JavaFieldSymbol.KIND).orElse(null);
    assertTrue(constant2Symbol != null);
//    JavaMethodSymbol methodSymbol = interfaceScope.<JavaMethodSymbol> resolve("method",
//        JavaMethodSymbol.KIND).orElse(null);
    Collection<JavaMethodSymbol> methodSymbols = interfaceScope.<JavaMethodSymbol> resolveMany("method", 
        JavaMethodSymbol.KIND);
    assertTrue(methodSymbols != null);
  }
  
  @Test
  @Ignore
  public void test_symbolTable_resolve_GeneralResolveTestClass() throws RecognitionException,
      IOException {
    // There should be four scopes (not counting global scope)
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/resolve/GeneralResolveTestClass");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    Scope superClassScope = artifactScope.getSubScopes().get(0);
    assertEquals("SuperClass", superClassScope.getSpanningSymbol().get().getName());
    Scope classScope = artifactScope.getSubScopes().get(1);
    assertEquals("GeneralResolveTestClass", classScope.getSpanningSymbol().get().getName());
    Scope typeVariableScope = classScope.getSubScopes().get(0);
    assertEquals("TYPE_VARIABLE", typeVariableScope.getSpanningSymbol().get().getName());
    
    // resolve tests
    { // resolve: TYPE_VARIABLE
      JavaTypeSymbol typeVariableSymbol = classScope.<JavaTypeSymbol> resolve(
          "TYPE_VARIABLE", JavaTypeSymbol.KIND).orElse(null);
      assertTrue(typeVariableSymbol != null);
      assertEquals(typeVariableSymbol.getEnclosingScope(), classScope);
      JavaTypeSymbol typeVariableSymbol2 = typeVariableSymbol;
      assertEquals(typeVariableSymbol2.getName(), "TYPE_VARIABLE");
    }
    { // resolve: x
      JavaFieldSymbol integerAttribute = classScope.<JavaFieldSymbol> resolve("x",
          JavaFieldSymbol.KIND).orElse(null);
      assertTrue(integerAttribute != null);
      assertEquals(integerAttribute.getEnclosingScope(), classScope);
    }
    { // resolve: someReference
      JavaFieldSymbol someReferenceSymbol = classScope.<JavaFieldSymbol> resolve(
          "someReference", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(someReferenceSymbol != null);
      assertEquals(someReferenceSymbol.getEnclosingScope(), classScope);
      assertEquals(someReferenceSymbol.getType().getName(), "TYPE_VARIABLE");
    }
    { // resolve: GeneralResolveTestClass
      JavaMethodSymbol constructorSymbol = classScope.<JavaMethodSymbol> resolve(
          "GeneralResolveTestClass", JavaMethodSymbol.KIND).orElse(null);
      assertTrue(constructorSymbol != null);
      assertEquals(constructorSymbol.getEnclosingScope(), classScope);
      JavaFieldSymbol initialXSymbol = constructorSymbol.getSpannedScope()
          .<JavaFieldSymbol> resolve("initialX", JavaFieldSymbol.KIND).orElse(null);
      assertEquals("int", initialXSymbol.getType().getName());
    }
    { // resolve: variableShadowingMethod
      JavaMethodSymbol variableShadowingMethodSymbol = classScope.<JavaMethodSymbol> resolve(
          "variableShadowingMethod", JavaMethodSymbol.KIND).orElse(null);
      assertTrue(variableShadowingMethodSymbol != null);
      assertEquals(variableShadowingMethodSymbol.getEnclosingScope(), classScope);
      JavaFieldSymbol shadowingXSymbol = variableShadowingMethodSymbol.getSpannedScope()
          .<JavaFieldSymbol> resolve("x", JavaFieldSymbol.KIND).orElse(null);
      assertEquals("String", shadowingXSymbol.getType().getName());
      assertEquals(variableShadowingMethodSymbol.getSpannedScope(),
          shadowingXSymbol.getEnclosingScope());
    }
    { // resolve: privateSuperMethod
      JavaMethodSymbol optionalPrivateSuperMethod = classScope.<JavaMethodSymbol> resolve(
          "privateSuperMethod", JavaMethodSymbol.KIND).orElse(null);
      assertFalse(optionalPrivateSuperMethod != null);
    }
    { // resolve: protectedSuperMethod
      JavaMethodSymbol protectedSuperMethod = classScope.<JavaMethodSymbol> resolve(
          "protectedSuperMethod", JavaMethodSymbol.KIND).orElse(null);
      assertTrue(protectedSuperMethod!=null);
    }
  }
  
  @Test
  public void test_symbolTable_resolve_TypeVariableShadowingTestClass()
      throws RecognitionException, IOException {
    // There should be nine scopes (not counting global scope)
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/resolve/TypeVariableShadowingTestClass");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    Scope outerMostTClassScope = artifactScope.getSubScopes().get(0);
    assertEquals("T", outerMostTClassScope.getSpanningSymbol().get().getName());
    {
      Scope TypeVariableShadowingTestClassScope = artifactScope.getSubScopes().get(1);
      assertEquals("TypeVariableShadowingTestClass", TypeVariableShadowingTestClassScope
          .getSpanningSymbol().get().getName());
      {
        Scope typeVariableShadowingTestClassScope = artifactScope.getSubScopes().get(1);
        Collection<? extends JavaTypeSymbol> symbols = typeVariableShadowingTestClassScope
            .<JavaTypeSymbol> resolveMany("T", JavaTypeSymbol.KIND);
        assertEquals(2, symbols.size());
        Iterator<? extends JavaTypeSymbol> iterator = symbols.iterator();
        JavaTypeSymbol symbol1 = iterator.next();
        assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass.T", symbol1.getFullName());
        JavaTypeSymbol symbol2 = iterator.next();
        assertEquals("symbolTable.resolve.TypeVariableShadowingTestClass.T", symbol2.getFullName());
        
        JavaFieldSymbol classTInstanceSymbol = typeVariableShadowingTestClassScope
            .<JavaFieldSymbol> resolve("thisIsAnInstanceOfTheInnerClassT",
                JavaFieldSymbol.KIND)
            .orElse(null);
        assertEquals("T", classTInstanceSymbol.getType().getName());
        // TODO resolving finds two symbols, thats expected
        // JavaTypeSymbol orElse =
        // typeVariableShadowingTestClassScope.<JavaTypeSymbol> resolve("T",
        // JavaTypeSymbol.KIND).orElse(null);
        // assertTrue(orElse == null);
        // TODO
      }
    }
  }
  
  // package symbolTable.typeArgumentsAndParameters.*
  @Test
  public void test_symbolTable_typeArgumentsAndParameters_TypeArgumentTestClass()
      throws RecognitionException,
      IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/typeArgumentsAndParameters/TypeArgumentTestClass");
    Scope globalScope = astCompilationUnit.getEnclosingScope().getEnclosingScope().get();
    Scope artifactScope = globalScope.getSubScopes().get(0);
    Scope classScope = artifactScope.getSubScopes().get(0);
    Set<Entry<String, Collection<Symbol>>> entry = classScope.getLocalSymbols().entrySet();
    assertEquals(12, entry.size());
    
    List<Symbol> symbols = new ArrayList<>();
    for (Entry<String, Collection<Symbol>> value : entry) {
      assertEquals(1, value.getValue().size());
      symbols.add(value.getValue().iterator().next());
    }
    assertEquals(12, symbols.size());
    {
      Symbol typeVariable = symbols.get(0);
      assertTrue(typeVariable instanceof JavaTypeSymbol);
    }
    {
      JavaFieldSymbol wildcardOnlySymbol = (JavaFieldSymbol) symbols.get(1);
      Symbol symbol = classScope.resolve("wildcardOnly", JavaFieldSymbol.KIND).get();
      assertEquals(wildcardOnlySymbol, symbol);
    }
    {
      JavaFieldSymbol typeVariableOnlySymbol = (JavaFieldSymbol) symbols.get(2);
      Symbol symbol = classScope.resolve("typeVariableOnly", JavaFieldSymbol.KIND).get();
      assertEquals(typeVariableOnlySymbol, symbol);
      JavaTypeSymbolReference type = typeVariableOnlySymbol.getType();
      assertEquals(1, type.getActualTypeArguments().size());
      assertFalse(type.getActualTypeArguments().get(0).isLowerBound());
      assertFalse(type.getActualTypeArguments().get(0).isUpperBound());
      assertEquals("T", type.getActualTypeArguments().get(0).getType().getName());
    }
    {
      JavaFieldSymbol superTypeVariableSymbol = (JavaFieldSymbol) symbols.get(3);
      Symbol symbol = classScope.resolve("superTypeVariable", JavaFieldSymbol.KIND).get();
      assertEquals(superTypeVariableSymbol, symbol);
      JavaTypeSymbolReference type = superTypeVariableSymbol.getType();
      assertEquals(1, type.getActualTypeArguments().size());
      assertTrue(type.getActualTypeArguments().get(0).isLowerBound());
      assertFalse(type.getActualTypeArguments().get(0).isUpperBound());
      assertEquals("T", type.getActualTypeArguments().get(0).getType().getName());
    }
    {
      JavaFieldSymbol extendsTypeVariableSymbol = (JavaFieldSymbol) symbols.get(4);
      Symbol symbol = classScope.resolve("extendsTypeVariable", JavaFieldSymbol.KIND).get();
      assertEquals(extendsTypeVariableSymbol, symbol);
      JavaTypeSymbolReference type = extendsTypeVariableSymbol.getType();
      assertEquals(1, type.getActualTypeArguments().size());
      assertFalse(type.getActualTypeArguments().get(0).isLowerBound());
      assertTrue(type.getActualTypeArguments().get(0).isUpperBound());
      assertEquals("T", type.getActualTypeArguments().get(0).getType().getName());
    }
    {
      JavaFieldSymbol superReferenceTypeSymbol = (JavaFieldSymbol) symbols.get(5);
      Symbol symbol = classScope.resolve("superReferenceType", JavaFieldSymbol.KIND).get();
      assertEquals(superReferenceTypeSymbol, symbol);
      JavaTypeSymbolReference type = superReferenceTypeSymbol.getType();
      assertEquals(1, type.getActualTypeArguments().size());
      assertTrue(type.getActualTypeArguments().get(0).isLowerBound());
      assertFalse(type.getActualTypeArguments().get(0).isUpperBound());
      assertEquals("String", type.getActualTypeArguments().get(0).getType().getName());
    }
    {
      JavaFieldSymbol extendsReferenceTypeSymbol = (JavaFieldSymbol) symbols.get(6);
      Symbol symbol = classScope.resolve("extendsReferenceType", JavaFieldSymbol.KIND).get();
      assertEquals(extendsReferenceTypeSymbol, symbol);
      JavaTypeSymbolReference type = extendsReferenceTypeSymbol.getType();
      assertEquals(1, type.getActualTypeArguments().size());
      assertFalse(type.getActualTypeArguments().get(0).isLowerBound());
      assertTrue(type.getActualTypeArguments().get(0).isUpperBound());
      assertEquals("String", type.getActualTypeArguments().get(0).getType().getName());
    }
    {
      JavaFieldSymbol superIntegerArrayTypeSymbol = (JavaFieldSymbol) symbols.get(7);
      Symbol symbol = classScope.resolve("superIntegerArrayType", JavaFieldSymbol.KIND).get();
      assertEquals(superIntegerArrayTypeSymbol, symbol);
      JavaTypeSymbolReference type = superIntegerArrayTypeSymbol
          .getType();
      assertEquals(1, type.getActualTypeArguments().size());
      ActualTypeArgument actualTypeArgument = type.getActualTypeArguments().get(0);
      assertTrue(actualTypeArgument.isLowerBound());
      assertFalse(actualTypeArgument.isUpperBound());
      assertEquals("Integer", actualTypeArgument.getType().getName());
      assertEquals(1, actualTypeArgument.getType().getDimension());
    }
    {
      JavaFieldSymbol extendsIntegerArrayTypeSymbol = (JavaFieldSymbol) symbols.get(8);
      Symbol symbol = classScope.resolve("extendsIntegerArrayType", JavaFieldSymbol.KIND).get();
      assertEquals(extendsIntegerArrayTypeSymbol, symbol);
      JavaTypeSymbolReference type = extendsIntegerArrayTypeSymbol
          .getType();
      assertEquals(1, type.getActualTypeArguments().size());
      ActualTypeArgument actualTypeArgument = type.getActualTypeArguments().get(0);
      assertFalse(actualTypeArgument.isLowerBound());
      assertTrue(actualTypeArgument.isUpperBound());
      assertEquals("Integer", actualTypeArgument.getType().getName());
      assertEquals(1, actualTypeArgument.getType().getDimension());
    }
    {
      JavaFieldSymbol superIntArrayTypeSymbol = (JavaFieldSymbol) symbols.get(9);
      Symbol symbol = classScope.resolve("superIntArrayType", JavaFieldSymbol.KIND).get();
      assertEquals(superIntArrayTypeSymbol, symbol);
      JavaTypeSymbolReference type = superIntArrayTypeSymbol.getType();
      assertEquals(1, type.getActualTypeArguments().size());
      ActualTypeArgument actualTypeArgument = type.getActualTypeArguments().get(0);
      assertTrue(actualTypeArgument.isLowerBound());
      assertFalse(actualTypeArgument.isUpperBound());
      assertEquals("int", actualTypeArgument.getType().getName());
      assertEquals(1, actualTypeArgument.getType().getDimension());
    }
    {
      JavaFieldSymbol extendsIntArrayTypeSymbol = (JavaFieldSymbol) symbols.get(10);
      Symbol symbol = classScope.resolve("extendsIntArrayType", JavaFieldSymbol.KIND).get();
      assertEquals(extendsIntArrayTypeSymbol, symbol);
      JavaTypeSymbolReference type = extendsIntArrayTypeSymbol.getType();
      assertEquals(1, type.getActualTypeArguments().size());
      ActualTypeArgument actualTypeArgument = type.getActualTypeArguments().get(0);
      assertFalse(actualTypeArgument.isLowerBound());
      assertTrue(actualTypeArgument.isUpperBound());
      assertEquals("int", actualTypeArgument.getType().getName());
      assertEquals(1, actualTypeArgument.getType().getDimension());
    }
    {
      JavaFieldSymbol multipleAndRecursiveTypeArgumentsSymbol = (JavaFieldSymbol) symbols
          .get(11);
      Symbol symbol = classScope.resolve("multipleAndRecursiveTypeArguments",
          JavaFieldSymbol.KIND).get();
      assertEquals(multipleAndRecursiveTypeArgumentsSymbol, symbol);
      JavaTypeSymbolReference type = multipleAndRecursiveTypeArgumentsSymbol
          .getType();
      assertEquals(2, type.getActualTypeArguments().size());
      
      // Second type argument is Collection<?>
      JavaTypeSymbolReference typeArgument0 = (JavaTypeSymbolReference) type
          .getActualTypeArguments().get(0).getType();
      assertEquals("Collection", typeArgument0.getName());
      assertEquals("?", typeArgument0.getActualTypeArguments().get(0).getType().getName());
      
      // Second type argument is Set<?>
      JavaTypeSymbolReference typeArgument1 = (JavaTypeSymbolReference) type
          .getActualTypeArguments().get(1).getType();
      assertEquals("Set", typeArgument1.getName());
      assertEquals("?", typeArgument1.getActualTypeArguments().get(0).getType().getName());
    }
  }
  
  @Test
  public void test_symbolTable_typeArgumentsAndParameters_TypeParameterTestClass()
      throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/typeArgumentsAndParameters/TypeParameterTestClass");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    
    Optional<Symbol> resolve = artifactScope.resolve("TypeParameterTestClass", JavaTypeSymbol.KIND);
    assertTrue(resolve.isPresent());
    Scope classScope = artifactScope.getSubScopes().get(0);
    // TODO
  }
  
  // package symbolTable.*
  @Test
  public void test_symbolTable_ScopesSymbolTableTestClass()
      throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/ScopesSymbolTableTestClass");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    // TODO scope asserts
    Scope someInterfaceScope = artifactScope.getSubScopes().get(0);
    Scope someReturnTypeScope = artifactScope.getSubScopes().get(1);
    Scope generalSymbolTableTestClassScope = artifactScope.getSubScopes().get(2);
    Scope classInitializerScope = generalSymbolTableTestClassScope.getSubScopes().get(0);
    Set<Entry<String, Collection<Symbol>>> entries = generalSymbolTableTestClassScope
        .getLocalSymbols().entrySet();
    assertTrue(entries.size() > 0);
    assertEquals(entries.iterator().next().getValue().iterator().next(),
        generalSymbolTableTestClassScope.resolve("field", JavaFieldSymbol.KIND).get());
    
    // field in the class initializer shadows the class attribute field
    entries = classInitializerScope.getLocalSymbols().entrySet();
    assertTrue(entries.size() > 0);
    assertEquals(entries.iterator().next().getValue().iterator().next(),
        classInitializerScope.resolve("field", JavaFieldSymbol.KIND).get());
  }
  
  @Test
  public void test_symbolTable_VariablesTestClass() throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/VariablesTestClass");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    assertEquals(2, artifactScope.getSubScopes().size());
    Scope classScope = artifactScope.getSubScopes().get(0);
    {
      JavaFieldSymbol publicValueSymbol = classScope.<JavaFieldSymbol> resolve(
          "publicValue", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(publicValueSymbol != null);
      assertTrue(publicValueSymbol.isPublic());
      assertFalse(publicValueSymbol.isProtected());
      assertFalse(publicValueSymbol.isPrivate());
    }
    {
      JavaFieldSymbol protectedValueSymbol = classScope.<JavaFieldSymbol> resolve(
          "protectedValue", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(protectedValueSymbol != null);
      assertFalse(protectedValueSymbol.isPublic());
      assertTrue(protectedValueSymbol.isProtected());
      assertFalse(protectedValueSymbol.isPrivate());
    }
    {
      JavaFieldSymbol privateValueSymbol = classScope.<JavaFieldSymbol> resolve(
          "privateValue", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(privateValueSymbol != null);
      assertFalse(privateValueSymbol.isPublic());
      assertFalse(privateValueSymbol.isProtected());
      assertTrue(privateValueSymbol.isPrivate());
    }
    {
      JavaFieldSymbol staticValueSymbol = classScope.<JavaFieldSymbol> resolve(
          "staticValue", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(staticValueSymbol != null);
      assertTrue(staticValueSymbol.isStatic());
    }
    {
      JavaFieldSymbol finalValueSymbol = classScope.<JavaFieldSymbol> resolve(
          "finalValue", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(finalValueSymbol != null);
      assertTrue(finalValueSymbol.isFinal());
    }
    // various primitive types and primitive array combination (leading/trailing
    // array brackets)
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "integerValue", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("int", symbol.getType().getName());
      assertEquals(0, symbol.getType().getDimension());
    }
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "integerArray", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("int", symbol.getType().getName());
      assertEquals(1, symbol.getType().getDimension());
    }
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "integerArrayWithTrailingBrackets", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("int", symbol.getType().getName());
      assertEquals(1, symbol.getType().getDimension());
    }
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "integerMatrixWithLeadingAndTrailingBrackets", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("int", symbol.getType().getName());
      assertEquals(2, symbol.getType().getDimension());
    }
    // various reference types and complex array combination (leading/trailing
    // array brackets)
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "someReference", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("VariablesTestClass", symbol.getType().getName());
      assertEquals(0, symbol.getType().getDimension());
    }
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "someReferenceArray", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("VariablesTestClass", symbol.getType().getName());
      assertEquals(1, symbol.getType().getDimension());
    }
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "someReferenceArrayWithTrailingBrackets", JavaFieldSymbol.KIND).orElse(null);
      assertTrue(symbol != null);
      assertEquals("VariablesTestClass", symbol.getType().getName());
      assertEquals(1, symbol.getType().getDimension());
    }
    {
      JavaFieldSymbol symbol = classScope.<JavaFieldSymbol> resolve(
          "someReferenceMatrixWithLeadingAndTrailingBrackets", JavaFieldSymbol.KIND).orElse(
              null);
      assertTrue(symbol != null);
      assertEquals("VariablesTestClass", symbol.getType().getName());
      assertEquals(2, symbol.getType().getDimension());
    }
    // someMethod
    {
      JavaMethodSymbol methodSymbol = classScope.<JavaMethodSymbol> resolve("someMethod",
          JavaMethodSymbol.KIND).orElse(null);
      assertTrue(methodSymbol != null);
      {
        assertEquals(1, methodSymbol.getParameters().size());
        JavaFieldSymbol javaParameterSymbol = methodSymbol.getParameters().get(0);
        assertTrue(javaParameterSymbol.isParameter());
        assertEquals(javaParameterSymbol,
            methodSymbol.getSpannedScope().resolve("someMethodParameter", JavaFieldSymbol.KIND)
                .orElse(null));
        Scope methodScope = classScope.getSubScopes().get(0);    
        JavaFieldSymbol localVariableSymbol = methodScope.<JavaFieldSymbol>resolve(
        "localVariable", JavaFieldSymbol.KIND).orElse(null);
        assertTrue(localVariableSymbol != null);
        assertEquals("int", localVariableSymbol.getType().getName());
        
        assertFalse(methodScope.<JavaFieldSymbol> resolve(
            "i", JavaMethodSymbol.KIND).isPresent());
      }
    }
  }
  
  @Test
  public void test_symbolTable_typevariableUpperbounds() throws RecognitionException, IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources",
        "generics/IComplexComponent");
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    
    JavaTypeSymbol interf = (JavaTypeSymbol) artifactScope.resolveLocally("IComplexComponent",
        JavaTypeSymbol.KIND).orElse(null);
    assertNotNull(interf);
    assertEquals(2, interf.getFormalTypeParameters().size());
    final JavaTypeSymbol kTypeSymbol = interf.getFormalTypeParameters().get(0);
    assertEquals("K", kTypeSymbol.getName());
    final JavaTypeSymbol vTypeSymbol = interf.getFormalTypeParameters().get(1);
    assertEquals("V", vTypeSymbol.getName());
    assertFalse(vTypeSymbol.getSuperClass().isPresent());
    assertEquals(1, vTypeSymbol.getSuperTypes().size());
    assertEquals("Number", vTypeSymbol.getSuperTypes().get(0).getName());
    
    assertSame(kTypeSymbol, interf.getSpannedScope().resolve("K", JTypeSymbol.KIND).get());
    assertSame(vTypeSymbol, interf.getSpannedScope().resolve("V", JTypeSymbol.KIND).get());
  }
  
  @Test
  public void testMethodParametersAndLocalVariablesAreDefinedInSameScope() throws IOException {
    ASTCompilationUnit astCompilationUnit = parse("src/test/resources/parsableAndCompilableModels",
        "symbolTable/MethodParametersAndLocalVariablesAreDefinedInSameScope");
    
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    JavaTypeSymbol typeSymbol = artifactScope.<JavaTypeSymbol> resolve(
        "MethodParametersAndLocalVariablesAreDefinedInSameScope", JavaTypeSymbol.KIND).orElse(null);
    assertNotNull(typeSymbol);
    
    JavaMethodSymbol method = typeSymbol.getMethod("testMethod").orElse(null);
    assertNotNull(method);
    
    assertEquals(3, method.getSpannedScope().resolveLocally(SymbolKind.KIND).size());
  }
}
