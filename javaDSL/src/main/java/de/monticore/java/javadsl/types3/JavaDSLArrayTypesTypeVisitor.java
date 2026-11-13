package de.monticore.java.javadsl.types3;

import de.monticore.java.javadsl._ast.ASTMCArrayType;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.types.mcarraytypes.types3.MCArrayTypesTypeVisitor;

public class JavaDSLArrayTypesTypeVisitor extends MCArrayTypesTypeVisitor implements
    JavaDSLVisitor2 {
  
  public void endVisit(ASTMCArrayType arrayType) {
    super.endVisit(arrayType);
  }
}
