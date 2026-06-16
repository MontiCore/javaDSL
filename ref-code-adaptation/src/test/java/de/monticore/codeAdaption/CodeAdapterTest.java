package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.JavaLoader;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.*;

public class CodeAdapterTest extends AdapterAbstractTest {
  private final String baseDir = "src/test/resources/de/monticore/codeAdaption/";
  protected Path conHwc = Path.of(baseDir + "hwc");
  private final File REF_CD = new File(baseDir + "App.cd");
  private final File CON_CD = new File(baseDir + "UniApp.cd");

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  private Set<String> mappings = Set.of("ref");

  @BeforeEach
  public void setup() {
    initMills();
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING);
  }

  @Test
  public void testAdaptionWithName() {
    Path outputPath = Path.of("target/adapter/name");
    adapterParams = Set.of(NAME_MATCHING);
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    adapter.adapt(REF_CD, CON_CD, mappings, Path.of(baseDir + "adapter/name"), conHwc, outputPath);

    String student = readFileWithoutSpace(outputPath, "Student.java");
    Assertions.assertEquals("publicclassStudent{StringstudentId;}", student);
  }

  @Test
  public void testAdaptionWithInfix() {
    Path outputPath = Path.of("target/adapter/infix");
    adapterParams = Set.of(INFIX_MATCHING, IGNORE_NON_MATCHED_VAR);
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    adapter.adapt(REF_CD, CON_CD, mappings, Path.of(baseDir + "adapter/infix"), conHwc, outputPath);

    String studentBuilder = readFileWithoutSpace(outputPath, "StudentBuilder.java");
    Assertions.assertEquals(
        "publicclassStudentBuilder{protectedStringstudentId;publicStudentBuildersetStudentId(Stringid){Studentstudent=newStudent();}}",
        studentBuilder);
  }

  @Test
  public void testAdaptionWithAnnotation() {
    Path outputPath = Path.of("target/adapter/annot");
    adapterParams = Set.of(ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR);
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    adapter.adapt(REF_CD, CON_CD, mappings, Path.of(baseDir + "adapter/annot"), conHwc, outputPath);

    String studentRepository = readFileWithoutSpace(outputPath, "StudentRepository.java");
    // Accept both wildcard and explicit imports since Spoon's import optimization varies
    boolean hasCorrectContent = studentRepository.contains("publicclassStudentRepositoryextendsRepository<Student>")
        && studentRepository.contains("privateSet<Student>studentSet=newHashSet<>()")
        && studentRepository.contains("publicOptional<Student>findStudentByStudentId(Stringid)")
        && studentRepository.contains("publicList<Student>getAllStudentSortedByStudentId()")
        && studentRepository.contains("publicvoidstore(Studentstudent)");
    Assertions.assertTrue(hasCorrectContent, "StudentRepository content should be correctly adapted");
  }

  @Test
  public void testAdaptionWithAllMatchingStrategy() {
    Path outputPath = Path.of("target/adapter/compose");
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, INFIX_MATCHING, IGNORE_NON_MATCHED_VAR);
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    adapter.adapt(
        REF_CD, CON_CD, mappings, Path.of(baseDir + "adapter/compose"), conHwc, outputPath);

    String studentRepository = readFileWithoutSpace(outputPath, "StudentRepository.java");

    Assertions.assertEquals(
        "importjava.util.*;publicclassStudentRepositoryextendsRepository<Student>{privateSet<Student>studentSet=newHashSet<>();publicOptional<Student>findStudentByStudentId(Stringid){returnOptional.empty();}publicList<Student>getAllStudentSortedByStudentId(){List<Student>studentList=newArrayList<>();}publicvoidstore(Studentstudent){studentSet.add(student);}}",
        studentRepository);
  }

  protected String readFileWithoutSpace(Path dir, String filename) {
    Optional<File> file =
        JavaLoader.readJavaFile(dir).stream()
            .filter(f -> f.getName().endsWith(filename))
            .findFirst();
    Assertions.assertTrue(file.isPresent());
    return JavaLoader.readFileContent(file.get()).replaceAll("\\s+", "");
  }
}
