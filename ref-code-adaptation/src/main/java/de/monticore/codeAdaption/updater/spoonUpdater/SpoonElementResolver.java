package de.monticore.codeAdaption.updater.spoonUpdater;

import static de.monticore.codeAdaption.utils.JavaLoader.print;

import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

final class SpoonElementResolver {

  private final Supplier<CtModel> model;
  private final Map<ASTTypeDeclaration, CtType<?>> typeMap = new LinkedHashMap<>();
  private final Map<ASTMethodDeclaration, CtMethod<?>> methodMap = new LinkedHashMap<>();

  SpoonElementResolver(Supplier<CtModel> model) {
    this.model = model;
  }

  void reset() {
    typeMap.clear();
    methodMap.clear();
  }

  CtType<?> getSpoonType(ASTTypeDeclaration mcType) {
    if (typeMap.containsKey(mcType)) {
      return typeMap.get(mcType);
    }
    CtModel currentModel = model.get();
    if (currentModel == null) {
      throw new IllegalStateException("Cannot resolve a type before a Spoon model has been loaded");
    }
    List<CtType<?>> candidates =
        currentModel.getAllTypes().stream()
            .filter(candidate -> mcType.getName().equals(candidate.getSimpleName()))
            .toList();
    CtType<?> type = resolveTypeBySource(mcType, candidates);
    typeMap.put(mcType, type);
    return type;
  }

  CtMethod<?> getSpoonMethod(ASTTypeDeclaration mcType, ASTMethodDeclaration mcMethod) {
    return findSpoonMethod(mcType, mcMethod)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No Spoon method corresponds to '"
                        + mcMethod.getName()
                        + "' in type '"
                        + mcType.getName()
                        + "'"));
  }

  Optional<CtMethod<?>> findSpoonMethod(
      ASTTypeDeclaration mcType, ASTMethodDeclaration mcMethod) {
    if (methodMap.containsKey(mcMethod)) {
      return Optional.of(methodMap.get(mcMethod));
    }

    CtType<?> spoonType = getSpoonType(mcType);
    List<CtMethod<?>> candidates =
        spoonType.getMethods().stream()
            .filter(candidate -> compare(mcMethod, candidate))
            .toList();
    Optional<CtMethod<?>> method =
        candidates.isEmpty()
            ? Optional.empty()
            : Optional.of(
                requireUnique(
                    candidates,
                    "declared Spoon method '"
                        + mcMethod.getName()
                        + "' in '"
                        + spoonType.getQualifiedName()
                        + "'"));
    method.ifPresent(value -> methodMap.put(mcMethod, value));
    return method;
  }

  void cacheType(ASTTypeDeclaration mcType, CtType<?> spoonType) {
    typeMap.put(mcType, spoonType);
  }

  boolean compare(ASTTypeDeclaration type, CtType<?> spoonType) {
    if (!type.getName().equals(spoonType.getSimpleName())) {
      return false;
    }
    Optional<Path> expectedFile = sourceFile(type);
    if (expectedFile.isEmpty()) {
      return true;
    }
    return spoonSourceFile(spoonType)
        .map(
            actual ->
                actual.equals(expectedFile.get())
                    || commonTrailingSegments(actual, expectedFile.get()) >= 2)
        .orElse(false);
  }

  boolean compare(ASTMethodDeclaration mcMethod, CtMethod<?> spoonMethod) {
    if (!mcMethod.getName().equals(spoonMethod.getSimpleName())) {
      return false;
    }
    if (!mcMethod.getFormalParameters().isPresentFormalParameterListing()) {
      return spoonMethod.getParameters().isEmpty();
    }
    List<ASTFormalParameter> mcParams =
        mcMethod.getFormalParameters().getFormalParameterListing().getFormalParameterList();
    if (spoonMethod.getParameters().size() != mcParams.size()) {
      return false;
    }
    for (int i = 0; i < spoonMethod.getParameters().size(); i++) {
      String expected = JavaSourceNames.normalizeType(print(mcParams.get(i).getMCType()));
      String actual = normalizedSpoonType(spoonMethod.getParameters().get(i).getType());
      if (!expected.equals(actual)) {
        return false;
      }
    }

    return true;
  }

  private static String normalizedSpoonType(spoon.reflect.reference.CtTypeReference<?> type) {
    if (type == null) {
      return "Object";
    }
    return JavaSourceNames.normalizeType(type.toString());
  }

  private static Optional<Path> sourceFile(ASTTypeDeclaration type) {
    return type.get_SourcePositionStart().getFileName().flatMap(SpoonElementResolver::pathOf);
  }

  private static CtType<?> resolveTypeBySource(
      ASTTypeDeclaration mcType, List<CtType<?>> candidates) {
    if (candidates.isEmpty()) {
      throw new IllegalStateException(
          "No Spoon type corresponds to JavaDSL type '" + mcType.getName() + "'");
    }
    Optional<Path> expected = sourceFile(mcType);
    if (expected.isPresent()) {
      List<CtType<?>> exact =
          candidates.stream()
              .filter(
                  candidate ->
                      spoonSourceFile(candidate).map(expected.get()::equals).orElse(false))
              .toList();
      if (!exact.isEmpty()) {
        return requireUnique(exact, "exact source match for '" + mcType.getName() + "'");
      }

      int greatestSuffix =
          candidates.stream()
              .map(SpoonElementResolver::spoonSourceFile)
              .flatMap(Optional::stream)
              .mapToInt(actual -> commonTrailingSegments(actual, expected.get()))
              .max()
              .orElse(0);
      if (greatestSuffix >= 2) {
        List<CtType<?>> suffixMatches =
            candidates.stream()
                .filter(
                    candidate ->
                        spoonSourceFile(candidate)
                                .map(actual -> commonTrailingSegments(actual, expected.get()))
                                .orElse(0)
                            == greatestSuffix)
                .toList();
        return requireUnique(
            suffixMatches,
            "best source-suffix match for '" + mcType.getName() + "' from " + expected.get());
      }
    }
    return requireUnique(
        candidates,
        "globally unique Spoon type named '"
            + mcType.getName()
            + "' (source "
            + expected.map(Path::toString).orElse("unknown")
            + ")");
  }

  private static Optional<Path> spoonSourceFile(CtType<?> type) {
    if (type.getPosition() == null || !type.getPosition().isValidPosition()) {
      return Optional.empty();
    }
    return Optional.of(type.getPosition().getFile().toPath().toAbsolutePath().normalize());
  }

  private static int commonTrailingSegments(Path first, Path second) {
    int common = 0;
    int firstIndex = first.getNameCount() - 1;
    int secondIndex = second.getNameCount() - 1;
    while (firstIndex >= 0
        && secondIndex >= 0
        && first.getName(firstIndex).toString().equals(second.getName(secondIndex).toString())) {
      common++;
      firstIndex--;
      secondIndex--;
    }
    return common;
  }

  private static Optional<Path> pathOf(String fileName) {
    try {
      return Optional.of(Path.of(fileName).toAbsolutePath().normalize());
    } catch (InvalidPathException ignored) {
      return Optional.empty();
    }
  }

  private static <T> T requireUnique(List<T> candidates, String description) {
    int candidateCount = candidates.size();
    if (candidateCount != 1) {
      throw new IllegalStateException(
          "Expected exactly one " + description + " but found " + candidateCount);
    }
    return candidates.get(0);
  }
}
