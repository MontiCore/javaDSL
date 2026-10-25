package de.monticore.codeAdaption.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.monticore.cd.codegen.DecoratorConfig;
import de.monticore.cd.codegen.trafo.TOPTrafo;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdgen.CDGenTool;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import javax.tools.ToolProvider;

/** Generates real CD2Pojo model classes below the adapter's handwritten TOP layers. */
final class Cd2PojoTestSupport {

  private Cd2PojoTestSupport() {}

  static void generateWithAdaptedTopLayer(Path model, Path handwrittenCode, Path output) {
    new AdaptedTopCDGenTool()
        .run(
            new String[] {
              "-i", model.toString(),
              "-o", output.toString(),
              "-hwc", handwrittenCode.toString(),
              "-ct", "cd2java.init.CD2Pojo",
              "-c", "-c2mc"
            });
  }

  /** Compiles both source trees and calls the generated API through the public HWC class. */
  static void compileAndExercise(Path adapted, Path generated, Path classes, String roleName)
      throws Exception {
    List<Path> sources = new ArrayList<>();
    for (Path root : List.of(adapted, generated)) {
      try (var files = Files.walk(root)) {
        sources.addAll(files.filter(path -> path.toString().endsWith(".java")).toList());
      }
    }
    Files.createDirectories(classes);
    var compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "The integration test requires a JDK");
    StringWriter diagnostics = new StringWriter();
    try (var manager = compiler.getStandardFileManager(null, null, null)) {
      assertEquals(
          Boolean.TRUE,
          compiler
              .getTask(
                  diagnostics,
                  manager,
                  null,
                  List.of("-d", classes.toString(), "-classpath", System.getProperty("java.class.path")),
                  null,
                  manager.getJavaFileObjectsFromPaths(sources))
              .call(),
          diagnostics::toString);
    }
    try (var loader =
        new URLClassLoader(
            new URL[] {classes.toUri().toURL()}, Cd2PojoTestSupport.class.getClassLoader())) {
      Class<?> studentClass = loader.loadClass("Concrete.Student");
      Class<?> roleClass = loader.loadClass("Concrete.HiwiRole");
      Object student = studentClass.getConstructor().newInstance();
      Object role = roleClass.getConstructor().newInstance();
      roleClass.getMethod("setName", String.class).invoke(role, "Tutor");
      String addMethod = "add" + Character.toUpperCase(roleName.charAt(0)) + roleName.substring(1);
      studentClass.getMethod(addMethod, roleClass).invoke(student, role);
      assertEquals("Tutor, ", studentClass.getMethod("printHiwiRoles").invoke(student));
    }
  }

  private static final class AdaptedTopCDGenTool extends CDGenTool {

    @Override
    public void decorateAndGenerate(
        GlobalExtensionManagement glex,
        Consumer<DecoratorConfig> initializeDecConf,
        GeneratorSetup setup,
        Runnable initDecoratedGlobalScope,
        Consumer<ASTCDCompilationUnit> postDecorate,
        Collection<ASTCDCompilationUnit> asts) {
      super.decorateAndGenerate(
          glex,
          initializeDecConf,
          setup,
          initDecoratedGlobalScope,
          decorated -> {
            postDecorate.accept(decorated);
            // Reserve the public HWC layer before CDGen's normal final TOP transformation.
            // With both Student.java and adapted StudentTOP.java on the HWC path, this
            // transforms Student -> StudentTOP, and the normal pass produces StudentTOPTOP.
            // Applying the upstream transformation to the same decorated AST preserves the
            // getter/setter/default-value template hooks created by CD2Pojo.
            var traverser = CD4CodeMill.inheritanceTraverser();
            new TOPTrafo(setup.getHandcodedPath()).addToTraverser(traverser);
            decorated.accept(traverser);
          },
          asts);
    }
  }
}
