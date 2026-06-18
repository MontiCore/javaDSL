package de.monticore.codeAdaption.updater.spoonUpdater;

import static de.monticore.codeAdaption.utils.JavaLoader.print;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.utils.Constants;
import de.monticore.codeAdaption.utils.JavaLoader;
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
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

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
      Map<String, Path> originalFilesByName = javaFilesBySimpleName(codePath);

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

      try (var paths = Files.walk(formattedPath)) {
        paths
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(
                p -> {
                  try {
                    Path target =
                        originalFilesByName.getOrDefault(
                            p.getFileName().toString(), codePath.resolve(formattedPath.relativize(p)));
                    Files.createDirectories(target.getParent());
                    copyReplacingWithRetry(p, target);
                  } catch (IOException e) {
                    throw new IllegalStateException(
                        "Failed to copy formatted file '" + p + "' to output", e);
                  }
                });
      }

      JavaSourcePostProcessor.processDirectory(codePath);
      Log.info("SpoonUpdater.cleanCode: Cleanup completed", "CodeAdapter");
    } catch (IOException e) {
      Log.error("SpoonUpdater.cleanCode: Failed to clean code: " + e.getMessage());
    } finally {
      FileUtils.deleteQuietly(formattedPath.toFile());
    }
  }

  private static Map<String, Path> javaFilesBySimpleName(Path codePath) throws IOException {
    Map<String, Path> result = new LinkedHashMap<>();
    try (var paths = Files.walk(codePath)) {
      paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> result.putIfAbsent(path.getFileName().toString(), path));
    }
    return result;
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
            ref.setSimpleName(newName);
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
      spoonType.getSuperclass().setSimpleName(newName);
      return;
    }

    // case super interface
    for (CtTypeReference<?> superType2 : spoonType.getSuperInterfaces()) {
      if (superType2.getSimpleName().equals(srcName)) {
        superType2.setSimpleName(newName);
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
        typeRef.setSimpleName(newName);
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
    // Clone the template field from Spoon model and add to the target type with new name and type.
    String templateFieldName = templateField.getVariableDeclarator(0).getDeclarator().getName();
    CtType<?> spoonType = getSpoonType(targetType);
    CtField<?> srcField = spoonType.getField(templateFieldName);
    assert srcField != null;
    CtField<?> clone = srcField.clone();
    clone.setSimpleName(newName);
    clone.setType(createTypeReference(newType));
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
    getSpoonType(targetType)
        .addMethod(cloneConfiguredMethod(targetType, templateMethod, newName, paramTypes, paramNames, returnType));
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

  private CtMethod<?> cloneConfiguredMethod(ASTTypeDeclaration targetType,
                                            ASTMethodDeclaration templateMethod,
                                            String newName,
                                            java.util.List<String> paramTypes,
                                            java.util.List<String> paramNames,
                                            String returnType) {
    CtMethod<?> srcMethod = getSpoonMethod(targetType, templateMethod);
    assert srcMethod != null;
    CtMethod<?> clone = srcMethod.clone();
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
    return clone;
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
    try {
      // Try to create reference by name
      return getFactory().Type().createReference(typeName);
    } catch (Exception e) {
      // Fallback for primitive types
        return switch (typeName) {
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
                    getFactory().Type().createReference("java.lang." + typeName);
        };
    }
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
