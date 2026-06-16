package de.monticore.codeAdaption.matcher;

import de.monticore.symboltable.ISymbol;
import java.util.ArrayList;
import java.util.List;

/***
 * Save matching information, each Matching is attached to an element (type,field,method,..) of
 * the reference code.
 * It saves the necessary implementation to build the concrete name of this element.
 */
public class CodeMatching {
  /***
   * is true when the code element has to be ignored during the adaptation.
   */
  private boolean ignore = false;

  /***
   * CD-references of the element
   */
  private final List<ISymbol> references = new ArrayList<>();

  /***
   * must be field with the concrete as argument reference to build the new name
   */
  private String template;
  private String generateTemplate;

  public CodeMatching(boolean ignore) {
    this.ignore = ignore;
  }

  public CodeMatching() {}

  public List<ISymbol> getReferences() {
    return references;
  }

  public String getTemplate() {
    return template;
  }

  public String getGenerateTemplate() {
    return generateTemplate;
  }

  public void addReference(ISymbol reference) {
    this.references.add(reference);
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public void setGenerateTemplate(String generateTemplate) {
    this.generateTemplate = generateTemplate;
  }

  public void setIgnore(boolean ignore) {
    this.ignore = ignore;
  }

  public boolean mustBePerform() {
    return !ignore;
  }
}
