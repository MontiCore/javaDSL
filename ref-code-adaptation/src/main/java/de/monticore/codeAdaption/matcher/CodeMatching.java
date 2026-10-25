package de.monticore.codeAdaption.matcher;

import de.monticore.symboltable.ISymbol;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes how one handwritten Java element is related to elements of the reference CD.
 *
 * <p>The {@linkplain #getReferences() references} are reference-CD symbols, not concrete
 * incarnations. During adaptation, they are resolved to concrete incarnations and inserted, in
 * list order, into {@linkplain #getTemplate() the naming template}. For example, references
 * {@code [Entity, Entity.id]} and template {@code find${}By${cap_first}} can produce {@code
 * findPersonByNumber} when {@code Entity -> Person} and {@code id -> number}.
 *
 * <p>A matching may instead represent an explicit {@code @Adapt(ignore = true)} instruction. In
 * that case {@link #mustBePerform()} returns {@code false}: the element remains relevant to
 * filtering, but no adaptation is performed on it.
 */
public class CodeMatching {
  /** Whether the matched Java element is intentionally excluded from adaptation. */
  private boolean ignore = false;
  private boolean explicitAnnotation;

  /** Reference-CD symbols consumed by the naming templates, in placeholder order. */
  private final List<ISymbol> references = new ArrayList<>();

  /** Template for adapting an existing handwritten name. */
  private String template;
  /** Optional template used when generating a new declaration. */
  private String generateTemplate;

  public CodeMatching(boolean ignore) {
    this.ignore = ignore;
  }

  public CodeMatching() {}

  /** Returns the mutable, placeholder-ordered list of reference-CD symbols. */
  public List<ISymbol> getReferences() {
    return references;
  }

  /** Returns the template used to adapt an existing handwritten name. */
  public String getTemplate() {
    return template;
  }

  /** Returns the optional generation template, or {@code null} when none was configured. */
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

  /** Returns whether this matching requests adaptation rather than explicit exclusion. */
  public boolean mustBePerform() {
    return !ignore;
  }

  public void setExplicitAnnotation(boolean explicitAnnotation) {
    this.explicitAnnotation = explicitAnnotation;
  }

  /** Returns whether this matching originated from an explicit {@code @Adapt} annotation. */
  public boolean isExplicitAnnotation() {
    return explicitAnnotation;
  }
}
