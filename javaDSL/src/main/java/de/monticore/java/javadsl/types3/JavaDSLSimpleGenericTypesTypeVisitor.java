package de.monticore.java.javadsl.types3;

import de.monticore.java.javadsl._ast.ASTMCBasicGenericType;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.types.mcsimplegenerictypes.types3.MCSimpleGenericTypesTypeVisitor;

public class JavaDSLSimpleGenericTypesTypeVisitor extends MCSimpleGenericTypesTypeVisitor implements JavaDSLVisitor2 {
  
  @Override
  public void endVisit(ASTMCBasicGenericType genericType) {
    super.endVisit(genericType);
  }
}
