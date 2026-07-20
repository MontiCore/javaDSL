package de.monticore.codeAdaption.validator;

import static de.monticore.codeAdaption.utils.AdapterParam.*;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CodeValidatorTest extends AdapterAbstractTest {
  @TempDir Path tempDir;

  protected String baseDir = "src/test/resources/de/monticore/codeAdaption/validator/";
  protected CodeValidator validator;
  protected ASTCDCompilationUnit cd;
  protected Set<AdapterParam> confParameters =
      Set.of(IGNORE_NON_MATCHED_TYPE, IGNORE_NON_MATCHED_TYPE_MEMBER, IGNORE_NON_MATCHED_VAR);

  @BeforeEach
  public void setup() {
    initMills();
    Log.enableFailQuick(false);
    cd = JavaLoader.loadCD(new File(baseDir + "Validator.cd"));
    validator = new CodeValidator(cd, confParameters);
  }

  public static Stream<Arguments> invalidFiles1() {
    return Stream.of(
        Arguments.of("InvalidReference.java", "0xRC001"),
        Arguments.of("ManyVarInOneFieldDecl.java", "0xRC002"),
        Arguments.of("ManyVarInOneLocalVarDecl.java", "0xRC002"),
        Arguments.of("MissingTemplate.java", "0xRC001"),
        Arguments.of("MissingTemplateArgument.java", "0xRC001"));
  }

  @ParameterizedTest
  @MethodSource("invalidFiles1")
  public void checkAnnotationPhase1(String filename, String errorCode) {

    String fileName = baseDir + "invalid/" + filename;
    ASTOrdinaryCompilationUnit ast = JavaLoader.loadJava(new File(fileName));
    validator.runAdapterCoCos(ast, cd);

    Assertions.assertEquals(1, Log.getErrorCount());
    Assertions.assertTrue(Log.getFindings().get(0).getMsg().startsWith(errorCode));
  }

  @Test
  void literalDollarDoesNotCountAsTemplatePlaceholder() throws IOException {
    ASTOrdinaryCompilationUnit ast =
        javaSource(
            "LiteralDollar.java",
            """
            import de.monticore.codeAdaption.utils.Adapt;
            @Adapt(ref = "Entity", template = "Price$${}", genTemplate = "Generated$${}")
            class LiteralDollar {}
            """);

    validator.runAdapterCoCos(ast, cd);

    Assertions.assertEquals(0, Log.getErrorCount());
  }

  @Test
  void generatedTemplateMustMatchReferenceArity() throws IOException {
    ASTOrdinaryCompilationUnit ast =
        javaSource(
            "InvalidGeneratedTemplate.java",
            """
            import de.monticore.codeAdaption.utils.Adapt;
            @Adapt(ref = "Entity", template = "${}", genTemplate = "${}${}")
            class InvalidGeneratedTemplate {}
            """);

    validator.runAdapterCoCos(ast, cd);

    Assertions.assertEquals(1, Log.getErrorCount());
    Assertions.assertTrue(Log.getFindings().get(0).getMsg().startsWith("0xRC001"));
  }

  @Test
  void validationInitializesSourceTypesBeforeCheckingSupertypes() throws IOException {
    Path sources = Files.createDirectory(tempDir.resolve("source-local-supertype"));
    Files.writeString(sources.resolve("Base.java"), "class Base {}");
    Files.writeString(sources.resolve("Child.java"), "class Child extends Base {}");
    CodeValidator strictMembers =
        new CodeValidator(cd, Set.of(IGNORE_NON_MATCHED_TYPE, IGNORE_NON_MATCHED_VAR));

    Assertions.assertTrue(strictMembers.isValid(cd, sources));
  }

  @Test
  void validationRejectsUnmatchedExternalSupertype() throws IOException {
    Path sources = Files.createDirectory(tempDir.resolve("external-supertype"));
    Files.writeString(sources.resolve("Child.java"), "class Child extends ExternalBase {}");
    CodeValidator strictMembers =
        new CodeValidator(cd, Set.of(IGNORE_NON_MATCHED_TYPE, IGNORE_NON_MATCHED_VAR));

    Assertions.assertFalse(strictMembers.isValid(cd, sources));
  }

  @Test
  void validationCanIgnoreUnmatchedExternalSupertype() throws IOException {
    Path sources = Files.createDirectory(tempDir.resolve("ignored-supertype"));
    Files.writeString(sources.resolve("Child.java"), "class Child extends ExternalBase {}");
    CodeValidator ignoredMembers =
        new CodeValidator(
            cd,
            Set.of(
                IGNORE_NON_MATCHED_TYPE,
                IGNORE_NON_MATCHED_TYPE_MEMBER,
                IGNORE_NON_MATCHED_VAR));

    Assertions.assertTrue(ignoredMembers.isValid(cd, sources));
  }

  private ASTOrdinaryCompilationUnit javaSource(String fileName, String source) throws IOException {
    Path file = tempDir.resolve(fileName);
    Files.writeString(file, source);
    return JavaLoader.loadJava(file.toFile());
  }

}
