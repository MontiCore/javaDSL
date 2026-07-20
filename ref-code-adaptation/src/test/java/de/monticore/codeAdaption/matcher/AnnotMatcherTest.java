package de.monticore.codeAdaption.matcher;

import de.monticore.codeAdaption.matcher.annotMatcher.AnnotVariableMatcher;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AnnotMatcherTest extends MatcherAbstractTest {
  private CodeValidator validator;

  @Override
  public void init(String javaFile) {
    super.init(javaFile);
    validator = new CodeValidator(cd, Set.of(AdapterParam.ANNOTATION_MATCHING));
  }

  @ParameterizedTest(name = "annotation matching for {0}")
  @ValueSource(strings = {"type", "method", "field", "local", "parameter"})
  void matchesAnnotatedElements(String elementKind) {
    init("/annotMatcher/EntityRepository.java");
    ASTTypeDeclaration type = collector.getAllTypeDeclarations().get(0);
    ExpectedMatching expected = expectedMatching(elementKind, type);
    Optional<CodeMatching> matching = expected.matching();

    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(
        expected.referenceNames(),
        matching.get().getReferences().stream().map(value -> value.getName()).toList());
    Assertions.assertEquals(expected.template(), matching.get().getTemplate());
    Assertions.assertTrue(matching.get().mustBePerform());
  }

  @Test
  void variableMatcherUsesReplacedReferenceCD() {
    init("/annotMatcher/EntityRepository.java");
    var unrelatedCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/validator/Validator.cd"));
    AnnotVariableMatcher matcher = new AnnotVariableMatcher(unrelatedCD);
    matcher.setReferenceCD(cd);

    ASTTypeDeclaration type = collector.getAllTypeDeclarations().get(0);
    ASTMethodDeclaration method = collector.getAllMethodDeclarations(type).get(2);
    ASTLocalVariableDeclaration local = collector.getAllLocVariables(type, method).get(0);
    Optional<CodeMatching> matching = matcher.getMatchedLocalVariable(type, method, local);

    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(
        List.of("Entity"),
        matching.get().getReferences().stream().map(value -> value.getName()).toList());
  }

  private ExpectedMatching expectedMatching(String elementKind, ASTTypeDeclaration type) {
    return switch (elementKind) {
      case "type" ->
          new ExpectedMatching(
              validator.getMatchedType(type), List.of("Entity"), "${}Repository");
      case "method" -> {
        ASTMethodDeclaration method = collector.getAllMethodDeclarations(type).get(0);
        yield new ExpectedMatching(
            validator.getMatchedMethod(type, method),
            List.of("Entity", "id"),
            "find${}By${cap_first}");
      }
      case "field" -> {
        ASTFieldDeclaration field = collector.getAllFieldDeclarations(type).get(0);
        yield new ExpectedMatching(
            validator.getMatchedField(type, field),
            List.of("Entity"),
            "${uncap_first}Set");
      }
      case "local" -> {
        ASTMethodDeclaration method = collector.getAllMethodDeclarations(type).get(2);
        ASTLocalVariableDeclaration local = collector.getAllLocVariables(type, method).get(0);
        yield new ExpectedMatching(
            validator.getMatchedLocalVariable(type, method, local),
            List.of("Entity"),
            "${uncap_first}List");
      }
      case "parameter" -> {
        ASTMethodDeclaration method = collector.getAllMethodDeclarations(type).get(3);
        ASTFormalParameter parameter = collector.getAllParameters(type, method).get(0);
        yield new ExpectedMatching(
            validator.getMatchedParameter(type, method, parameter),
            List.of("Entity"),
            "${uncap_first}");
      }
      default -> throw new IllegalArgumentException("Unknown element kind: " + elementKind);
    };
  }

  private record ExpectedMatching(
      Optional<CodeMatching> matching, List<String> referenceNames, String template) {}
}
