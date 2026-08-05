/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class JavaDSLToolTest extends AbstractTest {

  @TempDir
  private Path OUTPUT_DIR;
  
  public static Stream<Arguments> testTool(){
    return Stream.of(
        arguments(
            Paths.get("src","test","resources","de","monticore","java","parser","Test.java"),
            List.of(Paths.get("de","monticore","foo", "bla", "Test.java"))),
        arguments(
            Paths.get("src","test","resources","de","monticore","java","parser","ASTClassDeclaration.java"),
            List.of(
                Paths.get("de","monticore","java","javadsl","_ast","ASTClassDeclaration.java"),
                Paths.get("de","monticore","java","javadsl","_ast","Builder.java")))
    );
  }
  
  @ParameterizedTest
  @MethodSource
  public void testTool(Path inputPath, List<Path> relOutputPaths) {
    ArrayList<String> path = new ArrayList<>();
    Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).forEach(p -> {
      path.add("-path");
      path.add(p);
    });
    
    String[] cmdBase =
        new String[] { "-i", inputPath.toString(), "-o", OUTPUT_DIR.toString(), "-c2mc" };
    String[] toolArgs = Stream.concat(Arrays.stream(cmdBase), path.stream()).toArray(String[]::new);
    
    JavaDSLTool.main(toolArgs);
    
    for (Path relExpectedOutputPath : relOutputPaths) {
      Path expectedOutput = OUTPUT_DIR.resolve(relExpectedOutputPath);
      assertTrue(expectedOutput.toFile().exists(),
          "could not find generated file: " + expectedOutput);
      assertTrue(expectedOutput.toFile().length() > 0,
          "generated file is empty: " + expectedOutput);
    }
    
    assertTrue(Log.getFindings().isEmpty(), "log is not empty");
  }
}
