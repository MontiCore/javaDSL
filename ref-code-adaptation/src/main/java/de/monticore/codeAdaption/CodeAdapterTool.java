package de.monticore.codeAdaption;

import de.monticore.CD4CodeTool;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.cli.*;

public class CodeAdapterTool extends CD4CodeTool {

  /**
   * Processes user input from command line and delegates to the corresponding tools.
   *
   * @param args The input parameters for configuring the OCL tool.
   */
  @Override
  public void run(String[] args) {
    init();
    Options options = initOptions();
    /*
                java - jar AdapterTool . jar -i " University . cd " \
                2 -- reference " UserRole . cd " -- rc " code "
    */

    try {
      // create CLI parser and parse input options from command line
      CommandLineParser cliparser = new DefaultParser();
      CommandLine cmd = cliparser.parse(options, args);

      if (cmd.hasOption("i")) {
        // -option developer logging
        if (cmd.hasOption("d")) {
          Log.initDEBUG();
        } else {
          Log.init();
        }
      }

    } catch (ParseException e) {
      Log.error("0xA7101 Could not process CLI parameters: " + e.getMessage());
    }
  }

  /*=================================================================*/
  /* Part 2: Executing arguments
  /*=================================================================*/

  /**
   * Parses the contents of a given file as OCL.
   *
   * @param path The path to the OCL-file as String
   * @return parsed AST
   */
  @Override
  public ASTCDCompilationUnit parse(String path) {
    return null;
  }

  /**
   * Initializes the additional options for the OCL tool.
   *
   * @return The CLI options with arguments.
   */
  @Override
  public Options addAdditionalOptions(Options options) {

    // accept VariableSymbols
    Option varSymbols =
        Option.builder("i")
            .longOpt("concrete")
            .optionalArg(false)
            .argName("concrete class diagram")
            .hasArgs()
            .desc("introduce the concrete class diagram")
            .build();
    options.addOption(varSymbols);

    // accept FunctionSymbols
    Option funcSymbols =
        Option.builder("rc")
            .longOpt("reference-code")
            .optionalArg(false)
            .argName("reference-code")
            .hasArgs()
            .desc("Introduce the reference code.")
            .build();
    options.addOption(funcSymbols);

    // accept FunctionSymbols
    Option ignoreSymbols =
        Option.builder("r")
            .longOpt("reference")
            .optionalArg(false)
            .argName("reference class diagram")
            .hasArgs()
            .desc("introduce the reference class diagram")
            .build();
    options.addOption(ignoreSymbols);

    return options;
  }
}
