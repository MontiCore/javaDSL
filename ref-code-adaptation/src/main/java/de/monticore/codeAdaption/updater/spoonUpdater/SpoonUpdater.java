package de.monticore.codeAdaption.updater.spoonUpdater;

import static de.monticore.codeAdaption.utils.JavaLoader.print;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.utils.Constants;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.JavaSourcePostProcessor;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import spoon.reflect.visitor.filter.TypeFilter;
import de.se_rwth.commons.logging.Log;
import spoon.Launcher;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.refactoring.Refactoring;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.support.compiler.VirtualFile;

public class SpoonUpdater implements CodeUpdater {
  private File outputDir;
  private Launcher launcher;
  private CtModel spoonModel;
  // Mapping from concrete simple name -> grouping/simple interface name
  private Map<String, String> groupingMappings = Collections.emptyMap();
  private final Map<String, List<String>> concreteMethodSignatures = new LinkedHashMap<>();
  private final Map<StableElementKey, StableElementKey> methodRewrites = new LinkedHashMap<>();
  private final Map<ASTTypeDeclaration, CtType<?>> typeMap = new LinkedHashMap<>();
  private final Map<ASTMethodDeclaration, CtMethod<?>> methodMap = new LinkedHashMap<>();

  @Override
  public void setCodePath(Path path) {
    try {
      JavaSourcePostProcessor.processDirectory(path);
    } catch (IOException e) {
      Log.error("SpoonUpdater.setCodePath: Failed to prepare source code: " + e.getMessage());
    }

    // init spoon environment
    launcher = new Launcher();
    launcher.getEnvironment().setAutoImports(true);
    //launcher.getEnvironment().setShouldCompile(true);
    launcher.getEnvironment().setNoClasspath(true);

    // TODO: Ask Max
    // No more custom pretty-printer - use Spoon's default
    // The custom pretty-printer was causing annotation removal to fail
    // and did not fix spacing issues

    // add code to the environment
    launcher.addInputResource(path.toAbsolutePath().toString());

    // build model
    launcher.buildModel();
    spoonModel = launcher.getModel();
  }

  @Override
  public Set<File> printCode() {
    // Apply grouping/type replacements on the Spoon model before pretty-printing
    applyGroupingToModel();
    applyConcreteMethodSignatures();
    launcher.setSourceOutputDirectory(outputDir);
    launcher.prettyprint();
    return JavaLoader.readJavaFile(outputDir.toPath());
  }

  @Override
  public void cleanCode(Path codePath) {
    Path formattedPath = codePath.resolveSibling(codePath.getFileName() + "_formatted");
    try {
      FileUtils.deleteQuietly(formattedPath.toFile());
      Files.createDirectories(formattedPath);
      Map<Path, Path> originalFilesByRelativePath = javaFilesByRelativePath(codePath);

      Launcher cleanupLauncher = new Launcher();
      cleanupLauncher.getEnvironment().setAutoImports(true);
      cleanupLauncher.getEnvironment().setNoClasspath(true);
      try (var paths = Files.walk(codePath)) {
        paths
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(p -> cleanupLauncher.addInputResource(p.toAbsolutePath().toString()));
      }
      cleanupLauncher.buildModel();

      List<CtAnnotation<?>> annotations =
          new ArrayList<>(
              cleanupLauncher.getModel().getElements(new TypeFilter<>(CtAnnotation.class)));
      annotations.stream().filter(SpoonUpdater::isAdaptAnnotation).forEach(CtAnnotation::delete);

      cleanupLauncher.setSourceOutputDirectory(formattedPath.toFile());
      cleanupLauncher.prettyprint();

      Set<Path> copiedTargets = new LinkedHashSet<>();
      try (var paths = Files.walk(formattedPath)) {
        paths
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(
                p -> {
                  try {
                    Path relativePath = formattedPath.relativize(p);
                    Path target =
                        originalFilesByRelativePath.getOrDefault(
                            relativePath, codePath.resolve(relativePath));
                    Files.createDirectories(target.getParent());
                    copyReplacingWithRetry(p, target);
                    copiedTargets.add(target.toAbsolutePath().normalize());
                  } catch (IOException e) {
                    throw new IllegalStateException(
                        "Failed to copy formatted file '" + p + "' to output", e);
                  }
                });
      }
      deleteRehomedOriginals(originalFilesByRelativePath.values(), copiedTargets);

      JavaSourcePostProcessor.processDirectory(codePath);
      Log.info("SpoonUpdater.cleanCode: Cleanup completed", "CodeAdapter");
    } catch (IOException e) {
      Log.error("SpoonUpdater.cleanCode: Failed to clean code: " + e.getMessage());
    } finally {
      FileUtils.deleteQuietly(formattedPath.toFile());
    }
  }

  private static Map<Path, Path> javaFilesByRelativePath(Path codePath) throws IOException {
    Map<Path, Path> result = new LinkedHashMap<>();
    try (var paths = Files.walk(codePath)) {
      paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> result.put(codePath.relativize(path), path));
    }
    return result;
  }

  private static void deleteRehomedOriginals(Collection<Path> originalFiles, Set<Path> copiedTargets)
      throws IOException {
    for (Path original : originalFiles) {
      Path normalizedOriginal = original.toAbsolutePath().normalize();
      if (!copiedTargets.contains(normalizedOriginal) && Files.exists(original)) {
        Files.delete(original);
      }
    }
  }

  private static void copyReplacingWithRetry(Path source, Path target) throws IOException {
    IOException lastException = null;
    for (int attempt = 0; attempt < 5; attempt++) {
      try {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return;
      } catch (IOException e) {
        lastException = e;
        try {
          Thread.sleep(50L * (attempt + 1));
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw e;
        }
      }
    }
    throw lastException;
  }

  private static boolean isAdaptAnnotation(CtAnnotation<?> annotation) {
    CtTypeReference<?> annotationType = annotation.getAnnotationType();
    if (annotationType != null) {
      String simpleName = annotationType.getSimpleName();
      String qualifiedName = annotationType.getQualifiedName();
      if (Constants.ANNOT_NAME.equals(simpleName)
          || simpleName.endsWith("." + Constants.ANNOT_NAME)
          || Constants.ANNOT_PACKAGE.equals(qualifiedName)
          || qualifiedName.endsWith("." + Constants.ANNOT_NAME)) {
        return true;
      }
    }
    String rendered = annotation.toString().trim();
    return rendered.startsWith("@" + Constants.ANNOT_NAME)
        || rendered.startsWith("@." + Constants.ANNOT_NAME)
        || rendered.contains("." + Constants.ANNOT_NAME + "(")
        || rendered.contains("." + Constants.ANNOT_NAME + "[");
  }


  public void setGroupingMappings(Map<String, String> mappings) {
    if (mappings == null || mappings.isEmpty()) {
      this.groupingMappings = Collections.emptyMap();
    } else {
      this.groupingMappings = new LinkedHashMap<>(mappings);
    }
  }

  @Override
  public void registerConcreteMethodSignature(String methodName, List<String> parameterTypes) {
    if (methodName == null || methodName.isEmpty() || parameterTypes == null) {
      return;
    }
    concreteMethodSignatures.put(methodName, new ArrayList<>(parameterTypes));
  }

  @Override
  public void registerMethodRewrite(StableElementKey referenceMethod, StableElementKey concreteMethod) {
    if (referenceMethod == null
        || concreteMethod == null
        || referenceMethod.getKind() != StableElementKey.Kind.METHOD
        || concreteMethod.getKind() != StableElementKey.Kind.METHOD) {
      return;
    }
    methodRewrites.put(referenceMethod, concreteMethod);
  }

  private void applyConcreteMethodSignatures() {
    if (concreteMethodSignatures.isEmpty() && methodRewrites.isEmpty()) {
      return;
    }
    List<CtInvocation<?>> invocations = spoonModel.getElements(new TypeFilter<>(CtInvocation.class));
    for (CtInvocation<?> invocation : invocations) {
      String methodName = invocation.getExecutable() != null ? invocation.getExecutable().getSimpleName() : null;
      StableElementKey concreteTarget = findConcreteRewriteTarget(invocation, methodName);
      List<String> parameterTypes =
          concreteTarget != null
              ? concreteTarget.getParameterTypes()
              : getUnambiguousLegacyParameterTypes(methodName);
      if (parameterTypes == null) {
        continue;
      }
      if (invocation.getArguments().size() > parameterTypes.size()) {
        throw new IllegalStateException(
            "Concrete method '"
                + methodName
                + "' has fewer parameters than the reference invocation in "
                + invocation);
      }
      if (invocation.getArguments().size() >= parameterTypes.size()) {
        continue;
      }
      for (int i = invocation.getArguments().size(); i < parameterTypes.size(); i++) {
        invocation.addArgument(defaultExpression(parameterTypes.get(i)));
      }
    }
  }

  private StableElementKey findConcreteRewriteTarget(CtInvocation<?> invocation, String methodName) {
    if (methodName == null || methodRewrites.isEmpty()) {
      return null;
    }
    String owner = ownerName(invocation);
    List<StableElementKey> matches = new ArrayList<>();
    for (StableElementKey concrete : methodRewrites.values()) {
      if (!methodName.equals(concrete.getName())) {
        continue;
      }
      if (owner != null && concrete.getOwnerType().isPresent()) {
        String concreteOwner = concrete.getOwnerType().get();
        if (!owner.equals(concreteOwner) && !owner.equals(groupingMappings.get(concreteOwner))) {
          continue;
        }
      }
      matches.add(concrete);
    }
    if (matches.size() == 1) {
      return matches.get(0);
    }
    if (matches.size() > 1) {
      throw new IllegalStateException(
          "Ambiguous method rewrite for invocation '" + methodName + "' with owner '" + owner + "'");
    }
    return null;
  }

  private List<String> getUnambiguousLegacyParameterTypes(String methodName) {
    if (methodName == null || !concreteMethodSignatures.containsKey(methodName)) {
      return null;
    }
    long sameNameRewriteCount =
        methodRewrites.values().stream().filter(key -> methodName.equals(key.getName())).count();
    if (sameNameRewriteCount > 1) {
      return null;
    }
    return concreteMethodSignatures.get(methodName);
  }

  private String ownerName(CtInvocation<?> invocation) {
    try {
      if (invocation.getExecutable() != null
          && invocation.getExecutable().getDeclaringType() != null
          && invocation.getExecutable().getDeclaringType().getSimpleName() != null) {
        return invocation.getExecutable().getDeclaringType().getSimpleName();
      }
    } catch (Exception ignored) {
      // Fall through to target metadata when declaring type metadata is absent.
    }
    try {
      if (invocation.getTarget() != null
          && invocation.getTarget().getType() != null
          && invocation.getTarget().getType().getSimpleName() != null) {
        return invocation.getTarget().getType().getSimpleName();
      }
    } catch (Exception ignored) {
      // Fall through to the surrounding type when target metadata is absent.
    }
    CtType<?> parentType = invocation.getParent(CtType.class);
    return parentType != null ? parentType.getSimpleName() : null;
  }

  private CtExpression<?> defaultExpression(String typeName) {
    if (typeName == null) {
      return getFactory().Code().createLiteral(null);
    }
    String normalized = typeName.trim();
      return switch (normalized) {
          case "boolean" -> getFactory().Code().createLiteral(false);
          case "byte" -> getFactory().Code().createLiteral((byte) 0);
          case "short" -> getFactory().Code().createLiteral((short) 0);
          case "int" -> getFactory().Code().createLiteral(0);
          case "long" -> getFactory().Code().createLiteral(0L);
          case "float" -> getFactory().Code().createLiteral(0.0f);
          case "double" -> getFactory().Code().createLiteral(0.0d);
          case "char" -> getFactory().Code().createLiteral('\0');
          default -> getFactory().Code().createLiteral(null);
      };
  }

  /**
   * Traverse the Spoon model and replace type references whose simple name
   * matches a concrete implementer with the configured grouping type simple name.
   * This method skips replacements inside the concrete implementer type declarations
   * themselves to avoid altering the concrete class/interface definitions.
   */
  private void applyGroupingToModel() {
    if (groupingMappings == null || groupingMappings.isEmpty()) return;
    try {
      Set<String> concreteNames = new HashSet<>(groupingMappings.keySet());

      // collect concrete declarations to avoid replacing inside their own declarations
      Set<CtType<?>> concreteDecls = spoonModel.getAllTypes().stream()
          .filter(t -> concreteNames.contains(t.getSimpleName()))
          .collect(Collectors.toSet());

      List<CtTypeReference<?>> refs = spoonModel.getElements(new TypeFilter<>(CtTypeReference.class));
      for (CtTypeReference<?> ref : refs) {
        try {
          String simple = ref.getSimpleName();
          if (simple == null) continue;
          if (!groupingMappings.containsKey(simple)) continue;

          // Skip replacements that occur inside the concrete type declaration itself
          CtType<?> owner = ref.getParent(CtType.class);
          if (owner != null) {
            if (concreteDecls.contains(owner)) continue;
            // Also skip generated Builder classes to preserve builder return types and signatures
            if (owner.getSimpleName() != null && owner.getSimpleName().endsWith("Builder")) continue;
          }

          String newName = groupingMappings.get(simple);
          if (newName != null && !newName.equals(simple)) {
            rewriteTypeReferenceName(ref, newName);
          }
        } catch (Exception ignored) {
          // Keep independent references from blocking each other.
        }
      }
    } catch (Exception e) {
      Log.warn("applyGroupingToModel failed: " + e.getMessage());
    }
  }

  @Override
  public void updateType(ASTTypeDeclaration source, String newName) {
    CtType<?> type = getSpoonType(source);
    Refactoring.changeTypeName(type, newName);
  }

  @Override
  public void updateMethod(
      ASTTypeDeclaration srcType, ASTMethodDeclaration srcMethod, String newName) {
    CtMethod<?> method = getSpoonMethod(srcType, srcMethod);
    Refactoring.changeMethodName(method, newName);
  }

  @Override
  public void updateField(
      ASTTypeDeclaration srcType, ASTFieldDeclaration srcField, String newName) {

    // get Spoon Variable
    String srcName = srcField.getVariableDeclarator(0).getDeclarator().getName();
    CtVariable<?> attribute = getSpoonType(srcType).getField(srcName);
    if (attribute == null) {
      Log.warn(
          "Cannot rename field '"
              + srcName
              + "' in "
              + srcType.getName()
              + " because no Spoon target exists; completed-member projection may add it later.");
      return;
    }

    // perform update
    CtRenameGenericVariableRefactoring refactor = new CtRenameGenericVariableRefactoring();
    refactor.setTarget(attribute).setNewName(newName).refactor();
  }

  @Override
  public void updateSuperType(ASTTypeDeclaration type, ASTMCType supertype, String newName) {
    String srcName = JavaLoader.print(supertype);
    CtType<?> spoonType = getSpoonType(type);

    // case super class
    CtTypeReference<?> superType = spoonType.getSuperclass();
    if (superType != null && superType.getSimpleName().equals(srcName)) {
      rewriteTypeReferenceName(spoonType.getSuperclass(), newName);
      return;
    }

    // case super interface
    for (CtTypeReference<?> superType2 : spoonType.getSuperInterfaces()) {
      if (superType2.getSimpleName().equals(srcName)) {
        rewriteTypeReferenceName(superType2, newName);
      }
    }
  }

  @Override
  public void updateLocalVariable(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTLocalVariableDeclaration sourceVar,
      String newName) {

    // get spoon local-variable
    CtMethod<?> spoonMethod = getSpoonMethod(srcType, srcMethod);
    List<CtLocalVariable<?>> localVars = spoonMethod.getElements(Objects::nonNull);
    String varName = sourceVar.getVariableDeclarator(0).getDeclarator().getName();
    Optional<CtLocalVariable<?>> var =
        localVars.stream().filter(v -> v.getSimpleName().equals(varName)).findAny();
    assert var.isPresent();

    // perform update
    CtRenameGenericVariableRefactoring refactor = new CtRenameGenericVariableRefactoring();
    refactor.setTarget(var.get()).setNewName(newName).refactor();
  }

  @Override
  public void updateMethodParameter(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTFormalParameter srcParam,
      String newName) {

    // get spoon formal parameter of method
    CtMethod<?> spoonMethod = getSpoonMethod(srcType, srcMethod);
    List<CtParameter<?>> params = spoonMethod.getElements(Objects::nonNull);
    String srcVarName = srcParam.getDeclarator().getName();
    Optional<CtParameter<?>> param =
        params.stream().filter(v -> v.getSimpleName().equals(srcVarName)).findAny();

    if (param.isPresent()) {
      // perform update
      CtRenameGenericVariableRefactoring refactor = new CtRenameGenericVariableRefactoring();
      refactor.setTarget(param.get()).setNewName(newName).refactor();

    } else {

      // case formal param in for loop
      List<CtLocalVariable<?>> localVars = spoonMethod.getElements(Objects::nonNull);
      Optional<CtLocalVariable<?>> localvar =
          localVars.stream().filter(v -> v.getSimpleName().equals(srcVarName)).findAny();
      assert localvar.isPresent();

      // case formal param in for loop
      CtRenameGenericVariableRefactoring refactor = new CtRenameGenericVariableRefactoring();
      refactor.setTarget(localvar.get()).setNewName(newName).refactor();
    }
  }

  @Override
  public void updateCDType(ASTCDType cdType, String newName) {
    List<CtTypeReference<?>> refTypes = spoonModel.getElements(Objects::nonNull);

    for (CtTypeReference<?> typeRef : refTypes) {
      if (typeRef.getSimpleName().equals(cdType.getName())) {
        rewriteTypeReferenceName(typeRef, newName);
      }
    }

    //  for ()
  }

  @Override
  public void setOutputDirectory(Path outputPath) {
    this.outputDir = outputPath.toFile();
  }

  @Override
  public void addField(ASTTypeDeclaration targetType, ASTFieldDeclaration templateField, String newName, String newType) {
    addField(targetType, templateField, newName, newType, false);
  }

  @Override
  public void addField(
      ASTTypeDeclaration targetType,
      ASTFieldDeclaration templateField,
      String newName,
      String newType,
      boolean isStatic) {
    // Clone the template field from Spoon model and add to the target type with new name and type.
    String templateFieldName = templateField.getVariableDeclarator(0).getDeclarator().getName();
    CtType<?> spoonType = getSpoonType(targetType);
    if (spoonType.getField(newName) != null) {
      return;
    }
    CtField<?> srcField = spoonType.getField(templateFieldName);
    CtField<?> clone =
        srcField != null
            ? srcField.clone()
            : getFactory().Core().createField();
    clone.setSimpleName(newName);
    clone.setType(createTypeReference(newType));
    if (isStatic) {
      clone.addModifier(ModifierKind.STATIC);
    } else {
      clone.removeModifier(ModifierKind.STATIC);
    }
    spoonType.addField(clone);
  }

  @Override
  public void addType(ASTTypeDeclaration templateType, String newName) {
    // Clone the template type and register the new type in the Spoon model
    CtType<?> src = getSpoonType(templateType);
    CtType<?> clone = src.clone();
    clone.setSimpleName(newName);

    // Add cloned type to the same package as the source
    if (src.getPackage() != null) {
      src.getPackage().addType(clone);
    } else {
      // Fallback: add to root package
      getFactory().Package().getRootPackage().addType(clone);
    }

    // Register mapping so subsequent lookups for this template type will return the newly created clone
    typeMap.put(templateType, clone);
  }

  @Override
  public void addMethod(ASTTypeDeclaration targetType,
                        ASTMethodDeclaration templateMethod,
                        String newName,
                        java.util.List<String> paramTypes,
                        java.util.List<String> paramNames,
                        String returnType) {
    addMethod(targetType, templateMethod, newName, paramTypes, paramNames, returnType, false);
  }

  @Override
  public void addMethod(
      ASTTypeDeclaration targetType,
      ASTMethodDeclaration templateMethod,
      String newName,
      java.util.List<String> paramTypes,
      java.util.List<String> paramNames,
      String returnType,
      boolean isStatic) {
    CtType<?> spoonType = getSpoonType(targetType);
    if (hasMethod(spoonType, newName, paramTypes)) {
      return;
    }
    CtMethod<?> clone =
        cloneConfiguredMethod(targetType, templateMethod, newName, paramTypes, paramNames, returnType);
    if (isStatic) {
      clone.addModifier(ModifierKind.STATIC);
    } else {
      clone.removeModifier(ModifierKind.STATIC);
    }
    spoonType.addMethod(clone);
  }

  @Override
  @Deprecated
  public void addMethod(ASTTypeDeclaration targetType,
                        ASTMethodDeclaration templateMethod,
                        String newName,
                        java.util.List<String> paramTypes,
                        java.util.List<String> paramNames,
                        String returnType,
                        String methodBody) {
    CtMethod<?> clone = cloneConfiguredMethod(targetType, templateMethod, newName, paramTypes, paramNames, returnType);
    if (methodBody != null && !methodBody.isEmpty()) {
      clone.setBody(getFactory().Core().createBlock());
      clone.getBody().addStatement(getFactory().Code().createCodeSnippetStatement(methodBody));
    }
    getSpoonType(targetType).addMethod(clone);
  }

  @Override
  public void addMethod(ASTTypeDeclaration targetType,
                        ASTMethodDeclaration templateMethod,
                        String newName,
                        java.util.List<String> paramTypes,
                        java.util.List<String> paramNames,
                        String returnType,
                        MethodBodySpec methodBody) {
    CtMethod<?> clone = cloneConfiguredMethod(targetType, templateMethod, newName, paramTypes, paramNames, returnType);
    applyMethodBodySpec(targetType, clone, methodBody);
    getSpoonType(targetType).addMethod(clone);
  }

  @Override
  public boolean requiresSignatureOnlyMethod(
      ASTTypeDeclaration targetType, ASTMethodDeclaration templateMethod) {
    CtType<?> spoonType = getSpoonType(targetType);
    if (spoonType.isInterface()) {
      return true;
    }
    CtMethod<?> template = getSpoonMethod(targetType, templateMethod);
    return template != null && template.hasModifier(ModifierKind.ABSTRACT);
  }

  private CtMethod<?> cloneConfiguredMethod(ASTTypeDeclaration targetType,
                                            ASTMethodDeclaration templateMethod,
                                            String newName,
                                            java.util.List<String> paramTypes,
                                            java.util.List<String> paramNames,
                                            String returnType) {
    CtType<?> spoonType = getSpoonType(targetType);
    CtMethod<?> srcMethod = getSpoonMethod(targetType, templateMethod);
    CtMethod<?> clone =
        srcMethod != null
            ? srcMethod.clone()
            : getFactory().Core().createMethod();
    clone.setSimpleName(newName);
    clone.getParameters().clear();
    for (int i = 0; i < paramNames.size(); i++) {
      CtParameter<?> param = getFactory().Core().createParameter();
      param.setSimpleName(paramNames.get(i));
      param.setType(createTypeReference(paramTypes.get(i)));
      clone.addParameter(param);
    }
    if (returnType != null && !returnType.isEmpty()) {
      clone.setType(createTypeReference(returnType));
    }
    if (requiresSignatureOnlyMethod(targetType, templateMethod)) {
      clone.setBody(null);
      if (!spoonType.isInterface()) {
        clone.addModifier(ModifierKind.ABSTRACT);
      }
    } else if (clone.getBody() == null && (returnType == null || !"void".equals(returnType))) {
      CtBlock<?> body = getFactory().Core().createBlock();
      CtReturn<?> returnStatement = getFactory().Core().createReturn();
      returnStatement.setReturnedExpression((CtExpression) defaultExpression(returnType));
      body.addStatement(returnStatement);
      clone.setBody(body);
    } else if (clone.getBody() == null) {
      clone.setBody(getFactory().Core().createBlock());
    }
    return clone;
  }

  private boolean hasMethod(CtType<?> type, String name, java.util.List<String> paramTypes) {
    for (CtMethod<?> method : type.getMethods()) {
      if (!name.equals(method.getSimpleName())) {
        continue;
      }
      if (method.getParameters().size() != paramTypes.size()) {
        continue;
      }
      boolean sameParameters = true;
      for (int i = 0; i < paramTypes.size(); i++) {
        String actual = method.getParameters().get(i).getType().getSimpleName();
        String expected = JavaSourceNames.simpleName(paramTypes.get(i));
        if (!actual.equals(expected)) {
          sameParameters = false;
          break;
        }
      }
      if (sameParameters) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void applyMethodBodySpec(ASTTypeDeclaration targetType, CtMethod<?> clone, MethodBodySpec methodBody) {
    if (methodBody == null || methodBody.kind() == MethodBodySpec.Kind.EMPTY) {
      return;
    }
    CtBlock<?> body = getFactory().Core().createBlock();
    clone.setBody(body);
    switch (methodBody.kind()) {
      case ASSIGN_FIELD_AND_RETURN_THIS:
        CtAssignment assignment = getFactory().Core().createAssignment();
        assignment.setAssigned(
            (CtExpression) getFactory().Code().createCodeSnippetExpression("this." + methodBody.fieldName()));
        CtParameter<?> parameter =
            clone.getParameters().stream()
                .filter(param -> param.getSimpleName().equals(methodBody.parameterName()))
                .findFirst()
                .orElseGet(() -> clone.getParameters().isEmpty() ? null : clone.getParameters().get(0));
        if (parameter != null) {
          CtVariableReference<?> varRef = parameter.getReference();
          assignment.setAssignment((CtExpression) getFactory().Code().createVariableRead(varRef, false));
          body.addStatement(assignment);
        }
        CtReturn setterReturn = getFactory().Core().createReturn();
        setterReturn.setReturnedExpression((CtExpression) createThisAccess(targetType));
        body.addStatement(setterReturn);
        break;
      case RETURN_NEW:
        CtConstructorCall<?> ctor = getFactory().Core().createConstructorCall();
        ctor.setType(createTypeReference(methodBody.constructorType()));
        for (String fieldArgument : methodBody.constructorFieldArguments()) {
          ctor.addArgument(getFactory().Code().createCodeSnippetExpression("this." + fieldArgument));
        }
        CtReturn buildReturn = getFactory().Core().createReturn();
        buildReturn.setReturnedExpression((CtExpression) ctor);
        body.addStatement(buildReturn);
        break;
      default:
        break;
    }
  }

  private CtThisAccess<?> createThisAccess(ASTTypeDeclaration targetType) {
    CtType<?> spoonTypeForThis = getSpoonType(targetType);
    CtThisAccess<?> thisAccess = getFactory().Core().createThisAccess();
    CtTypeReference<?> thisTypeRef = getFactory().Type().createReference(spoonTypeForThis.getSimpleName());
    try {
      CtTypeAccess<?> typeAccess = getFactory().Code().createTypeAccess(thisTypeRef);
      thisAccess.setTarget(typeAccess);
    } catch (Exception e) {
      thisAccess.setType(thisTypeRef);
    }
    return thisAccess;
  }

  @Override
  public void removeField(ASTTypeDeclaration targetType, ASTFieldDeclaration field) {
    String name = field.getVariableDeclarator(0).getDeclarator().getName();
    CtType<?> spoonType = getSpoonType(targetType);
    CtField<?> f = (CtField<?>) spoonType.getField(name);
    if (f != null) {
      f.delete();
    }
  }

  @Override
  public void removeMethod(ASTTypeDeclaration targetType, ASTMethodDeclaration method) {
    CtMethod<?> m = getSpoonMethod(targetType, method);
    if (m != null) {
      m.delete();
    }
  }

  /**
   * Helper to create a CtTypeReference for a given fully-qualified or simple type name.
   */
  private CtTypeReference<?> createTypeReference(String typeName) {
    if (typeName == null || typeName.isEmpty()) {
      return getFactory().Type().createReference(Object.class);
    }
    Optional<CtTypeReference<?>> originalParsed = parseTypeReference(typeName.trim());
    if (originalParsed.isPresent()) {
      return originalParsed.get();
    }
    String normalized = JavaSourceNames.normalizeType(typeName);
    CtTypeReference<?> primitiveReference = primitiveTypeReference(normalized);
    if (primitiveReference != null) {
      return primitiveReference;
    }
    Optional<CtTypeReference<?>> parsed = parseTypeReference(normalized);
    if (parsed.isPresent()) {
      return parsed.get();
    }
    try {
      // Try to create reference by name
      return getFactory().Type().createReference(normalized);
    } catch (Exception e) {
      // Fallback for primitive types
        return switch (normalized) {
            case "int" -> getFactory().Type().createReference(int.class);
            case "long" -> getFactory().Type().createReference(long.class);
            case "double" -> getFactory().Type().createReference(double.class);
            case "float" -> getFactory().Type().createReference(float.class);
            case "boolean" -> getFactory().Type().createReference(boolean.class);
            case "char" -> getFactory().Type().createReference(char.class);
            case "short" -> getFactory().Type().createReference(short.class);
            case "byte" -> getFactory().Type().createReference(byte.class);
            default ->
                // TODO: Maybe a warning would be better and just null return here?
                // As a last resort, try java.lang.
                    getFactory().Type().createReference("java.lang." + normalized);
        };
    }
  }

  private CtTypeReference<?> primitiveTypeReference(String typeName) {
    return switch (typeName) {
      case "void" -> getFactory().Type().createReference(void.class);
      case "int" -> getFactory().Type().createReference(int.class);
      case "long" -> getFactory().Type().createReference(long.class);
      case "double" -> getFactory().Type().createReference(double.class);
      case "float" -> getFactory().Type().createReference(float.class);
      case "boolean" -> getFactory().Type().createReference(boolean.class);
      case "char" -> getFactory().Type().createReference(char.class);
      case "short" -> getFactory().Type().createReference(short.class);
      case "byte" -> getFactory().Type().createReference(byte.class);
      default -> null;
    };
  }

  private Optional<CtTypeReference<?>> parseTypeReference(String typeName) {
    try {
      Launcher parser = new Launcher();
      parser.getEnvironment().setNoClasspath(true);
      parser.addInputResource(
          new VirtualFile("class __TypeProbe { " + typeName + " value; }", "__TypeProbe.java"));
      parser.buildModel();
      return parser.getModel().getElements(new TypeFilter<>(CtField.class)).stream()
          .findFirst()
          .map(field -> field.getType().clone());
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  private void rewriteTypeReferenceName(CtTypeReference<?> reference, String newName) {
    reference.setSimpleName(newName);
    reference.setPackage(null);
    reference.setDeclaringType(null);
    reference.setSimplyQualified(true);
  }

  private spoon.reflect.factory.Factory getFactory() {
    return launcher.getFactory();
  }

  /***
   * Retrieves the spoonType from the Spoon Model based on the provided mcType.
   * Saves the found spoonType in the type map.
   *
   * @param mcType The ASTTypeDeclaration representing the type to be searched
   *               for in the Spoon model.
   * @return The corresponding CtType<?> found in the Spoon model for the
   *         given mcType.
   * @throws AssertionError if no matching CtType<?> is found in the Spoon
   *         model (assert will fail).
   */
  private CtType<?> getSpoonType(ASTTypeDeclaration mcType) {
    // cas already found
    if (typeMap.containsKey(mcType)) {
      return typeMap.get(mcType);
    }
    // search in the spoon model
    Optional<CtType<?>> type =
        spoonModel.getAllTypes().stream().filter(t -> compare(mcType, t)).findFirst();
    assert type.isPresent();
    typeMap.put(mcType, type.get());
    return type.get();
  }

  /***
   * Retrieves the spoonMethod from the Spoon Model based on the provided
   * mcType and mcMethod. Saves the found spoonMethod in the method map.
   *
   * @param mcType The ASTTypeDeclaration representing the type to which
   *               the method belongs.
   * @param mcMethod The ASTMethodDeclaration representing the method to be
   *                 searched for in the Spoon model.
   * @return The corresponding CtMethod found in the Spoon model for the
   *         given mcType and mcMethod.
   * @throws AssertionError if no matching CtMethod is found in the
   *                        Spoon model (assert will fail).
   */
  public CtMethod<?> getSpoonMethod(ASTTypeDeclaration mcType, ASTMethodDeclaration mcMethod) {
    // cas method was already found
    if (methodMap.containsKey(mcMethod)) {
      return methodMap.get(mcMethod);
    }

    // search method in the spoonType
    CtType<?> spoonType = getSpoonType(mcType);
    Optional<CtMethod<?>> method =
        spoonType.getAllMethods().stream()
            .filter(spMethod -> compare(mcMethod, spMethod))
            .findFirst();

    assert method.isPresent();
    methodMap.put(mcMethod, method.get());
    return method.get();
  }

  /**
   * Compares a mcType and spoonType and returns true if both are identical.
   *
   * @param type The ASTTypeDeclaration representing the type to be compared.
   * @param spoonType The CtType representing the spoon type to be compared.
   * @return True if the file name of the mcType ends with the simple name of the spoonType followed
   *     by ".java", or if the type names match; otherwise false.
   */
  protected boolean compare(ASTTypeDeclaration type, CtType<?> spoonType) {
    String fileName = type.get_SourcePositionStart().getFileName().orElse(type.getName());
    String mcName = type.getName();
    String spoonName = spoonType.getSimpleName();

    String normalizedFileName = fileName.replace('\\', '/');
    int lastSlash = normalizedFileName.lastIndexOf('/');
    String leafFileName =
        lastSlash >= 0 ? normalizedFileName.substring(lastSlash + 1) : normalizedFileName;
    if ((spoonName + ".java").equals(leafFileName)) {
      return true;
    }
    // Fallback: compare by type name (for multi-incarnation temp directory scenarios)
    return mcName.equals(spoonName);
  }

  /**
   * Compares a spoonMethod and mcMethod and returns true if both are identical.
   *
   * @param mcMethod The ASTMethodDeclaration representing the method to be compared.
   * @param spoonMethod The CtMethod representing the spoon method to be compared.
   * @return True if the names, parameter count, and parameter types of both methods match;
   *     otherwise false.
   */
  protected boolean compare(ASTMethodDeclaration mcMethod, CtMethod<?> spoonMethod) {
    // compare names
    if (!mcMethod.getName().endsWith(spoonMethod.getSimpleName())) {
      return false;
    }
    // is present parameters ?
    if (!mcMethod.getFormalParameters().isPresentFormalParameterListing()) {
      return spoonMethod.getParameters().isEmpty();
    }
    // same number of parameters ?
    List<ASTFormalParameter> mcParams =
        mcMethod.getFormalParameters().getFormalParameterListing().getFormalParameterList();
    if (spoonMethod.getParameters().size() != mcParams.size()) {
      return false;
    }
    // parameters have the same type ?
    for (int i = 0; i < spoonMethod.getParameters().size(); i++) {
      if (!(spoonMethod.getParameters().get(i).getType().getSimpleName())
          .equals(print(mcParams.get(i).getMCType()))) {
        return false;
      }
    }

    return true;
  }
}
