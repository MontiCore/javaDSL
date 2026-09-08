package de.monticore.codeAdaption;

import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks the canonical adapted form of every concretization helper during one adaptation.
 *
 * <p>The owning {@link MappingAdaptationRunner} is created once per adaptation, so entries are
 * shared by all incarnation passes and mapping names without leaking into later adaptations.
 */
final class HelperVariantRegistry {
  private final Map<String, RegisteredVariant> variants = new LinkedHashMap<>();

  /**
   * Registers one adapted helper variant.
   *
   * @return {@code true} when the caller must retain the unit, or {@code false} when an identical
   *     variant was registered earlier and the unit can be deduplicated
   * @throws CodeAdaptationException when the helper was previously produced with a structurally
   *     different AST
   */
  boolean register(
      String helperIdentity,
      ASTOrdinaryCompilationUnit variant,
      String mapping,
      String passDescription) {
    Objects.requireNonNull(helperIdentity);
    Objects.requireNonNull(variant);
    Origin incomingOrigin = new Origin(mapping, passDescription);
    RegisteredVariant registered = variants.get(helperIdentity);
    if (registered == null) {
      variants.put(helperIdentity, new RegisteredVariant(variant.deepClone(), incomingOrigin));
      return true;
    }
    if (registered.canonical().deepEquals(variant, true)) {
      return false;
    }
    throw new CodeAdaptationException(
        "Helper type '"
            + helperIdentity
            + "' has divergent outputs from "
            + registered.origin().describe()
            + " and "
            + incomingOrigin.describe());
  }

  private record RegisteredVariant(ASTOrdinaryCompilationUnit canonical, Origin origin) {}

  private record Origin(String mapping, String passDescription) {
    private Origin {
      Objects.requireNonNull(mapping);
      Objects.requireNonNull(passDescription);
    }

    String describe() {
      return "mapping '" + mapping + "', type selection " + passDescription;
    }
  }
}
