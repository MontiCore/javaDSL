package de.monticore.codeAdaption.matcher;

import static de.monticore.codeAdaption.utils.Constants.*;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.matcher.annotMatcher.AnnotElementCollector;
import de.monticore.codeAdaption.utils.AdaptAnnotationNames;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.*;
import de.monticore.statements.mccommonstatements._ast.ASTJavaModifier;
import de.monticore.statements.mcstatementsbasis._ast.ASTMCModifier;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.logging.Log;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Creates matching results and applies the naming templates shared by matcher strategies. */
public final class MatcherHelper {

  private MatcherHelper() {}

  /**
   * Creates an executable matching from a name template and its ordered reference symbols.
   *
   * @param template template later expanded with concrete incarnation names
   * @param references symbols consumed by the template in encounter order
   * @return non-ignored matching ready for name generation
   */
  public static CodeMatching mkMatching(String template, List<ISymbol> references) {
    CodeMatching matching = new CodeMatching();

    matching.setIgnore(false);
    matching.setTemplate(template);
    references.forEach(matching::addReference);
    return matching;
  }

  /**
   * Decodes one {@code @Adapt} annotation into a matching and resolves its textual references in
   * the supplied reference CD. Unresolvable references are warned about and omitted.
   */
  public static CodeMatching mkMatchingFromAnnotation(
      ASTMCModifier annotation, ASTCDCompilationUnit cd) {
    // init visitor and traverser
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    AnnotElementCollector collector = new AnnotElementCollector();
    traverser.add4JavaLight(collector);
    annotation.accept(traverser);

    // build matching
    CodeMatching matching = new CodeMatching();
    matching.setExplicitAnnotation(true);
    matching.setTemplate(collector.getTemplate());
    matching.setGenerateTemplate(collector.getGenTemplate());
    matching.setIgnore(collector.isIgnore());

    // resolve and add reference to the matching
    for (String ref : collector.getReferences()) {
      Optional<ISymbol> symbol = AdapterUtils.resolveCDSymbol(ref, cd);
      if (symbol.isPresent()) {
        matching.addReference(symbol.get());
      } else {
        Log.warn(String.format("MatcherHelper: could not resolve annotation reference '%s' in reference CD. Skipping this reference.", ref));
      }
    }

    return matching;
  }

  /**
   * Creates an infix-based matching when at least one candidate symbol occurs in the source name.
   * Redundant references are removed before the replacement template and positional symbol order
   * are calculated.
   */
  public static Optional<CodeMatching> mkMatchingFromInfixRef(
      List<ISymbol> references, String srcName) {

    // check references and build matching
    if (!references.isEmpty()) {
      List<ISymbol> newReferences = MatcherHelper.cleanReferences(references);
      String template = MatcherHelper.mkTemplateFormInfix(srcName, newReferences);
      List<ISymbol> sortedReferences = MatcherHelper.cleanReferences(srcName, newReferences);
      return Optional.of(MatcherHelper.mkMatching(template, sortedReferences));
    }
    return Optional.empty();
  }

  /**
   * Replaces occurrences of reference names in a source identifier with matcher placeholders while
   * preserving whether the occurrence starts with upper or lower case.
   *
   * <p>For example, reference {@code Person} turns {@code PersonRepository} into
   * {@code ${}Repository} and {@code personRepository} into {@code ${uncap_first}Repository}.
   */
  public static String mkTemplateFormInfix(String name, List<ISymbol> infixList) {
    String template = name;

    List<ISymbol> orderedInfixes = new ArrayList<>(infixList);
    orderedInfixes.sort(
        Comparator.comparingInt((ISymbol symbol) -> symbol.getName().length()).reversed());
    for (ISymbol infixSymbol : orderedInfixes) {
      String infix = infixSymbol.getName();
      if (infix.isEmpty()) {
        continue;
      }
      StringBuilder replaced = new StringBuilder(template.length());
      int position = 0;
      while (position < template.length()) {
        if (position + infix.length() > template.length()
            || !template.regionMatches(true, position, infix, 0, infix.length())) {
          replaced.append(template.charAt(position));
          position++;
          continue;
        }
        String matched = template.substring(position, position + infix.length());
        String placeholder =
            matched.equals(uncapFirst(infix)) && !matched.equals(infix)
                ? UNCAP_FIRST_PLACE_HOLDER
                : matched.equals(capFirst(infix)) && !matched.equals(infix)
                    ? CAP_FIRST_PLACE_HOLDER
                    : SIMPLE_PLACE_HOLDER;
        replaced.append(placeholder);
        position += infix.length();
      }
      template = replaced.toString();
    }
    return template;
  }

  /**
   * Selects non-overlapping reference-name occurrences and returns their symbols in source order.
   * Longer candidate names win when two candidates overlap.
   *
   * <p>For {@code UserRepositoryFactory} with candidates {@code User}, {@code Repository}, and
   * {@code UserRepository}, the result starts with {@code UserRepository}; the overlapping
   * {@code User} and {@code Repository} candidates are omitted.
   */
  public static List<ISymbol> cleanReferences(String name, List<ISymbol> infixList) {
    String lowerName = name.toLowerCase(Locale.ROOT);
    Map<Integer, List<ISymbol>> refsByPosition = new TreeMap<>();
    List<ISymbol> longestFirst = new ArrayList<>(infixList);
    longestFirst.sort(
        Comparator.comparingInt((ISymbol symbol) -> symbol.getName().length()).reversed());
    // Prefer longer symbols and reserve their character ranges. Ordinary substring helpers can
    // locate occurrences, but they do not provide this deterministic non-overlap policy.
    boolean[] occupied = new boolean[name.length()];
    for (ISymbol reference : longestFirst) {
      String infix = reference.getName().toLowerCase(Locale.ROOT);
      if (infix.isEmpty()) {
        continue;
      }
      int from = 0;
      while (from <= lowerName.length() - infix.length()) {
        int index = lowerName.indexOf(infix, from);
        if (index < 0) {
          break;
        }
        boolean overlaps = false;
        for (int i = index; i < index + infix.length(); i++) {
          overlaps |= occupied[i];
        }
        if (!overlaps) {
          refsByPosition.computeIfAbsent(index, ignored -> new ArrayList<>()).add(reference);
          Arrays.fill(occupied, index, index + infix.length(), true);
        }
        from = index + infix.length();
      }
    }
    List<ISymbol> result = new ArrayList<>();
    refsByPosition.values().forEach(result::addAll);
    return result;
  }

  /** Returns the adapter annotation among JavaDSL Java modifiers, if present. */
  public static Optional<ASTAnnotation> getInfoJavaAnnot(List<ASTJavaModifier> mods) {
    for (ASTMCModifier mod : mods) {
      if (mod instanceof ASTAnnotation
          && AdaptAnnotationNames.matches(
              ((ASTAnnotation) mod).getAnnotationName().getQName())) {
        return Optional.of((ASTAnnotation) mod);
      }
    }
    return Optional.empty();
  }

  /** Returns the adapter annotation among JavaLight modifiers, if present. */
  public static Optional<ASTAnnotation> getInfoAnnotation(List<ASTMCModifier> mods) {

    for (ASTMCModifier mod : mods) {
      if (mod instanceof ASTAnnotation
          && AdaptAnnotationNames.matches(((ASTAnnotation) mod).getAnnotationName().getQName())) {
        return Optional.of((ASTAnnotation) mod);
      }
    }
    return Optional.empty();
  }

  /**
   * Replaces matcher placeholders with reference-symbol names from left to right.
   *
   * <p>{@code ${}} inserts the symbol name, {@code ${cap_first}} capitalizes its first character,
   * and {@code ${uncap_first}} lowercases its first character. Every placeholder consumes one
   * symbol. Placeholders without a corresponding symbol remain unchanged.
   *
   * @param template the validation or generation name template
   * @param refSymbol reference symbols consumed in placeholder order
   * @return the expanded template, or the original value for null/empty inputs
   */
  public static String fillTemplate(String template, List<ISymbol> refSymbol) {
    if (template == null || template.isEmpty() || refSymbol == null || refSymbol.isEmpty()) {
      return template;
    }
    Pattern pattern = Pattern.compile(PLACE_HOLDER_REGEX);
    Matcher matcher = pattern.matcher(template);
    StringBuffer result = new StringBuffer();
    int referenceIndex = 0;
    while (matcher.find()) {
      if (referenceIndex >= refSymbol.size()) {
        matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
        continue;
      }
      ISymbol field = refSymbol.get(referenceIndex++);
      String pHolder = matcher.group(1);
      String replacement;
      if (pHolder.equals(CAP_FIRST)) {
        replacement = capFirst(field.getName());
      } else if (pHolder.equals(UNCAP_FIRST)) {
        replacement = uncapFirst(field.getName());
      } else {
        replacement = field.getName();
      }
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /** Returns {@code str} with its first character upper-cased, preserving null and empty values. */
  public static String capFirst(String str) {
    return JavaSourceNames.capitalize(str);
  }

  /** Returns {@code str} with its first character lower-cased, preserving null and empty values. */
  public static String uncapFirst(String str) {
    return JavaSourceNames.uncapitalize(str);
  }

  /**
   * Resolves CD type symbols whose names occur both in a variable name and its declared Java type.
   * This avoids treating a type mentioned only in a generic declaration as a variable-name match.
   */
  public static List<ISymbol> resolveReferencesFromType(
      String varName, ASTMCType type, ASTCDCompilationUnit cd) {

    String t = JavaLoader.print(type);

    List<ISymbol> refList = new ArrayList<>();
    for (ASTCDType astcdType : AdapterUtils.getAllCDTypes(cd)) {
      if (t.contains(astcdType.getName()) && matchInfix(varName, astcdType.getName())) {
        refList.add(astcdType.getSymbol());
      }
    }

    return refList;
  }

  /**
   * Removes duplicate symbols and shorter names contained in longer reference names. The returned
   * list is a copy and the input collection is never mutated.
   */
  public static List<ISymbol> cleanReferences(List<ISymbol> references) {
    if (references.isEmpty() || references.size() == 1) {
      return new ArrayList<>(references);
    }

    // Remove duplicates first, then remove shorter reference names embedded in longer references;
    // for example, User is redundant when UserRepository is already a reference.
    List<ISymbol> result = new ArrayList<>(new LinkedHashSet<>(references));
    List<ISymbol> snapshot = new ArrayList<>(result);
    for (ISymbol symbol : snapshot) {
      for (ISymbol other : snapshot) {
        if (!symbol.equals(other)
            && symbol.getName().length() > other.getName().length()
            && matchInfix(symbol.getName(), other.getName())) {
          result.remove(other);
        }
      }
    }
    return result;
  }

  /** Performs a locale-independent, case-insensitive infix test. */
  public static boolean matchInfix(String element, String infix) {
    return element.toLowerCase(Locale.ROOT).contains(infix.toLowerCase(Locale.ROOT));
  }
}
