package de.monticore.codeAdaption.updater;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * this class execute the update operation in the reference code.
 */
public interface CodeUpdater {
  /***
   * set the path to the referenceCode.
   * @param path the path.
   */
  void setCodePath(Path path);

  /***
   * print the current state of the reference code as set of files.
   * @return the code as files
   */
  Set<File> printCode();

  /***
   * change the name of a type in the reference code. By the declaration
   * an all its reference an uses.
   * @param srcType the source type to replace.
   * @param newName the new name of the type.
   */
  void updateType(ASTTypeDeclaration srcType, String newName);

  /***
   * change the name of a method in the reference code. By the declaration
   * an all its reference an uses.
   * @param srcType the source-type  that contains the method-declaration.
   * @param srcMethod the source method.
   * @param newName the new name of the type.
   */
  void updateMethod(ASTTypeDeclaration srcType, ASTMethodDeclaration srcMethod, String newName);

  /***
   * change the name of a field in the reference code. By its declaration
   * an all its references an uses.
   * @param srcType the source-type that contains the field-declaration
   * @param srcField the source Field.
   * @param newName the new name of the field.
   */
  void updateField(ASTTypeDeclaration srcType, ASTFieldDeclaration srcField, String newName);

  /**
   * Change accesses to a field generated from an association role. Association fields do not have
   * declarations in handwritten reference code, so they cannot be handled by {@link #updateField}.
   * The source type scopes the rewrite and prevents changes to same-named roles on other owners.
   */
  default void updateAssociationRole(
      ASTTypeDeclaration srcType, String sourceRole, String concreteRole) {
    throw unsupported("updateAssociationRole");
  }

  /***
   * change the name of a local-variable in the reference code. By its declaration
   * an all its references an uses.
   * @param srcType the source-type that contains the local variable.
   * @param srcMethod the source method tha contains le local variable.
   * @param sourceVar the source local variable.
   * @param newName the new name of the local variable.
   */
  void updateLocalVariable(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTLocalVariableDeclaration sourceVar,
      String newName);

  /***
   * change the name of a local-variable in the reference code. By it declaration
   * and all its references an uses
   * @param srcType the source type that contains the local parameter
   * @param srcMethod the source method that contains le the parameter
   * @param srcParam the source parameter
   * @param newName the new name of the parameter
   */
  void updateMethodParameter(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTFormalParameter srcParam,
      String newName);

  /***
   * Update all types that are not present in the reference code.
   * @param cdType type  in the reference class diagram to update in the reference code.
   * @param newName new name of the type.
   */
  void updateCDType(ASTCDType cdType, String newName);
  /***
   * set the output directory of the code by printing.
   * @param outputPath the output directory
   */
  void setOutputDirectory(Path outputPath);

  void updateSuperType(ASTTypeDeclaration type, ASTMCType supertype, String newName);

  /**
   * Remove adapter-only metadata and perform implementation-specific post-processing on generated
   * Java sources.
   */
  default void cleanCode(Path codePath) {
    throw unsupported("cleanCode");
  }

  /**
   * Provide a mapping of concrete-type-simple-name -> grouping-type-simple-name
   * so that updaters that operate on an AST (e.g. SpoonUpdater) can apply
   * grouping replacements before pretty-printing.
   * @param mappings mapping from concrete simple name to grouping simple name
   */
  default void setGroupingMappings(Map<String, String> mappings) {
    if (mappings != null && !mappings.isEmpty()) {
      throw unsupported("setGroupingMappings");
    }
  }

  /**
   * Register a concrete method signature so updaters can fix invocations that were
   * renamed from a reference method with fewer arguments.
   */
  default void registerConcreteMethodSignature(String methodName, List<String> parameterTypes) {
    throw unsupported("registerConcreteMethodSignature");
  }

  /**
   * Register an owner/signature-aware method rewrite. The reference key describes the source
   * method before adaptation; the concrete key describes the target method after adaptation.
   */
  default void registerMethodRewrite(StableElementKey referenceMethod, StableElementKey concreteMethod) {
    throw unsupported("registerMethodRewrite");
  }

  /**
   * Add or complete a field on the target type. Implementations may clone the template field when
   * present; completed-CD projection passes {@code null} when no Java template owns the member.
   */
  default void addField(
      ASTTypeDeclaration targetType,
      ASTFieldDeclaration templateField,
      String newName,
      String newType,
      boolean isStatic) {
    throw unsupported("addField");
  }

  /**
   * Add or complete a method on the target type. Implementations may clone the template method when
   * present; completed-CD projection passes {@code null} when no Java template owns the member.
   */
  default void addMethod(
      ASTTypeDeclaration targetType,
      ASTMethodDeclaration templateMethod,
      String newName,
      List<String> paramTypes,
      List<String> paramNames,
      String returnType,
      boolean isStatic,
      MethodBodySpec methodBody) {
    throw unsupported("addMethod");
  }

  /**
   * Create a new top-level type by cloning the provided template type and giving it the provided name.
   */
  default void addType(ASTTypeDeclaration templateType, String newName) {
    throw unsupported("addType");
  }

  /** Adds a completed-CD superclass or interface relationship to an existing Java type. */
  default void addSuperType(
      ASTTypeDeclaration targetType, String superTypeName, boolean interfaceType) {
    throw unsupported("addSuperType");
  }

  /** Adds an enum constant introduced by completed-CD projection. */
  default void addEnumConstant(
      ASTTypeDeclaration targetType, String constantName, int expectedIndex) {
    throw unsupported("addEnumConstant");
  }

  /**
   * Remove a field from the given target type (by template AST field reference).
   */
  default void removeField(ASTTypeDeclaration targetType, ASTFieldDeclaration field) {
    throw unsupported("removeField");
  }

  /**
   * Remove a method from the given target type (by template AST method reference).
   */
  default void removeMethod(ASTTypeDeclaration targetType, ASTMethodDeclaration method) {
    throw unsupported("removeMethod");
  }

  private UnsupportedOperationException unsupported(String operation) {
    return new UnsupportedOperationException(
        getClass().getName() + " does not support updater operation: " + operation);
  }

  final class MethodBodySpec {
    public enum Kind {
      EMPTY,
      ASSIGN_FIELD_AND_RETURN_THIS,
      RETURN_NEW
    }

    private final Kind kind;
    private final String fieldName;
    private final String parameterName;
    private final String constructorType;
    private final java.util.List<String> constructorFieldArguments;

    private MethodBodySpec(
        Kind kind,
        String fieldName,
        String parameterName,
        String constructorType,
        List<String> constructorFieldArguments) {
      this.kind = kind;
      this.fieldName = fieldName;
      this.parameterName = parameterName;
      this.constructorType = constructorType;
      this.constructorFieldArguments =
          constructorFieldArguments == null
              ? java.util.List.of()
              : java.util.List.copyOf(constructorFieldArguments);
    }

    public static MethodBodySpec empty() {
      return new MethodBodySpec(Kind.EMPTY, null, null, null, java.util.List.of());
    }

    public static MethodBodySpec assignFieldAndReturnThis(String fieldName, String parameterName) {
      return new MethodBodySpec(
          Kind.ASSIGN_FIELD_AND_RETURN_THIS, fieldName, parameterName, null, java.util.List.of());
    }

    public static MethodBodySpec returnNew(
        String constructorType, java.util.List<String> fieldArguments) {
      return new MethodBodySpec(Kind.RETURN_NEW, null, null, constructorType, fieldArguments);
    }

    public Kind kind() {
      return kind;
    }

    public String fieldName() {
      return fieldName;
    }

    public String parameterName() {
      return parameterName;
    }

    public String constructorType() {
      return constructorType;
    }

    public List<String> constructorFieldArguments() {
      return constructorFieldArguments;
    }
  }
}
