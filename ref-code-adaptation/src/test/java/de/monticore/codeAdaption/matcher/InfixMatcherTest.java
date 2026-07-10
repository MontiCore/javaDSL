package de.monticore.codeAdaption.matcher;

import static de.monticore.codeAdaption.utils.AdapterParam.INFIX_MATCHING;

import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.*;

class InfixMatcherTest extends MatcherAbstractTest {
  private CodeValidator validator;

  @Override
  public void init(String javaFile) {
    super.init(javaFile);
    validator = new CodeValidator(cd, Set.of(INFIX_MATCHING));
  }

  @Test
  public void TestTypeInfixMatching() {
    init("/infixMatcher/EntityRepository.java");
    ASTTypeDeclaration entityRepos = collector.getAllTypeDeclarations().get(0);
    Optional<CodeMatching> matching = validator.getMatchedType(entityRepos);

    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(1, matching.get().getReferences().size());
    Assertions.assertEquals("Entity", matching.get().getReferences().get(0).getName());
    Assertions.assertEquals("${}Repository", matching.get().getTemplate());
    Assertions.assertTrue(matching.get().mustBePerform());
  }

  @Test
  public void TestMethodInfixMatching() {
    init("/infixMatcher/EntityRepository.java");
    ASTTypeDeclaration entityRepos = collector.getAllTypeDeclarations().get(0);
    ASTMethodDeclaration method = collector.getAllMethodDeclarations(entityRepos).get(0);

    Optional<CodeMatching> matching = validator.getMatchedMethod(entityRepos, method);
    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(2, matching.get().getReferences().size());
    Assertions.assertEquals("Entity", matching.get().getReferences().get(0).getName());
    Assertions.assertEquals("id", matching.get().getReferences().get(1).getName());
    Assertions.assertEquals("find${}By${cap_first}", matching.get().getTemplate());
    Assertions.assertTrue(matching.get().mustBePerform());
  }

  @Test
  public void TestAttributeInfixMatching() {
    init("/infixMatcher/EntityRepository.java");
    ASTTypeDeclaration entityRepos = collector.getAllTypeDeclarations().get(0);
    ASTFieldDeclaration attribute = collector.getAllFieldDeclarations(entityRepos).get(0);

    Optional<CodeMatching> matching = validator.getMatchedField(entityRepos, attribute);
    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(1, matching.get().getReferences().size());
    Assertions.assertEquals("Entity", matching.get().getReferences().get(0).getName());
    Assertions.assertEquals("${uncap_first}Set", matching.get().getTemplate());
    Assertions.assertTrue(matching.get().mustBePerform());
  }

  @Test
  public void TestLocalVariableInfixMatching() {
    init("/infixMatcher/EntityRepository.java");
    ASTTypeDeclaration type = collector.getAllTypeDeclarations().get(0);
    ASTMethodDeclaration method = collector.getAllMethodDeclarations(type).get(2);
    ASTLocalVariableDeclaration localVar = collector.getAllLocVariables(type, method).get(0);

    Optional<CodeMatching> matching = validator.getMatchedLocalVariable(type, method, localVar);
    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(1, matching.get().getReferences().size());
    Assertions.assertEquals("Entity", matching.get().getReferences().get(0).getName());
    Assertions.assertEquals("${uncap_first}List", matching.get().getTemplate());
    Assertions.assertTrue(matching.get().mustBePerform());
  }

  @Test
  public void TestFormalParameterAnnotationMatching() {
    init("/infixMatcher/EntityRepository.java");
    ASTTypeDeclaration type = collector.getAllTypeDeclarations().get(0);
    ASTMethodDeclaration method = collector.getAllMethodDeclarations(type).get(3);
    ASTFormalParameter param = collector.getAllParameters(type, method).get(0);

    Optional<CodeMatching> matching = validator.getMatchedParameter(type, method, param);
    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(1, matching.get().getReferences().size());
    Assertions.assertEquals("Entity", matching.get().getReferences().get(0).getName());
    Assertions.assertEquals("${uncap_first}", matching.get().getTemplate());
    Assertions.assertTrue(matching.get().mustBePerform());
  }

  @Test
  void repeatedInfixesProduceOneReferencePerPlaceholder() {
    init("/infixMatcher/EntityRepository.java");
    ASTCDType entity =
        AdapterUtils.getAllCDTypes(cd).stream()
            .filter(type -> "Entity".equals(type.getName()))
            .findFirst()
            .orElseThrow();

    CodeMatching matching =
        MatcherHelper.mkMatchingFromInfixRef(
                List.of(entity.getSymbol()), "EntityToEntity")
            .orElseThrow();

    Assertions.assertEquals("${}To${}", matching.getTemplate());
    Assertions.assertEquals(2, matching.getReferences().size());
    Assertions.assertEquals(
        "EntityToEntity",
        MatcherHelper.fillTemplate(matching.getTemplate(), matching.getReferences()));
  }
}
