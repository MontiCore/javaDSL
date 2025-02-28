package de.monticore.codeAdaption;

import static de.monticore.codeAdaption.utils.AdapterUtils.getFileName;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.handler.BasicUpdateHandler;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.spoonUpdater.SpoonUpdater;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.visitors.AnnotationRemover;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class CodeAdapter {
  protected Set<AdapterParam> adapterParams;
  protected Set<CDConfParameter> confParams;

  public CodeAdapter(Set<AdapterParam> adapterParams, Set<CDConfParameter> confParams) {
    this.confParams = confParams;
    this.adapterParams = adapterParams;
  }

  /***
   *
   * @param referenceCD the reference class diagram.
   * @param concreteCD, the concrete class diagram.
   * @param refHwcPath   the handwritten reference-code.
   * @param conHwcPath path to the concrete code,
   * @param mappings    different maping names.
   * @param outputPath the output path
   */
  public void adapt(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath) {

    // load CD models
    ASTCDCompilationUnit refCD = JavaLoader.loadCD(referenceCD);
    ASTCDCompilationUnit conCD = JavaLoader.loadCD(concreteCD);

    Set<ASTOrdinaryCompilationUnit> adaptedCode = new HashSet<>();

    for (String mapping : mappings) {

      // check conformance
      CDConformanceChecker checker = new CDConformanceChecker(confParams);
      if (!checker.checkConformance(conCD, refCD, mapping)) {
        Log.error("The concrete CD is not Conform to the Reference CD");
      }

      // check validity of the reference code
      CodeValidator validator = new CodeValidator(refCD, adapterParams);
      if (!validator.isValid(refCD, refHwcPath)) {
        Log.error("The reference code is not valid for the reference CD");
      }

      // build updater and clean output directory
      CodeUpdater updater = new SpoonUpdater();
      updater.setCodePath(refHwcPath);
      updater.setOutputDirectory(outputPath);
      JavaLoader.removeDirectory(outputPath);

      // update handler
      BasicUpdateHandler handler =
          new BasicUpdateHandler(refCD, conCD, conHwcPath, checker, updater, validator);

      // perform updates
      handler.handleUpdate(JavaLoader.readJavaCode(refHwcPath));

      // clean code
      cleanCode(outputPath);
      adaptedCode = mergeAdaptedCode(adaptedCode, JavaLoader.readJavaCode(outputPath));
    }
    JavaLoader.printAST(adaptedCode, outputPath);
  }

  private Set<ASTOrdinaryCompilationUnit> mergeAdaptedCode(
      Set<ASTOrdinaryCompilationUnit> actualCode, Set<ASTOrdinaryCompilationUnit> newAdaptedCode) {
    if (actualCode.isEmpty()) {
      return newAdaptedCode;
    }

    for (ASTOrdinaryCompilationUnit newAdapted : newAdaptedCode) {
      Optional<ASTOrdinaryCompilationUnit> actual =
          actualCode.stream().filter(f -> getFileName(f).equals(getFileName(newAdapted))).findAny();
      if (actual.isEmpty()) {

        actualCode.add(newAdapted);
      } else {
        actualCode.add(AdapterUtils.mergeAsts(actual.get(), newAdapted));
      }
    }
    return actualCode;
  }

  /***
   * clean the annotations and further extra information
   * in the adapted code.
   * @param codePath the code path.
   */
  private static void cleanCode(Path codePath) {

    Set<ASTOrdinaryCompilationUnit> asts = JavaLoader.readJavaCode(codePath);

    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    AnnotationRemover remover = new AnnotationRemover();
    traverser.add4JavaDSL(remover);
    traverser.add4JavaLight(remover);
    traverser.add4MCCommonStatements(remover);
    traverser.add4MCVarDeclarationStatements(remover);

    asts.forEach(ast -> ast.accept(traverser));

    JavaLoader.printAST(asts, codePath);
  }
}
