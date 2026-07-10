package de.monticore.codeAdaption.context;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContextBuilder;
import de.monticore.codeAdaption.handler.multiIncarnation.ManualIncarnationContextBuilder;
import de.se_rwth.commons.logging.Log;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Builds mapping-specific incarnation contexts for manual and concretization modes. */
public final class AdaptationContextFactory {

  private final Set<CDConfParameter> confParams;
  private final MappingConformanceService conformanceService;

  public AdaptationContextFactory(
      Set<CDConfParameter> confParams, MappingConformanceService conformanceService) {
    this.confParams = Set.copyOf(confParams);
    this.conformanceService = conformanceService;
  }

  /** Builds the context and retains the checker that produced it for downstream handlers. */
  public Map<String, AdaptationContextResult> buildResults(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Set<String> mappings,
      boolean useConcretizationMappings) {

    Map<String, AdaptationContextResult> results = new LinkedHashMap<>();

    if (!useConcretizationMappings) {
      ManualIncarnationContextBuilder builder =
          new ManualIncarnationContextBuilder(refCD, conCD, confParams);
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
                + "' failed conformance check - will use stereotype-based fallback for this mapping");
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

      IncarnationContext context;
      if (!mappingValid || checker.getIncarnationMapping() == null) {
        context =
            new ManualIncarnationContextBuilder(refCD, conCD, confParams)
                .buildContextForMapping(mapping);
      } else {
        IncarnationContextBuilder builder = new IncarnationContextBuilder(checker, refCD, conCD);
        context = builder.buildContextForMapping(mapping, false);
      }
      results.put(mapping, new AdaptationContextResult(context, checker, mappingValid));
    }

    return results;
  }

  public record AdaptationContextResult(
      IncarnationContext context, CDConformanceChecker checker, boolean conformanceValid) {}
}
