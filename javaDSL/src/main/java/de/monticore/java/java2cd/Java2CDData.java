/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.java2cd;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;

public class Java2CDData {

  protected final ASTCDCompilationUnit compilationUnit;

  public Java2CDData(ASTCDCompilationUnit compilationUnit) {
    this.compilationUnit = compilationUnit;
  }

  public ASTCDCompilationUnit getCompilationUnit() {
    return compilationUnit;
  }
}
