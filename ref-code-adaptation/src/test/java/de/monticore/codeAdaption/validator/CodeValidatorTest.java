package de.monticore.codeAdaption.validator;

import static de.monticore.codeAdaption.utils.AdapterParam.*;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CodeValidatorTest extends AdapterAbstractTest {
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
    validator.runCoCosPhase1(ast, cd);

    Assertions.assertEquals(1, Log.getErrorCount());
    Assertions.assertTrue(Log.getFindings().get(0).getMsg().startsWith(errorCode));
  }

}
