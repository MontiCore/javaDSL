/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.java2cd;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;

public class Java2CDConverter {

  public Java2CDData doConvert(ASTCompilationUnit ast, GlobalExtensionManagement glex) {

    Java2CDVisitor visitor = new Java2CDVisitor(glex);
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(visitor);
    traverser.add4JavaLight(visitor);

    CD4CodeMill.init();
    ast.accept(traverser);

    return new Java2CDData(visitor.getCompilationUnit());
  }

}
