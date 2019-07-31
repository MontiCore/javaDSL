/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.report;

import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.generating.templateengine.reporting.reporter.SymbolTableReporter;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.Symbol;

/** *
 * @author MB
 */
public class JavaDSLSymbolTableReporter extends SymbolTableReporter {
  
  /**
   * Constructor for report.JavaDSLSymbolTableReporter.
   * 
   * @param outputDir
   * @param modelName
   * @param repository
   */
  public JavaDSLSymbolTableReporter(
      String outputDir,
      String modelName,
      ReportingRepository repository) {
    super(outputDir, modelName, repository);
    setPrintEmptyList(true);
    setPrintEmptyOptional(true);
  }
  
  @Override
  protected void reportAttributes(Symbol sym, IndentPrinter printer) {
    super.reportAttributes(sym, printer);
    if (sym instanceof JavaFieldSymbol) {
      reportAttributes((JavaFieldSymbol) sym, printer);
    }
    else if (sym instanceof JavaMethodSymbol) {
      reportAttributes((JavaMethodSymbol) sym, printer);
    }
    else if (sym instanceof JavaTypeSymbol) {
      reportAttributes((JavaTypeSymbol) sym, printer);
    }
  }
  
  private void reportAttributes(JavaFieldSymbol sym, IndentPrinter printer) {
    reportCommonJFieldAttributes(sym, printer);
    printer.println("isVolatile = " + sym.isVolatile() + ";");
    printer.println("isTransient = " + sym.isTransient() + ";");
    reportListOfReferences("annotations", sym.getAnnotations(), printer);
  }

  private void reportAttributes(JavaMethodSymbol sym, IndentPrinter printer) {
    reportCommonJMethodAttributes(sym, printer);
    printer.println("isNative = " + sym.isNative() + ";");
    printer.println("isSynchronized = " + sym.isSynchronized() + ";");
    printer.println("isStrictfp = " + sym.isStrictfp() + ";");
    reportListOfReferences("annotations", sym.getAnnotations(), printer);
  }

  private void reportAttributes(JavaTypeSymbol sym, IndentPrinter printer) {
    reportCommonJTypeAttributes(sym, printer);
    printer.println("isAnnotation = " + sym.isAnnotation() + ";");
    printer.println("isStatic = " + sym.isStatic() + ";");
    printer.println("isStrictfp = " + sym.isStrictfp() + ";");
    printer.println("isTypeVariable = " + sym.isTypeVariable() + ";");
    reportListOfReferences("annotations", sym.getAnnotations(), printer);
  }
}
