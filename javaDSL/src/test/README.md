<!-- (c) https://github.com/MontiCore/monticore -->
# Tests of the symbol table and the type check

## Structure

| Test class | Tests |
|---|---|
| `LanguageFeaturesTest` | parsing and symbol table construction of all language features up to Java 21, see below |
| `_symboltable.TypeDeclarationSymbolTest` | kinds, modifiers, super types, type parameters, nested/local/anonymous types, enums, records, annotations, artifact scope |
| `_symboltable.VariableSymbolTest` | types and modifiers of fields, parameters, local variables, `var`, loop/resource/catch/lambda variables |
| `_symboltable.MethodSymbolTest` | signatures, modifiers, exceptions, annotations, generic methods, overloading, constructors |
| `_symboltable.ScopeTest` | scope tree, visibility and shadowing of names |
| `_symboltable.ResolutionTest` | types across compilation units, packages, imports, JDK types via class2mc |
| `_symboltable.JavaDSLArtifactScopeTest` | `calculateQualifiedNames` |
| `types3.ExpressionTypeTest` | types of valid expressions in the context `ExpressionProbe.CONTEXT` |
| `types3.InvalidExpressionTest` | ill-typed expressions must produce a type error and the obscure type |
| `types3.AccessTest` | access modifiers (JLS 6.6) |
| `types3.StatementTypeCheckTest` | statements and member declarations with the `TypeCheckCoCos` |
| `types3.TypeCheckModelsTest` | complete projects in `resources/typecheck` |

Helpers in `de.monticore.java.util`:

* `SymbolTables` builds the complete symbol table (all phases) of models given as source code or
  files, each time with a fresh global scope.
* `Symbols` looks up symbols by their full name, e.g., `field("a.C.x")`, `method("a.C.m")`, and
  local variables within the scope of a method: `localVariable(method("a.C.m"), "x")`.
* `JavaDSLAssertions.assertType("java.util.List<java.lang.String>", type)` compares types by
  their full print.
* `Javac` compiles models with javac for Java 21 (`--release 21`), independently of the JDK that
  executes the tests.

## Conventions

* **Every test model is checked with javac.** Models that are expected to be valid must compile,
  invalid ones must not (`SymbolTables.fromSource` does this automatically; use
  `fromUncheckedSources` for models that are invalid on purpose). Hence, a failing test is caused
  by the JavaDSL and not by a wrong expectation. When adding a test, expect the behavior of
  javac (and the JLS), not the current behavior of the JavaDSL.
* Each test should fail for one reason only. E.g., members of the classes in
  `ExpressionProbe.CONTEXT` are public, so that the expression tests do not depend on the
  (broken) access of package private members, which is tested by `AccessTest`.
* Small models are written inline as text blocks next to the assertions. Resource models are only
  used for complete projects (`resources/typecheck/valid/<project>`) and invalid models with error
  markers (`resources/typecheck/invalid`): a line with `// error: 0xA0909` expects an error with
  this code at this line, `// error` expects any error; errors on unmarked lines fail the test.

## Java 21

The JavaDSL aims at the language of Java 21. `LanguageFeaturesTest` lists the language features
named after the Java version in which they became final, e.g., *Java 16: records*, and checks for
each one that javac accepts the model, that it is parsable, and that its symbol table is built
without findings. The semantics of the features are tested in the respective test classes (e.g.,
pattern variables in `VariableSymbolTest`, switch expressions in `ExpressionTypeTest`, pattern
scoping in `StatementTypeCheckTest`), and `resources/typecheck/valid/calculator` is a complete
Java 21 project. Preview features of Java 21 (e.g., unnamed patterns `_`, string templates) are
not targeted.

State of 2026-10:

| Feature | Parser | Symbol table |
|---|---|---|
| Java 5 to 7 (generics, enums, annotations, static imports, try-with-resources, multi-catch, switch on strings, ...) | yes | yes |
| Java 8: lambdas, method references, default methods, type annotations, intersection casts | yes | yes |
| Java 8: receiver parameters (`void m(C this)`) | **no** | |
| Java 9: private interface methods, diamond with anonymous classes, modules | yes | yes |
| Java 9: try-with-resources on variables (`try (reader)`) | yes | **no** (`IllegalStateException`) |
| Java 10/11: `var` (also in lambda parameters) | yes | yes |
| Java 14: switch expressions, `yield`, arrow labels, several constants per label | **no** | |
| Java 15: text blocks | yes | yes |
| Java 16: records | yes | yes |
| Java 16: pattern matching for `instanceof`, local records/enums/interfaces, static members of inner classes | yes | yes |
| Java 17: sealed classes and interfaces (`sealed`, `non-sealed`, `permits`) | **no** | |
| Java 21: pattern matching for switch (`case null`, guards with `when`), record patterns | **no** | |
| Contextual keywords as identifiers | yes, except `record` | yes |

## Known issues

Many tests currently fail. The following causes are known (state of 2026-10). The listed tests
are examples, as most issues affect several tests.

### Symbol table

| Issue | Example tests |
|---|---|
| Trailing array dimensions are ignored for local variables, parameters, and methods (`int e[]`, `int m()[]`) | `severalDeclaratorsInOneDeclaration`, `parameters`, `signatures` |
| A variable arity parameter `String... rest` has the type `String` instead of `String[]` | `variableArityParameterIsArray`, `ExpressionTypeTest` `params` |
| `for (var x : list)` gets the type of the list instead of its element type | `varLoopVariables` |
| `JavaDSLSymbolTableUtil.buildSymbolTable(ASTCompilationUnit)` skips phase 3, i.e., `var` stays untyped | `buildSymbolTableOfSingleModelInfersVar` |
| Lambda parameters without explicit type (`x -> ...`, `(var x) -> ...`) have no type | `lambdaParameters`, `TypeCheckModelsTest` (bank, geometry) |
| Catch parameters are not part of the symbol table | `resourcesAndCatchParameters` |
| Annotations of methods (`@Override`) are not stored | `MethodSymbolTest.annotations` |
| Within a generic class `C<T>`, the name `C` denotes `C<T>` instead of the raw type | `VariableSymbolTest` `C x` |
| A type parameter shadows a member type of the same name, but javac resolves the member type (the old test expected the wrong behavior) | `typeParameterAndMemberTypeWithSameName` |
| A single type import does not shadow a type of the same package | `singleTypeImportShadowsTypeOfSamePackage` |
| Member types inherited from a super class are not resolvable | `inheritedNestedTypeIsVisibleInSubClass` |
| Package private (and protected) members are not accessible from other classes of the same package (`TypeContextCalculator`: "missing: package_local"); this also affects nested types like `D.Nested` and the (default) constructors of package private classes like `new Base()` | `AccessTest` (`SamePackage`) |
| Private and package private members of a super class are accessible in a sub class of another package | `AccessTest` (`SubClass`) |
| class2mc: nested JDK types have wrong full names (`java.util.Entry` instead of `java.util.Map.Entry`), `Map.Entry<K, V>` is not resolvable in declarations (`0xA0323`), types outside of `java.base` (e.g., `java.sql`) are not found | `VariableSymbolTest` `Map.Entry`, `java.sql.Connection`, `ExpressionTypeTest` `msi.entrySet()` |
| Static imports are not supported (`TODO` in `JavaDSLScopesGenitor`) | `ExpressionTypeTest.staticImports` |
| Parser: the Java 14, 17, and 21 features listed above, receiver parameters, and `record` as identifier | `LanguageFeaturesTest`, `switchExpressionsAndPatterns`, `validArrowAndPatternSwitch`, `calculator` |

### Type check

| Issue | Example tests |
|---|---|
| Not supported: qualified `Outer.this` and `Outer.super`, explicit type arguments (`Context.<T>m()`), `outer.new Inner()`, and method references (no type visitor); MontiCore's `JavaClassExpressionsTypeVisitor` does not support `this`/`super` at all, hence `JavaDSLJavaClassExpressionsTypeVisitor` implements the unqualified forms | `ExpressionTypeTest` `Context.this`, `this.new Inner()`, `targetTyped` |
| Function types of lambdas are not compatible with functional interfaces (`() -> void` vs. `Runnable`) | `ExpressionTypeTest.targetTyped`, `StatementTypeCheckTest.validMembers` |
| String literals have the regex type `R"..."` instead of `String` (also the type of `var x = "..."`) | `ExpressionTypeTest` `"text"`, `typeOfVarWithStringLiteral` |
| Conditional expressions have union types (`int \| long`) instead of the Java types | `ExpressionTypeTest.conditional` |
| Type inference does not box primitive type arguments (`id(1)` is `int`) | `ExpressionTypeTest` `id(1)`, `Box.of(Box.of(1))` |
| Overload resolution does not follow the phases of JLS 15.12.2 (`overloaded(boxedInt)`), `List.of(1, 2)` is ambiguous, arrays are not accepted for variable arity parameters (`varargs(sa)`, `Arrays.asList(sa)`) | `ExpressionTypeTest.overloadingAndVarargs` |
| Inherited methods are found twice when called unqualified in a sub class, and overridden methods are not filtered (`area()`) | `ExpressionTypeTest` `baseMethod()`, `area()` |
| `0x7A017` (multiple super classes) in `JavaDSLOOWithinTypeBasicSymbolsResolver` for class2mc types such as `StringBuilder` | `ExpressionTypeTest` `new StringBuilder().append(i)` |
| String concatenation is rejected for operands that are not String or numeric (`s + o`, `s + null`, `s + bool`) | `ExpressionTypeTest.stringConcatenation` |
| Arrays: `.length` and array creation with initializer (`new int[] {1}`) are not supported | `ExpressionTypeTest.arrays` |
| Casts from `Object` to arrays are rejected; `(Integer) l`, `(Long) i` and incompatible `instanceof` are accepted | `ExpressionTypeTest.castsAndInstanceOf`, `InvalidExpressionTest.castsAndInstanceOf` |
| `boxedInt++` has the type `int` instead of `Integer` | `ExpressionTypeTest.arithmetic` |
| Assignments: constant narrowing (`byte x = 1`) is rejected; `boxedLong = 1`, assignments to non-variables (`1 = i`) and to final variables are accepted | `StatementTypeCheckTest`, `InvalidExpressionTest.assignments*` |
| A field access to a method name (`s.length`, or `point.x` for the private record field `x`) yields a function type; a `long` array index is accepted | `InvalidExpressionTest` |
| `var x = s.length();` causes `0xFD111` (typeOf without target type in phase 3, then with target type in the CoCo) | `StatementTypeCheckTest` |

### CoCos of the MontiCore grammar library

| Issue | Example tests |
|---|---|
| `ForEachIsValid` throws a `NullPointerException`, as the JavaDSL overrides `EnhancedForControl` | `StatementTypeCheckTest.validForEach`, `ForEach.java` |
| `SwitchStatementValid` rejects switch on `String`; enum constants in `case` labels are not resolved | `StatementTypeCheckTest.validSwitch` |
| `ReturnTypeAssignmentIsValid` reports methods that end with `throw` or an infinite loop, and it attributes the return statements of lambdas within the method to the method (a JavaDSL variant could use `ReturnStatementCollector`) | `StatementTypeCheckTest.validMembers` |
| Several errors are logged without source position (`0xA0921`, `0xA0906`, `0xA0917`, `0xA0925`, `0xFD44E`), so the error markers of the invalid models can not be matched | `TypeCheckModelsTest` (`Initializers.java`, `Expressions.java`) |
