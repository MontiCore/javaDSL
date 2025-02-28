package de.monticore.codeAdaption.matcher;

import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NameTypeMatcherTest extends MatcherAbstractTest {
  private CodeValidator validator;

  @Override
  public void init(String javaFile) {
    super.init(javaFile);
    validator = new CodeValidator(cd, Set.of(AdapterParam.NAME_MATCHING));
  }

  @Test
  public void TestTypeNameMatching() {
    init("/nameMatcher/Entity.java");
    ASTTypeDeclaration entity = collector.getAllTypeDeclarations().get(0);
    Optional<CodeMatching> matching = validator.getMatchedType(entity);

    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(1, matching.get().getReferences().size());
    Assertions.assertEquals("Entity", matching.get().getReferences().get(0).getName());
    Assertions.assertEquals("${}", matching.get().getTemplate());
    Assertions.assertTrue(matching.get().mustBePerform());
  }

  @Test
  public void TestAttributeNameMatching() {
    init("/nameMatcher/Entity.java");
    ASTTypeDeclaration entityRepos = collector.getAllTypeDeclarations().get(0);
    ASTFieldDeclaration attribute = collector.getAllFieldDeclarations(entityRepos).get(0);

    Optional<CodeMatching> matching = validator.getMatchedField(entityRepos, attribute);
    Assertions.assertTrue(matching.isPresent());
    Assertions.assertEquals(1, matching.get().getReferences().size());
    Assertions.assertEquals("id", matching.get().getReferences().get(0).getName());
    Assertions.assertEquals("${}", matching.get().getTemplate());
    Assertions.assertTrue(matching.get().mustBePerform());
  }
}
