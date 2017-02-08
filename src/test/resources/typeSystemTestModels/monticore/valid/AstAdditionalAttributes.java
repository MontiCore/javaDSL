package typeSystemTestModels.monticore.valid;

import java.lang.String;

public class AstAdditionalAttributes {

  private String attributeDeclaration;

  private AstAdditionalAttributes(String def) {
    this.attributeDeclaration = def;
  }

  public String getDeclaration() {
    return attributeDeclaration;
  }

}