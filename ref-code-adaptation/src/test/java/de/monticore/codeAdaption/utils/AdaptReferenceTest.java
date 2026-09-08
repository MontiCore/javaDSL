package de.monticore.codeAdaption.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.java.javadsl.JavaDSLMill;
import de.se_rwth.commons.logging.LogStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdaptReferenceTest {

  @BeforeEach
  void setUp() {
    JavaDSLMill.init();
    JavaDSLMill.globalScope().clear();
    LogStub.init();
    LogStub.enableFailQuick(false);
  }

  @Test
  void parsesQualifiedOwnerWithoutConfusingParameterTypePackages() {
    AdaptReference reference =
        AdaptReference.parse("model.Order.update(java.time.Instant)").orElseThrow();

    assertEquals("model.Order", reference.owner().orElseThrow());
    assertEquals("update", reference.memberName());
    assertEquals("update(Instant)", reference.methodSignature().orElseThrow());
  }

  @Test
  void normalizesNestedGenericMethodParametersThroughJavaDsl() {
    AdaptReference reference =
        AdaptReference.parse("Outer.Inner.update(Map<String, List<Integer>>)").orElseThrow();

    assertEquals("Outer.Inner", reference.owner().orElseThrow());
    assertEquals("update(Map<String,List<Integer>>)", reference.memberReference());
  }

  @Test
  void distinguishesFieldsAndMethods() {
    AdaptReference field = AdaptReference.parse("model.Order.number").orElseThrow();
    AdaptReference method = AdaptReference.parse("update()").orElseThrow();

    assertFalse(field.isMethod());
    assertEquals("number", field.memberName());
    assertTrue(method.isMethod());
    assertTrue(method.owner().isEmpty());
  }

  @Test
  void rejectsMalformedReferences() {
    assertTrue(AdaptReference.parse("malformed(reference").isEmpty());
    assertTrue(AdaptReference.parse(".field").isEmpty());
    assertTrue(AdaptReference.parse("Owner.").isEmpty());
  }

  @Test
  void annotationNamesRequireAnExactMatch() {
    assertTrue(AdaptAnnotationNames.matches("Adapt"));
    assertTrue(AdaptAnnotationNames.matches("de.monticore.codeAdaption.utils.Adapt"));
    assertFalse(AdaptAnnotationNames.matches("NotAdapt"));
    assertFalse(AdaptAnnotationNames.matches("example.NotAdapt"));
  }
}
