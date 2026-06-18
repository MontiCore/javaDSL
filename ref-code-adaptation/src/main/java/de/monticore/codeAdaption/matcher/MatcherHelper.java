package de.monticore.codeAdaption.matcher;

import static de.monticore.codeAdaption.utils.Constants.*;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.matcher.annotMatcher.AnnotElementCollector;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTJavaAnnotation;
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
import java.util.stream.Collectors;

/***
 * helper functions of the matcher.
 */
public class MatcherHelper {

  public static CodeMatching mkMatching(String template, List<ISymbol> references) {
    CodeMatching matching = new CodeMatching();

    matching.setIgnore(false);
    matching.setTemplate(template);
    references.forEach(matching::addReference);
    return matching;
  }

  public static CodeMatching mkMatchingFromAnnotation(
      ASTMCModifier annotation, ASTCDCompilationUnit cd) {
    // init visitor and traverser
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    AnnotElementCollector collector = new AnnotElementCollector();
    traverser.add4JavaLight(collector);
    annotation.accept(traverser);

    // build matching
    CodeMatching matching = new CodeMatching();
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

  public static String mkTemplateFormInfix(String name, List<ISymbol> infixList) {
    String template = name;

    List<ISymbol> orderedInfixes = new ArrayList<>(infixList);
    orderedInfixes.sort(
        Comparator.comparingInt((ISymbol symbol) -> symbol.getName().length()).reversed());
    for (ISymbol infixSymbol : orderedInfixes) {
      String infix = infixSymbol.getName();
      if (name.contains(infix)) {
        template = template.replace(infix, SIMPLE_PLACE_HOLDER);
      }
      if (name.contains(capFirst(infix))) {
        template = template.replace(capFirst(infix), CAP_FIRST_PLACE_HOLDER);
      }
      if (name.contains(uncapFirst(infix))) {
        template = template.replace(uncapFirst(infix), UNCAP_FIRST_PLACE_HOLDER);
      }
    }
    return template;
  }

  public static List<ISymbol> cleanReferences(String name, List<ISymbol> infixList) {
    List<String> refs =
        infixList.stream().map(ref -> ref.getName().toLowerCase()).collect(Collectors.toList());
    Map<Integer, ISymbol> refMap = new LinkedHashMap<>();

    for (int i = 0; i < refs.size(); i++) {
      refMap.put(name.toLowerCase().indexOf(refs.get(i)), infixList.get(i));
    }

    List<ISymbol> result = new ArrayList<>();
    for (int i = 0; i < name.length(); i++) {
      if (refMap.containsKey(i)) {
        result.add(refMap.get(i));
      }
    }
    return result;
  }

  public static Optional<ASTJavaAnnotation> getInfoJavaAnnot(List<ASTJavaModifier> mods) {
    for (ASTJavaModifier mod : mods) {
      if (mod instanceof ASTJavaAnnotation
          && ((ASTJavaAnnotation) mod).getAnnotationName().getQName().equals(ANNOT_NAME)) {
        return Optional.of((ASTJavaAnnotation) mod);
      }
    }
    return Optional.empty();
  }

  public static Optional<ASTAnnotation> getInfoAnnotation(List<ASTMCModifier> mods) {

    for (ASTMCModifier mod : mods) {
      if (mod instanceof ASTAnnotation
          && ((ASTAnnotation) mod).getAnnotationName().getQName().equals(ANNOT_NAME)) {
        return Optional.of((ASTAnnotation) mod);
      }
    }
    return Optional.empty();
  }

  /***
   * fill template with the reference.
   * @param template the template.
   * @param refSymbol the template arguments
   * @return the generated String.
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

  public static String capFirst(String str) {
    return JavaSourceNames.capitalize(str);
  }

  public static String uncapFirst(String str) {
    return JavaSourceNames.uncapitalize(str);
  }

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

  public static List<ISymbol> cleanReferences(List<ISymbol> references) {
    if (references.isEmpty() || references.size() == 1) {
      return references;
    }

    Set<ISymbol> temps = new LinkedHashSet<>(references);
    for (ISymbol symbol : temps) {
      for (ISymbol symbol1 : temps) {
        if (!symbol.equals(symbol1) && (matchInfix(symbol.getName(), symbol1.getName()))) {
          references.remove(symbol1);
        }
      }
    }
    return references;
  }

  public static boolean matchInfix(String element, String infix) {
    return element.toLowerCase().contains(infix.toLowerCase());
  }
}
