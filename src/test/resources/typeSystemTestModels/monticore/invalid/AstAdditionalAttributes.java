package typeSystemTestModels.monticore.invalid;

import java.lang.String;
import java.lang.Integer;

public class AstAdditionalAttributes {

  private String attributeDeclaration;

  private AstAdditionalAttributes(boolean def) {
    this.attributeDeclaration = def;
  }

  public Integer getDeclaration() {
    return attributeDeclaration;
  }

}
