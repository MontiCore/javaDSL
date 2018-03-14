/* (c) https://github.com/MontiCore/monticore */


package typeSystemTestModels.monticore.invalid;

import java.lang.String;
import java.util.ArrayList;

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
    return new ArrayList<>();
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
    }String typeName = "Hello";
    switch (typeName) {
      case "Hi":
        return "false";
      case "Bye":
        return "0";
      case "Good morning":
        return "(short) 0";
      case new Integer(10):
        return "0";
      case "Yo":
        return "0.0f";
      case true:
        return "0.0";
      case "Ayo":
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
    return "+ of + "
        + getEmfType() + " + ";
  }

}
