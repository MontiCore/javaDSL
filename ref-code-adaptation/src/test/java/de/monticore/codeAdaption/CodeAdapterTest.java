package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterParam;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

public class CodeAdapterTest extends AdapterAbstractTest {
  private final String baseDir = "src/test/resources/de/monticore/codeAdaption/";
  protected Path conHwc = Path.of(baseDir + "hwc");
  private final File REF_CD = new File(baseDir + "App.cd");
  private final File CON_CD = new File(baseDir + "UniApp.cd");

  @TempDir Path temporaryDirectory;

  private Set<CDConfParameter> confParameters;

  private Set<String> mappings = Set.of("ref");

  @BeforeEach
  public void setup() {
    initMills();
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING);
  }

  @Test
  public void testAdaptionWithName() {
    Path outputPath = adapt(Set.of(NAME_MATCHING), "name");

    String student = readFileContent(outputPath, "Student.java");
    Assertions.assertTrue(student.contains("public class Student"));
    Assertions.assertTrue(student.contains("String studentId;"));
  }

  @Test
  public void testAdaptionWithInfix() {
    Path outputPath = adapt(Set.of(INFIX_MATCHING, IGNORE_NON_MATCHED_VAR), "infix");

    String studentBuilder = readFileContent(outputPath, "StudentBuilder.java");
    Assertions.assertTrue(studentBuilder.contains("public class StudentBuilder"));
    Assertions.assertTrue(studentBuilder.contains("protected String studentId;"));
    Assertions.assertTrue(
        studentBuilder.contains("public StudentBuilder setStudentId(String id)"));
    Assertions.assertTrue(studentBuilder.contains("Student student = new Student();"));
  }

  @Test
  public void testAdaptionWithAnnotation() {
    Path outputPath = adapt(Set.of(ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR), "annot");

    String studentRepository = readFileContent(outputPath, "StudentRepository.java");
    Assertions.assertTrue(
        studentRepository.contains("public class StudentRepository extends Repository<Student>"));
    Assertions.assertTrue(
        studentRepository.contains("private Set<Student> studentSet = new HashSet<>();"));
    Assertions.assertTrue(
        studentRepository.contains("public Optional<Student> findStudentByStudentId(String id)"));
    Assertions.assertTrue(
        studentRepository.contains("public List<Student> getAllStudentSortedByStudentId()"));
    Assertions.assertTrue(studentRepository.contains("public void store(Student student)"));
  }

  @Test
  public void testAdaptionWithAllMatchingStrategy() {
    Path outputPath =
        adapt(
            Set.of(NAME_MATCHING, ANNOTATION_MATCHING, INFIX_MATCHING, IGNORE_NON_MATCHED_VAR),
            "compose");

    String studentRepository = readFileContent(outputPath, "StudentRepository.java");
    Assertions.assertTrue(
        studentRepository.contains("public class StudentRepository extends Repository<Student>"));
    Assertions.assertTrue(studentRepository.contains("return Optional.empty();"));
    Assertions.assertTrue(
        studentRepository.contains("List<Student> studentList = new ArrayList<>();"));
    Assertions.assertTrue(studentRepository.contains("studentSet.add(student);"));
  }

  private Path adapt(Set<AdapterParam> adapterParams, String fixtureName) {
    Path outputPath = temporaryDirectory.resolve(fixtureName);
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    adapter.adapt(
        REF_CD,
        CON_CD,
        mappings,
        Path.of(baseDir + "adapter/" + fixtureName),
        conHwc,
        outputPath);
    return outputPath;
  }
}
