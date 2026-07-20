package de.monticore.codeAdaption.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.se_rwth.commons.logging.LogStub;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JavaSourceNamesTest {

  @BeforeEach
  void setUp() {
    LogStub.init();
    LogStub.enableFailQuick(false);
  }

  @Test
  void normalizesQualifiedGenericArrayAndAnyTypes() {
    assertEquals(
        "Map<String,List<Object[]>>",
        JavaSourceNames.normalizeType("java.util.Map<java.lang.String, java.util.List<any[]>>"));
    assertEquals("Object", JavaSourceNames.normalizeType("any"));
    assertEquals("String[]", JavaSourceNames.normalizeType("java.lang.String []"));
    assertEquals("int[]", JavaSourceNames.normalizeType("int[]"));
    assertEquals("void", JavaSourceNames.normalizeType("void"));
    assertEquals("Optional<Long>", JavaSourceNames.normalizeType("Optional<long>"));
    assertEquals("List<Integer>", JavaSourceNames.normalizeType("List<int>"));
    assertEquals(
        "List<?extendsNumber>",
        JavaSourceNames.normalizeType("java.util.List<? extends java.lang.Number>"));
  }

  @Test
  void replacesSimpleTypeNamesInsideNestedGenericTypes() {
    String replaced =
        JavaSourceNames.replaceSimpleTypeNames(
            "Map<String, List<Person[]>>",
            simple -> "Person".equals(simple) ? Optional.of("Named") : Optional.empty());

    assertEquals("Map<String, List<Named[]>>", replaced);
  }

  @Test
  void preservesQualifiedNamesWhenReplacingParsedTypeLeaves() {
    String replaced =
        JavaSourceNames.replaceSimpleTypeNames(
            "java.util.Map<java.lang.String, java.util.List<Person[]>>",
            simple -> "Person".equals(simple) ? Optional.of("Named") : Optional.empty());

    assertEquals("java.util.Map<java.lang.String, java.util.List<Named[]>>", replaced);
  }

  @Test
  void extractsQualifiedIdentityFromNestedGenericAndWildcardTypes() {
    assertEquals(
        List.of(
            new JavaSourceNames.TypeReferenceName("java.util.Map", "Map", true),
            new JavaSourceNames.TypeReferenceName("a.User", "User", true),
            new JavaSourceNames.TypeReferenceName("java.util.List", "List", true),
            new JavaSourceNames.TypeReferenceName("b.User", "User", true)),
        JavaSourceNames.typeReferences(
            "java.util.Map<a.User[], java.util.List<? extends b.User>>"));
  }

  @Test
  void extractsSimpleNameThroughTypeParserWhenPossible() {
    assertEquals("Entry", JavaSourceNames.simpleName("java.util.Map.Entry"));
    assertEquals("List", JavaSourceNames.simpleName("java.util.List<java.lang.String>[]"));
    assertEquals("update", JavaSourceNames.simpleName("update()"));
  }

  @Test
  void normalizesMethodParametersThroughTheTypeParser() {
    assertEquals(
        Optional.of("update(Map<String,List<Object[]>>,int[],long)"),
        JavaMethodSignatures.parseNormalized(
            " update(java.util.Map<java.lang.String, java.util.List<any[]>>, int[], long) "));
    assertEquals(Optional.of("update()"), JavaMethodSignatures.parseNormalized(" update( ) "));
  }

  @Test
  void rejectsMalformedMethodSignatures() {
    assertEquals(
        Optional.empty(),
        JavaMethodSignatures.parseNormalized("update(Map<String,List<Integer>,int)"));
    assertEquals(
        Optional.empty(), JavaMethodSignatures.parseNormalized("update(String) trailing"));
  }
}
