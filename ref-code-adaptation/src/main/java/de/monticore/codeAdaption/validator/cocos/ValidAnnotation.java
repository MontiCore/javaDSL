package de.monticore.codeAdaption.validator.cocos;

import static de.monticore.codeAdaption.utils.AdapterUtils.getPosition;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.annotMatcher.AnnotElementCollector;
import de.monticore.codeAdaption.utils.AdaptAnnotationNames;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.Constants;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTJavaAnnotation;
import de.monticore.java.javadsl._cocos.JavaDSLASTJavaAnnotationCoCo;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTAnnotation;
import de.monticore.javalight._cocos.JavaLightASTAnnotationCoCo;
import de.monticore.statements.mcstatementsbasis._ast.ASTMCModifier;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidAnnotation implements JavaDSLASTJavaAnnotationCoCo, JavaLightASTAnnotationCoCo {
  private final ASTCDCompilationUnit cd;

  public ValidAnnotation(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  protected String missingTemplate =
      "0xRC001  %s Invalid annotation: expected a template or ignore set to 'true'";

  protected String templateArguments =
      "0xRC001  %s Invalid annotation:the number of references is [%d], but the template expect [%d] arguments";

  protected String refNotFound =
      "0xRC001  %s Invalid annotation:the  reference [%s] was not found in the class diagram [%s]";

  @Override
  public void check(ASTJavaAnnotation node) {
    if (AdaptAnnotationNames.matches(node.getAnnotationName().getQName())) {
      checkAnnotation(node);
    }
  }

  @Override
  public void check(ASTAnnotation node) {
    if (AdaptAnnotationNames.matches(node.getAnnotationName().getQName())) {
      checkAnnotation(node);
    }
  }

  public void checkAnnotation(ASTMCModifier node) {
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    AnnotElementCollector collector = new AnnotElementCollector();
    traverser.add4JavaLight(collector);
    node.accept(traverser);

    String pos = getPosition(node.get_SourcePositionStart());

    if (!collector.isIgnore()) {

      // template missing
      if (collector.getTemplate() == null) {
        Log.error(String.format(missingTemplate, pos));
      }

      long references = collector.getReferences().size();
      validateTemplateArity(collector.getTemplate(), references, pos);
      if (collector.getGenTemplate() != null && !collector.getGenTemplate().isBlank()) {
        validateTemplateArity(collector.getGenTemplate(), references, pos);
      }

      // reference must exist in the class diagram
      for (String ref : collector.getReferences()) {
        Optional<ISymbol> symbol = AdapterUtils.resolveCDSymbol(ref, cd);
        if (symbol.isEmpty()) {
          Log.error(String.format(refNotFound, pos, ref, cd.getCDDefinition().getName()));
        }
      }
    }
  }

  private void validateTemplateArity(String template, long references, String pos) {
    long arguments = template == null ? 0 : extractPlaceHolders(template).size();
    // A constant template is valid and intentionally ignores its references. As soon as a
    // placeholder is used, every reference must have a corresponding placeholder.
    if (arguments > 0 && arguments != references) {
      Log.error(String.format(templateArguments, pos, references, arguments));
    }
  }

  public static List<String> extractPlaceHolders(String template) {
    List<String> matches = new ArrayList<>();
    Pattern pattern = Pattern.compile(Constants.PLACE_HOLDER_REGEX);
    Matcher matcher = pattern.matcher(template);

    while (matcher.find()) {
      matches.add(matcher.group());
    }

    return matches;
  }
}
