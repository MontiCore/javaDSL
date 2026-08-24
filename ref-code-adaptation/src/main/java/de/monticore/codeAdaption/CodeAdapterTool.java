package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.ADAPTED_NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_CARD_RESTRICTION;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.METHOD_OVERLOADING;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.SRC_TARGET_ASSOC_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE_MEMBER;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.INFIX_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;

import de.monticore.CD4CodeTool;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command-line entry point for reference-code adaptation. */
public class CodeAdapterTool extends CD4CodeTool {

  public static void main(String[] args) {
    int exitCode = new CodeAdapterTool().execute(args);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  @Override
  public void run(String[] args) {
    execute(args);
  }

  /** Executes one CLI invocation and returns a process-style exit code. */
  int execute(String[] args) {
    init();
    Options options = addAdditionalOptions(new Options());
    if (Arrays.stream(args).anyMatch(argument -> "-h".equals(argument) || "--help".equals(argument))) {
      printAdapterHelp(options);
      return 0;
    }
    try {
      CommandLine commandLine = new DefaultParser().parse(options, args);
      if (commandLine.hasOption("debug")) {
        Log.initDEBUG();
      } else {
        Log.init();
      }

      Set<String> mappings = values(commandLine, "mapping");
      if (mappings.isEmpty()) {
        throw new ParseException("At least one --mapping must be supplied");
      }

      Set<AdapterParam> adapterParameters = matchingParameters(commandLine);
      if (commandLine.hasOption("ignore-unmatched")) {
        adapterParameters.add(IGNORE_NON_MATCHED_TYPE);
        adapterParameters.add(IGNORE_NON_MATCHED_TYPE_MEMBER);
        adapterParameters.add(IGNORE_NON_MATCHED_VAR);
      }

      CodeAdapter adapter = new CodeAdapter(adapterParameters, defaultConformanceParameters());
      adapter.adapt(
          requiredFile(commandLine, "reference"),
          requiredFile(commandLine, "concrete"),
          mappings,
          requiredPath(commandLine, "reference-code"),
          requiredPath(commandLine, "concrete-code"),
          requiredPath(commandLine, "output"),
          commandLine.hasOption("concretize"),
          !commandLine.hasOption("no-common-parent"),
          !commandLine.hasOption("no-persist-concretized-cd"));
      return 0;
    } catch (ParseException | IllegalArgumentException | CodeAdaptationException exception) {
      Log.warn("Code adaptation failed: " + exception.getMessage());
      printAdapterHelp(options);
      return 2;
    } catch (RuntimeException exception) {
      Log.warn("Unexpected code-adaptation failure: " + exception.getMessage());
      return 1;
    }
  }

  @Override
  public ASTCDCompilationUnit parse(String path) {
    return JavaLoader.parseCD(path);
  }

  @Override
  public Options addAdditionalOptions(Options options) {
    options.addOption(requiredPathOption("r", "reference", "reference class diagram"));
    options.addOption(requiredPathOption("c", "concrete", "concrete class diagram"));
    options.addOption(requiredPathOption("rc", "reference-code", "reference Java source directory"));
    options.addOption(requiredPathOption("cc", "concrete-code", "concrete Java source directory"));
    options.addOption(requiredPathOption("o", "output", "generated Java output directory"));
    options.addOption(
        Option.builder("m")
            .longOpt("mapping")
            .hasArgs()
            .argName("name...")
            .desc("mapping stereotype name; at least one is required")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("matching")
            .hasArgs()
            .argName("name|infix|annotation")
            .desc("matching strategies; defaults to all strategies")
            .build());
    options.addOption(
        Option.builder().longOpt("concretize").desc("complete the concrete CD first").build());
    options.addOption(
        Option.builder()
            .longOpt("no-persist-concretized-cd")
            .desc("do not write the concretized CD to the output directory")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("no-common-parent")
            .desc("disable common-parent grouping for multiple incarnations")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("ignore-unmatched")
            .desc("retain unmatched reference-code elements instead of reporting them")
            .build());
    options.addOption(Option.builder("d").longOpt("debug").desc("enable debug logging").build());
    options.addOption(Option.builder("h").longOpt("help").desc("show this help").build());
    return options;
  }

  private static Option requiredPathOption(String shortName, String longName, String description) {
    return Option.builder(shortName)
        .longOpt(longName)
        .hasArg()
        .required()
        .argName("path")
        .desc(description)
        .build();
  }

  private static File requiredFile(CommandLine commandLine, String option) throws ParseException {
    return requiredPath(commandLine, option).toFile();
  }

  private static Path requiredPath(CommandLine commandLine, String option) throws ParseException {
    String value = commandLine.getOptionValue(option);
    if (value == null || value.isBlank()) {
      throw new ParseException("Missing required option --" + option);
    }
    return Path.of(value).toAbsolutePath().normalize();
  }

  private static Set<String> values(CommandLine commandLine, String option) {
    String[] supplied = commandLine.getOptionValues(option);
    return supplied == null
        ? new LinkedHashSet<>()
        : new LinkedHashSet<>(Arrays.asList(supplied));
  }

  private static Set<AdapterParam> matchingParameters(CommandLine commandLine)
      throws ParseException {
    Set<String> requested = values(commandLine, "matching");
    if (requested.isEmpty()) {
      return new LinkedHashSet<>(Set.of(NAME_MATCHING, INFIX_MATCHING, ANNOTATION_MATCHING));
    }
    Set<AdapterParam> result = new LinkedHashSet<>();
    for (String value : requested) {
      switch (value.toLowerCase(Locale.ROOT)) {
        case "name" -> result.add(NAME_MATCHING);
        case "infix" -> result.add(INFIX_MATCHING);
        case "annotation" -> result.add(ANNOTATION_MATCHING);
        default -> throw new ParseException("Unknown matching strategy: " + value);
      }
    }
    return result;
  }

  private static Set<CDConfParameter> defaultConformanceParameters() {
    return Set.of(
        STEREOTYPE_MAPPING,
        NAME_MAPPING,
        SRC_TARGET_ASSOC_MAPPING,
        INHERITANCE,
        ALLOW_CARD_RESTRICTION,
        METHOD_OVERLOADING,
        ADAPTED_NAME_MAPPING,
        ALLOW_ADDITIONAL_PARAMETERS);
  }

  private static void printAdapterHelp(Options options) {
    new HelpFormatter().printHelp("java -jar CodeAdapter.jar", options, true);
  }
}
