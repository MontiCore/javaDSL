/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.symboltable;

import de.monticore.symboltable.Scope;
import de.monticore.symboltable.types.references.CommonJTypeReference;
import de.monticore.symboltable.types.references.JTypeReference;

public class JavaTypeSymbolReference extends CommonJTypeReference<JavaTypeSymbol> implements JTypeReference<JavaTypeSymbol> {

  public JavaTypeSymbolReference(String name, Scope definingScopeOfReference, int arrayDimension) {
    super(name, JavaTypeSymbol.KIND, definingScopeOfReference);
    setDimension(arrayDimension);
  }

  @Override
  public String toString() {
    return getName();
  }
}
