/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.cd.codegen.CDGenerator;
import de.monticore.cd.codegen.CdUtilsPrinter;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.TemplateController;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.io.paths.MCPath;
import de.monticore.java.java2cd.Java2CDConverter;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitorDelegator;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symboltable.ImportStatement;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaDSLTool extends de.monticore.java.javadsl.JavaDSLTool {

  /**
   * main method of the JavaDSL
   *
   * @param args array of the command line arguments
   */

  public static void main(String[] args) {
    JavaDSLTool tool = new JavaDSLTool();
    tool.run(args);
  }

  /**
   * executes the arguments stated in the command line like parsing a given model to an ast,
   * creating and printing out a corresponding symbol table or generating java files
   * based of additional configuration templates or handwritten code
   *
   * @param args array of the command line arguments
   */

  @Override
  public void run(String[] args) {
    Options options = initOptions();

    try {
      CommandLineParser cliParser = new DefaultParser();
      CommandLine cmd = cliParser.parse(options, args);

      if (!cmd.hasOption("i") || cmd.hasOption("h")) {
        printHelp(options);
        return;
      }

      if (cmd.hasOption("v")) {
        printVersion();
      }

      Log.init();
      JavaDSLMill.init();

      BasicSymbolsMill.initializePrimitives();
      BasicSymbolsMill.initializeString();

      Log.enableFailQuick(false);
      Collection<ASTCompilationUnit> asts =
          this.parse(".java", this.createModelPath(cmd).getEntries());
      Log.enableFailQuick(true);

      if (cmd.hasOption("path")) {
        String[] paths = splitPathEntries(cmd.getOptionValue("path"));
        JavaDSLMill.globalScope().setSymbolPath(new MCPath(paths));
      }

      Collection<IJavaDSLArtifactScope> scopes =
          asts.stream()
              .map(ast -> createSymbolTable(ast, cmd))
              .collect(Collectors.toList());

      if (cmd.hasOption("s")) {
        for (IJavaDSLArtifactScope scope : scopes) {
          this.storeSymTab(scope, cmd.getOptionValue("s"));
        }
      }

      if (cmd.hasOption("o")) {
        GlobalExtensionManagement glex = new GlobalExtensionManagement();
        glex.setGlobalValue("cdPrinter", new CdUtilsPrinter());
        GeneratorSetup setup = new GeneratorSetup();

        if (cmd.hasOption("tp")) {
          setup.setAdditionalTemplatePaths(
              Arrays.stream(cmd.getOptionValues("tp"))
                  .map(Paths::get)
                  .map(Path::toFile)
                  .collect(Collectors.toList()));
        }

        String outputPath = Paths.get(cmd.getOptionValue("o")).toString();

        setup.setGlex(glex);
        setup.setOutputDirectory(new File(outputPath));

        CDGenerator generator = new CDGenerator(setup);
        String configTemplate = cmd.getOptionValue("ct", "java2cd.Java2CD");
        TemplateController tc = setup.getNewTemplateController(configTemplate);
        TemplateHookPoint hpp = new TemplateHookPoint(configTemplate);

        Java2CDConverter converter = new Java2CDConverter();
        List<Object> configTemplateArgs = Arrays.asList(glex, converter, setup.getHandcodedPath(), generator);

        asts.forEach(ast -> hpp.processValue(tc, ast, configTemplateArgs));
      }

    } catch (ParseException e) {
      JavaDSLMill.globalScope().clear();
      Log.error(String.format("0xA7114 Could not process parameters: %s", e.getMessage()));
    }
  }

  /**
   * adds additional options to the cli tool
   *
   * @param options collection of all the possible options
   */

  public Options addAdditionalOptions(Options options) {

    options.addOption(
        Option.builder("o")
            .longOpt("output")
            .argName("dir")
            .hasArg()
            .desc("Sets the output path.")
            .build());

    options.addOption(
        Option.builder("ct")
            .longOpt("configtemplate")
            .hasArg()
            .argName("template")
            .desc("Sets a template for configuration.")
            .build());

    options.addOption(
        Option.builder("tp")
            .longOpt("template")
            .hasArg()
            .argName("path")
            .desc("Sets the path for additional templates.")
            .build());

    options.addOption(
        Option.builder("c2mc")
            .longOpt("class2mc")
            .desc("Enables to resolve java classes in the model path")
            .build());

    return options;
  }

  /**
   * gets the paths of all input models
   *
   * @param cmd cli arguments
   * @return path of all models
   */
  public MCPath createModelPath(CommandLine cmd) {
    if (cmd.hasOption("i")) {
      return new MCPath(splitPathEntries(cmd.getOptionValues("i")));
    } else {
      return new MCPath();
    }
  }

  /**
   * splits the compound paths of all input models
   *
   * @param composedPath combined path of all models
   * @return seperated paths of input models
   */
  public String[] splitPathEntries(String composedPath) {
    return composedPath.split(Pattern.quote(File.pathSeparator));
  }

  /**
   * splits the compound paths of all input models
   *
   * @param composedPaths combined paths of all models
   * @return seperated paths of input models
   */
  public final String[] splitPathEntries(String[] composedPaths) {
    return Arrays.stream(composedPaths)
        .map(this::splitPathEntries)
        .flatMap(Arrays::stream)
        .toArray(String[]::new);
  }

  /**
   * parses all input models with a given file ending
   *
   * @param file file ending of the files to parse
   * @param dirs input directories
   * @return collection of asts of all parsed models
   */
  public Collection<ASTCompilationUnit> parse(String file, Collection<Path> dirs) {
    return dirs.stream()
        .flatMap(directory -> this.parse(file, directory).stream())
        .collect(Collectors.toList());
  }

  /**
   * parses all input models with a given file ending
   *
   * @param fileExt file ending of the files to parse
   * @param directory input directory
   * @return collection of asts of all parsed models
   */
  public Collection<ASTCompilationUnit> parse(String fileExt, Path directory) {
    try (Stream<Path> paths = Files.walk(directory)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(file -> file.getFileName().toString().endsWith(fileExt))
          .map(Path::toString)
          .map(this::parse)
          .collect(Collectors.toSet());
    } catch (IOException e) {
      Log.error("0xA1063 Error while traversing the file structure `" + directory + "`.", e);
    }
    return Collections.emptySet();
  }


  /**
   * creates the symboltable for the given ast
   *
   * @param ast the input ast
   * @param cmd cli arguments
   * @return the symbol-table of the ast
   */

  public IJavaDSLArtifactScope createSymbolTable(ASTCompilationUnit ast, CommandLine cmd) {
    JavaDSLScopesGenitorDelegator genitor = JavaDSLMill.scopesGenitorDelegator();
    IJavaDSLArtifactScope scope = genitor.createFromAST(ast);
    if (cmd.hasOption("c2mc")) {
      scope.addImports(new ImportStatement("java.lang", true));
    }
    return scope;
  }

  /**
   * prints the symboltable of the given scope out to a file
   *
   * @param scope symboltable to store
   * @param path location of the file or directory containing the printed table
   */
  public void storeSymTab(IJavaDSLArtifactScope scope, String path) {
    if (Path.of(path).toFile().isFile()) {
      this.storeSymbols(scope, path);
    } else {
      this.storeSymbols(scope, Paths.get(
          path, Names.getPathFromPackage(scope.getFullName()) + ".javasym").toString());
    }
  }

}
