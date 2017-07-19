
package typeSystemTestModels.monticore.valid;

import java.lang.String;
/**
 * Contains all attribute properties needed by emf generator
 *
 * @author (last commit) $Author$
 */
public class EmfAttribute {

  private String eDataType;

  private String defaultValue;

  private String definedGrammar;

  private String fullName;

  private boolean isAstNode;

  private boolean isAstList;

  private boolean isOptional;

  private boolean isInherited;

  private boolean isEnum;

  private boolean isExternal;

  private boolean hasExternalType;

  private String ecoreObjectType;

  /**
   * @return ecoreObjectType
   */
  public String getEcoreObjectType() {
    return this.ecoreObjectType;
  }

  /**
   * @param ecoreObjectType the ecoreObjectType to set
   */
  public void setEcoreObjectType(String ecoreObjectType) {
    this.ecoreObjectType = ecoreObjectType;
  }

  /**
   * @return definedGrammar
   */
  public String getDefinedGrammar() {
    return this.definedGrammar;
  }

  /**
   * @return defaultValue
   */
  public String getDefaultValue() {
    return this.defaultValue;
  }

  /**
   * @return eDataType
   */
  public String getEDataType() {
    return this.eDataType;
  }

  /**
   * @return hasExternalType
   */
  public boolean hasExternalType() {
    return this.hasExternalType;
  }

  /**
   * @param hasExternalType the hasExternalType to set
   */
  public void setHasExternalType(boolean hasExternalType) {
    this.hasExternalType = hasExternalType;
  }

  /**
   * @return fullName
   */
  public String getFullName() {
    return this.fullName;
  }

  /**
   * @param fullName the fullName to set
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  /**
   * @return isExternal
   */
  public boolean isExternal() {
    return this.isExternal;
  }

  /**
   * @return istAstNode
   */
  public boolean isAstNode() {
    return this.isAstNode;
  }

  /**
   * @param istAstNode the istAstNode to set
   */
  public void setAstNode(boolean isAstNode) {
    this.isAstNode = isAstNode;
  }

  /**
   * @return isASTList
   */
  public boolean isAstList() {
    return this.isAstList;
  }

  /**
   * @return isEnum
   */
  public boolean isEnum() {
    return this.isEnum;
  }

  /**
   * @return isInherited
   */
  public boolean isInherited() {
    return this.isInherited;
  }

  /**
   * @return isOptionalAstNode
   */
  public boolean isOptional() {
    return this.isOptional;
  }

  /**
   * @param isOptionalAstNode the isOptionalAstNode to set
   */
  public void setOptional(boolean isOptional) {
    this.isOptional = isOptional;
  }

  /**
   * @param isASTList the isASTList to set
   */
  public void setAstList(boolean isASTList) {
    this.isAstList = isASTList;
  }

  private String createDefaultValue() {
    if (isAstNode()) {
      return "null";
    }
    //initially it was String but String is not allowed in the expression of switch statement in java 6
    Integer typeName = 10;
    switch (typeName) {
      case 1:
        return "false";
      case 2:
        return "0";
      case 3:
        return "(short) 0";
      case 4:
        return "0";
      case 5:
        return "0.0f";
      case 6:
        return "0.0";
      case 7:
        return "'\u0000'";
      default:
        return "null";
    }
  }

  public String getEmfType() {
    return (isAstNode() || isAstList()) ? "EReference" : "EAttribute";
  }

  public boolean isEAttribute() {
    return "EAttribute".equals(getEmfType());
  }

  public boolean isEReference() {
    return "EReference".equals(getEmfType());
  }

  public EmfAttribute(
      String name,
      String eDataType,
      String definedGrammar,
      boolean isAstNode,
      boolean isAstList,
      boolean isOptional,
      boolean isInherited,
      boolean isExternal,
      boolean isEnum,
      boolean hasExternalType) {
    this.fullName = name;
    this.definedGrammar = definedGrammar;
    this.isAstNode = isAstNode;
    this.isAstList = isAstList;
    this.isOptional = isOptional;
    this.isInherited = isInherited;
    this.isExternal = isExternal;
    this.isEnum = isEnum;
    this.hasExternalType = hasExternalType;
    this.eDataType = eDataType;
    this.defaultValue = createDefaultValue();
  }

  public String toString() {
    return "+ of + "+ EType
        + getEmfType() + " + ";
  }

}
