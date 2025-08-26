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
import de.monticore.java.javadsl._ast.ASTModularCompilationUnit;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.utils.JavaDSLSymbolTableUtil;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaDSLTool extends de.monticore.java.javadsl.JavaDSLTool {
  
  
  protected static final String SYMBOLS_OUT_DIRECTORY = "target" + File.separator + "symbols";
  
  
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
      
      boolean useClass2MC = cmd.hasOption("c2mc");
      JavaDSLSymbolTableUtil.prepareMill(useClass2MC);
      
      MCPath symbolPath = new MCPath();
      if (cmd.hasOption("path")) {
        String[] paths = cmd.getOptionValues("path");
        Arrays.stream(paths).forEach(p -> symbolPath.addEntry(Paths.get(p)));
      }
      JavaDSLMill.globalScope().setSymbolPath(symbolPath);

      Log.enableFailQuick(false);
      Collection<ASTCompilationUnit> parsed = this.parse(".java", this.createModelPath(cmd).getEntries());
      List<ASTCompilationUnit> asts = parsed.stream().filter(Objects::nonNull).collect(Collectors.toList());
      if (asts.size() < parsed.size()) {
        Set<String> errorFiles =
            Log.getFindings().stream().map(Finding::getSourcePosition).filter(Optional::isPresent)
                .map(x -> x.get().getFileName()).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet());
        Log.warn(String.format("There are %d files that could not be parsed!", errorFiles.size()));
      }
      Log.clearFindings();
      Log.enableFailQuick(true);
      
      System.out.printf("Successfully parsed %d files%n", asts.size());
      
      if (cmd.hasOption("pp")) {
        String[] ppTargets = cmd.getOptionValues("pp");
        if (ppTargets == null || ppTargets.length == 0) {
          asts.forEach(ast -> prettyPrintInFolder(ast, SYMBOLS_OUT_DIRECTORY));
        }
        else if (ppTargets.length == 1 && isLikelyFolderPath(cmd.getOptionValue("pp"))) {
          asts.forEach(
              compUnit -> prettyPrintInFolder(compUnit, cmd.getOptionValue("pp")));
        }
        else if (ppTargets.length == asts.size()
            && ppTargets.length == cmd.getOptionValues("i").length) {
          for (int i = 0; i < asts.size(); i++) {
            prettyPrintInFolder(
                asts.get(i),
                ppTargets[i]
            );
          }
        }
        else {
          Log.error(String.format("Received '%s' output files for the prettyprint option. "
                  + "Expected that '%s' many output files are specified. "
                  + "If output files for the prettyprint option are specified, then the number "
                  + "of specified output files must be equal to the number of specified input files, "
                  + "or one outputfolder should be specified.",
              cmd.getOptionValues("pp").length, asts.size()));
        }
      }
      
      if (cmd.hasOption("s") || cmd.hasOption("o")) {
        // Build symbol table and run symbol table completer
        JavaDSLSymbolTableUtil.buildSymbolTable(asts);
      }
      
      if (cmd.hasOption("s")) {
        if (cmd.getOptionValues("s") == null || cmd.getOptionValues("s").length == 0) {
          for (ASTCompilationUnit compilationUnit : asts) {
            storeSymbolsInFolder(compilationUnit, SYMBOLS_OUT_DIRECTORY);
          }
        }
        else if (cmd.getOptionValues("s").length == 1 &&
            isLikelyFolderPath(cmd.getOptionValue("s"))) {
          asts.forEach(
              compUnit -> this.storeSymbolsInFolder(compUnit, cmd.getOptionValue("s")));
        }
        else if (cmd.getOptionValues("s").length == asts.size()
            && cmd.getOptionValues("s").length == cmd.getOptionValues("i").length) {
          for (int i = 0; i < asts.size(); i++) {
            storeSymbols(
                (IJavaDSLArtifactScope) asts.get(i).getEnclosingScope(),
                cmd.getOptionValues("s")[i]
            );
          }
        }
        else {
          Log.error(String.format("Received '%s' output files for the storesymbols option. "
                  + "Expected that '%s' many output files are specified. "
                  + "If output files for the storesymbols option are specified, then the number "
                  + "of specified output files must be equal to the number of specified input files, "
                  + "or one outputfolder should be specified.",
              cmd.getOptionValues("s").length, asts.size()));
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
    String[] inputPathEntries = cmd.getOptionValues("i");
    return new MCPath(splitPathEntries(inputPathEntries));
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
   * Stores the symbols for ast in the specified folder.
   *
   * @param compilationUnit The ast of the Java CompilationUnit
   * @param folderPath      The folder to store the symbols in
   */
  protected void storeSymbolsInFolder(ASTCompilationUnit compilationUnit, String folderPath) {
    String relativeFilePath = getRelativeFilePath(compilationUnit).concat(".javasym");
    Path filePath = Paths.get(folderPath, relativeFilePath);
    storeSymbols((IJavaDSLArtifactScope) compilationUnit.getEnclosingScope(), filePath.toString());
  }
  
  /**
   * Stores the pretty printed result for ast in the specified folder.
   *
   * @param compilationUnit The ast of the Java CompilationUnit
   * @param folderPath      The folder to store the pret
   */
  protected void prettyPrintInFolder(ASTCompilationUnit compilationUnit, String folderPath) {
    String relativeFilePath = getRelativeFilePath(compilationUnit).concat(".java");
    Path filePath = Paths.get(folderPath, relativeFilePath);
    prettyPrint(compilationUnit, filePath.toString());
  }
  
  /**
   * finds the file (without extension) for ast,
   * given its package and name.
   * E.g.: model with qualified name a.b.c
   * "a/b/c"
   *
   * @param compilationUnit The ast of the model
   * @return the relative file path as String based on the models qualified name
   */
  protected String getRelativeFilePath(ASTCompilationUnit compilationUnit) {
    Optional<ASTMCQualifiedName> qualifiedName = Optional.empty();
    String artifactName = "";
    if (JavaDSLMill.typeDispatcher().isJavaDSLASTOrdinaryCompilationUnit(compilationUnit)) {
      ASTOrdinaryCompilationUnit ast = JavaDSLMill.typeDispatcher().asJavaDSLASTOrdinaryCompilationUnit(compilationUnit);
      if (ast.isPresentPackageDeclaration()) {
        qualifiedName = Optional.of(ast.getPackageDeclaration().getMCQualifiedName());
      }
      if (ast.getTypeDeclarationList().size() == 1) {
        artifactName = ast.getTypeDeclarationList().get(0).getName();
      } else {
        Optional<ASTTypeDeclaration> publicTypeDeclaration = ast.getTypeDeclarationList().stream().filter(x -> x.getSymbol().isIsPublic()).findFirst();
        if (publicTypeDeclaration.isPresent()) {
          artifactName = publicTypeDeclaration.get().getName();
        }
      }
    } else if (JavaDSLMill.typeDispatcher().isJavaDSLASTModularCompilationUnit(compilationUnit)) {
      ASTModularCompilationUnit ast = JavaDSLMill.typeDispatcher().asJavaDSLASTModularCompilationUnit(compilationUnit);
      qualifiedName = Optional.of(ast.getModuleDeclaration().getMCQualifiedName());
    }
    
    if (qualifiedName.isPresent()) {
      String packagePath = qualifiedName.get().getQName().replace('.', File.separatorChar);
      return Paths.get(packagePath, artifactName).toString();
    } else {
      if (artifactName.isBlank()) {
        Log.error("0x7A005: Could not determine symbol table export path. "
            + "Make sure that the file contains exactly one public class!");
      }
      return Paths.get(artifactName).toString();
    }
  }
  
  /**
   * heuristic to test if the path seems to be a folder path
   *
   * @param pathStr the path to check
   * @return whether we assume it is a path to a folder
   */
  protected boolean isLikelyFolderPath(String pathStr) {
    // if it already exists, check:
    Path path = Paths.get(pathStr);
    File file = path.toFile();
    if (file.exists()) {
      return file.isDirectory();
    }
    // if it does not exist yet,
    // check if the last part ends with an extension
    // note that "a/b/.c" is expected to be a folder,
    // "a/b/c.d" is not expected to be a folder,
    // so we skip the first character
    return !path.getFileName().toString().substring(1).contains(".");
  }
  
}
