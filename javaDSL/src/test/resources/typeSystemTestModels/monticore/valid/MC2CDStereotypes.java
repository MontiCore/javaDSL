/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.monticore.valid;

import java.lang.String;

public class MC2CDStereotypes {
//  /**
//   * The rule attribute is defined in a super grammar
//   */
//  INHERITED("inherited"),
//  /**
//   * Type defined in the Java language
//   */
//  EXTERNAL_TYPE("externalType"),
//  /**
//   * Referenced symbol
//   */
//  REFERENCED_SYMBOL("referencedSymbol");

  private final String stereotype;

  private MC2CDStereotypes(String stereotype) {
    this.stereotype = stereotype;
  }

  @Override
  public String toString() {
    return stereotype;
  }

}
