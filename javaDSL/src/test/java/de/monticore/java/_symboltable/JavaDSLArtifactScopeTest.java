package de.monticore.java._symboltable;

import de.monticore.java.javadsl._symboltable.JavaDSLArtifactScope;
import de.monticore.symboltable.ImportStatement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class JavaDSLArtifactScopeTest {
  
  @Test
  void testImports1() {
    JavaDSLArtifactScope scope = new JavaDSLArtifactScope();
    String name = "List";
    String packageName = "de.monticore.java._symboltable";
    List<ImportStatement> imports = List.of(new ImportStatement("java.util", true));
    Set<String> calculatedQualifiedNames =
        scope.calculateQualifiedNames(name, packageName, imports);
    
    List<String> expectedQualifiedNames =
        List.of(name, packageName + "." + name, "java.util.List");
    assertEquals(expectedQualifiedNames.size(), calculatedQualifiedNames.size());
    assertIterableEquals(expectedQualifiedNames, calculatedQualifiedNames);
  }
  
  @Test
  void testImports2() {
    JavaDSLArtifactScope scope = new JavaDSLArtifactScope();
    String name = "List";
    String packageName = "de.monticore.java._symboltable";
    List<ImportStatement> imports = List.of(new ImportStatement("java.util.Iterator", false),
        new ImportStatement("java.util.List", false), new ImportStatement("java.util.Set", false));
    Set<String> calculatedQualifiedNames =
        scope.calculateQualifiedNames(name, packageName, imports);
    
    List<String> expectedQualifiedNames =
        List.of(name, packageName + "." + name, "java.util.List");
    assertEquals(expectedQualifiedNames.size(), calculatedQualifiedNames.size());
    assertIterableEquals(expectedQualifiedNames, calculatedQualifiedNames);
  }
  
  @Test
  void testImports3() {
    JavaDSLArtifactScope scope = new JavaDSLArtifactScope();
    String name = "Map";
    String packageName = "de.monticore.java._symboltable";
    List<ImportStatement> imports = List.of(new ImportStatement("java.util.Map", false));
    Set<String> calculatedQualifiedNames =
        scope.calculateQualifiedNames(name, packageName, imports);
    
    List<String> expectedQualifiedNames =
        List.of(name, packageName + "." + name, "java.util.Map");
    assertEquals(expectedQualifiedNames.size(), calculatedQualifiedNames.size());
    assertIterableEquals(expectedQualifiedNames, calculatedQualifiedNames);
  }
  
  @Test
  void testImports4() {
    JavaDSLArtifactScope scope = new JavaDSLArtifactScope();
    String name = "Map.Entry";
    String packageName = "de.monticore.java._symboltable";
    List<ImportStatement> imports = List.of(new ImportStatement("java.util", true));
    Set<String> calculatedQualifiedNames =
        scope.calculateQualifiedNames(name, packageName, imports);
    
    List<String> expectedQualifiedNames =
        List.of(name, packageName + "." + name, "java.util.Map.Entry");
    assertEquals(expectedQualifiedNames.size(), calculatedQualifiedNames.size());
    assertIterableEquals(expectedQualifiedNames, calculatedQualifiedNames);
  }
  
  @Test
  void testImports5() {
    JavaDSLArtifactScope scope = new JavaDSLArtifactScope();
    String name = "Map.Entry";
    String packageName = "de.monticore.java._symboltable";
    List<ImportStatement> imports = List.of(new ImportStatement("java.util.Map", false),
        new ImportStatement("java.util.List", false), new ImportStatement("java.util.Set", false));
    Set<String> calculatedQualifiedNames =
        scope.calculateQualifiedNames(name, packageName, imports);
    
    List<String> expectedQualifiedNames =
        List.of(name, packageName + "." + name, "java.util.Map.Entry");
    assertEquals(expectedQualifiedNames.size(), calculatedQualifiedNames.size());
    assertIterableEquals(expectedQualifiedNames, calculatedQualifiedNames);
  }
  
  @Test
  void testImports6() {
    JavaDSLArtifactScope scope = new JavaDSLArtifactScope();
    String name = "Map.Entry";
    String packageName = "de.monticore.java._symboltable";
    List<ImportStatement> imports = List.of(new ImportStatement("java.util.Map.Entry", false),
        new ImportStatement("java.util.List", false), new ImportStatement("java.util.Set", false));
    Set<String> calculatedQualifiedNames =
        scope.calculateQualifiedNames(name, packageName, imports);
    
    List<String> expectedQualifiedNames =
        List.of(name, packageName + "." + name);
    assertEquals(expectedQualifiedNames.size(), calculatedQualifiedNames.size());
    assertIterableEquals(expectedQualifiedNames, calculatedQualifiedNames);
  }
  
  @Test
  void testImports7() {
    JavaDSLArtifactScope scope = new JavaDSLArtifactScope();
    String name = "X.Y.Z";
    String packageName = "de.monticore.java._symboltable";
    List<ImportStatement> imports = List.of(new ImportStatement("A.B", false),
        new ImportStatement("A.B.C", false), new ImportStatement("A.B.C.X", false));
    Set<String> calculatedQualifiedNames =
        scope.calculateQualifiedNames(name, packageName, imports);
    
    List<String> expectedQualifiedNames =
        List.of(name, packageName + "." + name, "A.B.C.X.Y.Z");
    assertEquals(expectedQualifiedNames.size(), calculatedQualifiedNames.size());
    assertIterableEquals(expectedQualifiedNames, calculatedQualifiedNames);
  }
}
