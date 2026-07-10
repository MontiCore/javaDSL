package de.monticore.codeAdaption.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.se_rwth.commons.logging.LogStub;
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
  void extractsSimpleNameThroughTypeParserWhenPossible() {
    assertEquals("Entry", JavaSourceNames.simpleName("java.util.Map.Entry"));
    assertEquals("List", JavaSourceNames.simpleName("java.util.List<java.lang.String>[]"));
    assertEquals("update", JavaSourceNames.simpleName("update()"));
  }

  @Test
  void normalizesMethodParametersThroughTheTypeParser() {
    assertEquals(
        "update(Map<String,List<Object[]>>,int[],long)",
        JavaMethodSignatures.normalize(
            " update(java.util.Map<java.lang.String, java.util.List<any[]>>, int[], long) "));
    assertEquals("update()", JavaMethodSignatures.normalize(" update( ) "));
  }

  @Test
  void leavesMalformedMethodSignaturesUnchanged() {
    assertEquals(
        "update(Map<String,List<Integer>,int)",
        JavaMethodSignatures.normalize("update(Map<String,List<Integer>,int)"));
    assertEquals(
        "update(String) trailing",
        JavaMethodSignatures.normalize("update(String) trailing"));
  }
}
