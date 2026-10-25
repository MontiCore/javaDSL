package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.utils.JavaSourceNames;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import spoon.reflect.declaration.CtImportKind;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtIntersectionTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;

/** Shared qualification-preserving identity for executable signatures and projection. */
final class SpoonTypeIdentity {
  private SpoonTypeIdentity() {}

  /** Reads the reference graph directly; Spoon's pretty printer may suppress known qualifiers. */
  static String qualifiedSource(CtTypeReference<?> type) {
    if (type instanceof CtArrayTypeReference<?> array) {
      return qualifiedSource(array.getComponentType()) + "[]";
    }
    if (type instanceof CtWildcardReference wildcard) {
      CtTypeReference<?> bound = wildcard.getBoundingType();
      if (bound == null
          || (wildcard.isUpper() && "java.lang.Object".equals(bound.getQualifiedName()))) {
        return "?";
      }
      return "?" + (wildcard.isUpper() ? " extends " : " super ") + qualifiedSource(bound);
    }
    if (type instanceof CtIntersectionTypeReference<?> intersection) {
      return intersection.getBounds().stream()
          .map(SpoonTypeIdentity::qualifiedSource)
          .collect(Collectors.joining(" & "));
    }
    String name = type.getQualifiedName();
    if (!type.getActualTypeArguments().isEmpty()) {
      name +=
          type.getActualTypeArguments().stream()
              .map(SpoonTypeIdentity::qualifiedSource)
              .collect(Collectors.joining(",", "<", ">"));
    }
    return name;
  }

  /** Resolves unqualified leaves only from unique model declarations or direct java.lang. */
  static String canonical(SpoonWorkspace workspace, String source) {
    return JavaSourceNames.canonicalType(
        JavaSourceNames.replaceTypeNames(
            source,
            reference -> {
              if (reference.qualified()) {
                return Optional.empty();
              }
              List<String> modelTypes = modelTypes(workspace, reference.simpleName());
              if (modelTypes.size() == 1) {
                return Optional.of(modelTypes.get(0));
              }
              return javaLang(reference.simpleName());
            }));
  }

  /** Binds projected simple names using the source owner before falling back to unique model types. */
  static String canonical(SpoonWorkspace workspace, CtType<?> owner, String source) {
    String bound =
        JavaSourceNames.replaceTypeNames(
            source,
            reference -> {
              if (reference.qualified()) {
                return Optional.empty();
              }
              if (owner.getFormalCtTypeParameters().stream()
                  .anyMatch(parameter -> parameter.getSimpleName().equals(reference.simpleName()))) {
                return Optional.empty();
              }
              if (owner.getPosition().isValidPosition()) {
                List<String> imported =
                    owner.getPosition().getCompilationUnit().getImports().stream()
                        .filter(value -> value.getImportKind() == CtImportKind.TYPE)
                        .map(value -> (CtTypeReference<?>) value.getReference())
                        .filter(value -> value.getSimpleName().equals(reference.simpleName()))
                        .map(CtTypeReference::getQualifiedName)
                        .distinct()
                        .toList();
                if (imported.size() == 1) {
                  return Optional.of(imported.get(0));
                }
                if (imported.size() > 1) {
                  throw new IllegalStateException("Ambiguous projected type imports: " + imported);
                }
              }
              List<String> candidates = modelTypes(workspace, reference.simpleName());
              String packageName = owner.getPackage().getQualifiedName();
              String localName =
                  packageName.isBlank()
                      ? reference.simpleName()
                      : packageName + "." + reference.simpleName();
              if (candidates.contains(localName)) {
                return Optional.of(localName);
              }
              if (candidates.size() == 1) {
                return Optional.of(candidates.get(0));
              }
              if (candidates.size() > 1) {
                throw new IllegalStateException(
                    "Ambiguous projected type '" + reference.simpleName() + "': " + candidates);
              }
              return javaLang(reference.simpleName());
            });
    return JavaSourceNames.canonicalType(bound);
  }

  private static List<String> modelTypes(SpoonWorkspace workspace, String simpleName) {
    return workspace.model().getAllTypes().stream()
        .filter(type -> simpleName.equals(type.getSimpleName()))
        .map(CtType::getQualifiedName)
        .distinct()
        .toList();
  }

  private static Optional<String> javaLang(String simpleName) {
    String qualified = "java.lang." + simpleName;
    try {
      Class.forName(qualified, false, SpoonTypeIdentity.class.getClassLoader());
      return Optional.of(qualified);
    } catch (ClassNotFoundException | LinkageError ignored) {
      return Optional.empty();
    }
  }
}
