/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import javax.tools.*;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Compiles test models with javac. The type check tests use it as an oracle: a model that is
 * expected to be valid must compile, and a model that is expected to be invalid must not compile.
 * This ensures that a failing JavaDSL test is caused by the JavaDSL and not by a wrong test model.
 *
 * <p>The models are compiled for {@link #JAVA_VERSION}, the Java version targeted by the JavaDSL,
 * independently of the JDK that executes the tests. The compiled classes are discarded, so that
 * the compilations of different tests do not influence each other.</p>
 *
 * <p>{@code de.monticore.runtime.junit.TestJavaCompiler} is not used, as it only compiles files,
 * fails on compile errors, and writes and loads the compiled classes.</p>
 */
public final class Javac {

  /** The Java version whose language the JavaDSL aims to support. */
  public static final int JAVA_VERSION = 21;

  /** Reused for all compilations, as the file manager caches the JDK classes. */
  private static StandardJavaFileManager fileManager;

  private static JavaCompiler javac;

  private Javac() {
  }

  public static void assertCompiles(String... sources) {
    assertCompiles(sourceObjects(sources), String.join("\n", sources));
  }

  public static void assertDoesNotCompile(String... sources) {
    assertDoesNotCompile(sourceObjects(sources), String.join("\n", sources));
  }

  public static void assertCompiles(List<Path> files) {
    assertCompiles(fileObjects(files), files.toString());
  }

  public static void assertDoesNotCompile(List<Path> files) {
    assertDoesNotCompile(fileObjects(files), files.toString());
  }

  private static void assertCompiles(List<JavaFileObject> sources, String models) {
    List<Diagnostic<? extends JavaFileObject>> errors = compile(sources);
    if (!errors.isEmpty()) {
      fail("javac rejects the model:\n" + print(errors) + "\n" + models);
    }
  }

  private static void assertDoesNotCompile(List<JavaFileObject> sources, String models) {
    if (compile(sources).isEmpty()) {
      fail("javac accepts the model that is expected to be invalid:\n" + models);
    }
  }

  private static synchronized List<Diagnostic<? extends JavaFileObject>> compile(
      List<JavaFileObject> sources) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    // -proc:none disables annotation processing, -Xlint:none and -nowarn only report errors
    List<String> options = List.of("--release", String.valueOf(JAVA_VERSION),
        "-proc:none", "-Xlint:none", "-nowarn");
    javac().getTask(null, new DiscardingFileManager(fileManager), diagnostics, options, null,
        sources).call();
    return diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
  }

  private static synchronized JavaCompiler javac() {
    if (javac == null) {
      javac = ToolProvider.getSystemJavaCompiler();
      assertNotNull(javac, "javac is not available");
      fileManager = javac.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
    }
    return javac;
  }

  private static List<JavaFileObject> sourceObjects(String... sources) {
    List<JavaFileObject> result = new ArrayList<>();
    for (int i = 0; i < sources.length; i++) {
      result.add(new SourceString("Model" + i, sources[i]));
    }
    return result;
  }

  private static List<JavaFileObject> fileObjects(List<Path> files) {
    javac();
    List<JavaFileObject> result = new ArrayList<>();
    fileManager.getJavaFileObjectsFromPaths(files).forEach(result::add);
    return result;
  }

  private static String print(List<Diagnostic<? extends JavaFileObject>> errors) {
    return errors.stream()
        .map(d -> (d.getSource() == null ? "" : d.getSource().getName()) + ":"
            + d.getLineNumber() + ": " + d.getMessage(Locale.ROOT))
        .collect(Collectors.joining("\n"));
  }

  /** Discards the compiled classes. */
  private static final class DiscardingFileManager
      extends ForwardingJavaFileManager<StandardJavaFileManager> {

    DiscardingFileManager(StandardJavaFileManager fileManager) {
      super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className,
        JavaFileObject.Kind kind, FileObject sibling) {
      URI uri = URI.create("discarded:///" + className.replace('.', '/') + kind.extension);
      return new SimpleJavaFileObject(uri, kind) {
        @Override
        public OutputStream openOutputStream() {
          return OutputStream.nullOutputStream();
        }
      };
    }
  }

  /**
   * A model given as source code. Its name is compatible with every type name, as javac would
   * otherwise require a public type {@code X} (or a module declaration) to be declared in a file
   * {@code X.java} (or {@code module-info.java}).
   */
  private static final class SourceString extends SimpleJavaFileObject {

    private final String source;

    SourceString(String name, String source) {
      super(URI.create("string:///" + name + ".java"), Kind.SOURCE);
      this.source = source;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
      return kind == Kind.SOURCE;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return source;
    }
  }
}
