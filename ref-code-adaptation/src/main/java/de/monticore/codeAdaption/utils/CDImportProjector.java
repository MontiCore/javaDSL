package de.monticore.codeAdaption.utils;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTImportDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Projects external type imports declared by class diagrams into handwritten Java adapters. */
public final class CDImportProjector {

  private CDImportProjector() {}

  public static void project(
      Set<ASTOrdinaryCompilationUnit> javaUnits, ASTCDCompilationUnit... classDiagrams) {
    Map<String, ASTImportDeclaration> imports = parseImports(classDiagrams);
    if (imports.isEmpty()) {
      return;
    }

    for (ASTOrdinaryCompilationUnit unit : javaUnits) {
      Set<String> existing = new LinkedHashSet<>();
      unit.getImportDeclarationList()
          .forEach(importDeclaration -> existing.add(importDeclaration.getMCQualifiedName().getQName()));
      for (Map.Entry<String, ASTImportDeclaration> entry : imports.entrySet()) {
        if (existing.add(entry.getKey())) {
          unit.addImportDeclaration(entry.getValue().deepClone());
        }
      }
    }
  }

  private static Map<String, ASTImportDeclaration> parseImports(
      ASTCDCompilationUnit... classDiagrams) {
    Set<String> qualifiedNames = new LinkedHashSet<>();
    for (ASTCDCompilationUnit classDiagram : classDiagrams) {
      if (classDiagram == null) {
        continue;
      }
      classDiagram.getMCImportStatementList().stream()
          .map(statement -> statement.getMCQualifiedName().getQName())
          .filter(name -> !name.startsWith("java.lang."))
          .forEach(qualifiedNames::add);
    }
    if (qualifiedNames.isEmpty()) {
      return Map.of();
    }

    StringBuilder source = new StringBuilder();
    qualifiedNames.forEach(name -> source.append("import ").append(name).append(";\n"));
    source.append("class CDImportHolder {}\n");
    try {
      Optional<ASTCompilationUnit> parsed =
          JavaDSLMill.parser().parse_StringCompilationUnit(source.toString());
      if (parsed.isEmpty() || !(parsed.get() instanceof ASTOrdinaryCompilationUnit unit)) {
        throw new IllegalStateException("Could not parse class-diagram imports as Java imports");
      }
      Map<String, ASTImportDeclaration> result = new LinkedHashMap<>();
      unit.getImportDeclarationList()
          .forEach(importDeclaration -> result.put(importDeclaration.getMCQualifiedName().getQName(), importDeclaration));
      return result;
    } catch (IOException e) {
      throw new IllegalStateException("Could not parse class-diagram imports as Java imports", e);
    }
  }
}
