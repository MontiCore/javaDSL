package de.monticore.codeAdaption.context;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContextBuilder;
import de.monticore.codeAdaption.handler.multiIncarnation.ManualIncarnationContextBuilder;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.se_rwth.commons.logging.Log;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Builds one incarnation context per requested mapping stereotype.
 *
 * <p>For example, mappings {@code [shop, billing]} produce a result map whose keys are {@code
 * shop} and {@code billing}. Each value contains only the incarnations derived for that mapping;
 * mappings are never combined into one context.
 */
public final class AdaptationContextFactory {

  private final Set<CDConfParameter> confParams;
  private final MappingConformanceService conformanceService;

  public AdaptationContextFactory(
      Set<CDConfParameter> confParams, MappingConformanceService conformanceService) {
    this.confParams = Set.copyOf(confParams);
    this.conformanceService = conformanceService;
  }

  /**
   * Builds mapping-name to adaptation-result entries in deterministic mapping-name order.
   *
   * <p>If {@code useConcretizationMappings} is {@code false}, every context is derived directly
   * from stereotypes and deterministic name matches; the result has no checker. If it is {@code
   * true}, each mapping is first checked independently. A successful checker result is retained
   * only when it also supplies an incarnation mapping. Otherwise that one mapping falls back to
   * manual derivation while other mappings may still use their checker results.
   *
   * @param referenceIndex indexed reference CD whose elements become mapping keys
   * @param concreteIndex indexed concrete CD whose elements become incarnations
   * @param mappings stereotype names to evaluate, for example {@code shop} and {@code billing}
   * @param useConcretizationMappings whether to prefer usable conformance-checker incarnation
   *     mappings over manual stereotype derivation
   * @return mapping stereotype name to its isolated context, optional checker, and checker-mapping
   *     availability flag
   */
  public Map<String, AdaptationContextResult> buildResults(
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex,
      Set<String> mappings,
      boolean useConcretizationMappings) {

    ASTCDCompilationUnit refCD = referenceIndex.cd();
    ASTCDCompilationUnit conCD = concreteIndex.cd();

    Map<String, AdaptationContextResult> results = new LinkedHashMap<>();

    if (!useConcretizationMappings) {
      ManualIncarnationContextBuilder builder =
          new ManualIncarnationContextBuilder(referenceIndex, concreteIndex, confParams);
      mappings.stream()
          .sorted()
          .forEach(
              mapping ->
                  results.put(
                      mapping,
                      new AdaptationContextResult(
                          builder.buildContextForMapping(mapping), null, false)));
      return results;
    }

    for (String mapping : mappings.stream().sorted().toList()) {
      CDConformanceChecker checker = conformanceService.newChecker();
      boolean mappingValid = conformanceService.checkOrFalse(checker, conCD, refCD, mapping);

      if (!mappingValid) {
        Log.warn(
            "Mapping '"
                + mapping
                + "' failed conformance check - will use conflict-checked stereotype-based fallback for this mapping");
      } else {
        Log.info(
            "Mapping '"
                + mapping
                + "' passed conformance check with incarnation mapping: "
                + (checker.getIncarnationMapping() != null
                    ? checker.getIncarnationMapping().getClass().getSimpleName()
                    : "null"),
            "CodeAdapter");
      }

      boolean conformanceMappingAvailable =
          mappingValid && checker.getIncarnationMapping() != null;
      IncarnationContext context;
      if (!conformanceMappingAvailable) {
        context =
            new ManualIncarnationContextBuilder(referenceIndex, concreteIndex, confParams)
                .buildContextForMapping(mapping);
      } else {
        IncarnationContextBuilder builder =
            new IncarnationContextBuilder(checker, referenceIndex, concreteIndex);
        context = builder.buildContextForMapping(mapping, false);
      }
      results.put(
          mapping,
          new AdaptationContextResult(
              context,
              conformanceMappingAvailable ? checker : null,
              conformanceMappingAvailable));
    }

    return results;
  }

  /**
   * Result for one mapping stereotype.
   *
   * @param context incarnations used by adaptation; always present, including after manual fallback
   * @param checker checker retained only when it produced the context's usable incarnation mapping
   * @param conformanceValid whether a usable conformance-checker incarnation mapping produced this
   *     context; {@code false} also covers a successful check that exposed no incarnation mapping
   */
  public record AdaptationContextResult(
      IncarnationContext context, CDConformanceChecker checker, boolean conformanceValid) {}
}
