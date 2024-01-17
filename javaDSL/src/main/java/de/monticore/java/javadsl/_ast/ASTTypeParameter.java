// (c) https://github.com/MontiCore/monticore
package de.monticore.java.javadsl._ast;

import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.Optional;

public class ASTTypeParameter extends ASTTypeParameterTOP {

  @Override
  public Optional<ASTMCType> getMCTypeOpt() {
    return Optional.empty();
  }

}
