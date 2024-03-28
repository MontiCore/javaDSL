/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.javadsl._ast;

import de.monticore.cd.facade.MCQualifiedNameFacade;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;

public class ASTMCQualifiedType extends ASTMCQualifiedTypeTOP {

  public ASTMCQualifiedName getMCQualifiedName() {
    StringBuilder name = new StringBuilder();
    annotatedNames.stream()
        .map(ASTAnnotatedName::getName)
        .forEach(n -> name.append(n).append("."));
    return MCQualifiedNameFacade.createQualifiedName(name.toString());
  }

}
