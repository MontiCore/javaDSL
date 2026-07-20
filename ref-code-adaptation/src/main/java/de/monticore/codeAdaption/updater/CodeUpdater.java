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

/**
 * Technology-neutral mutation facade used to transform one loaded handwritten Java model.
 *
 * <p>Implementations must update declarations and their uses consistently. Default methods fail
 * explicitly when an implementation does not support a generation or cleanup capability.
 */
public interface CodeUpdater {
  /**
   * Loads the Java source tree that subsequent operations mutate.
   *
   * @param path source directory
   */
  void setCodePath(Path path);

  /**
   * Prints the current model into the configured output directory.
   *
   * @return generated Java files
   */
  Set<File> printCode();

  /**
   * Renames a type declaration and all resolvable references and uses.
   *
   * @param srcType the source type to replace.
   * @param newName the new name of the type.
   */
  void updateType(ASTTypeDeclaration srcType, String newName);

  /**
   * Renames a method declaration and the invocations resolved to that method.
   *
   * @param srcType the source-type  that contains the method-declaration.
   * @param srcMethod the source method.
   * @param newName the new name of the method
   */
  void updateMethod(ASTTypeDeclaration srcType, ASTMethodDeclaration srcMethod, String newName);

  /**
   * Renames a field declaration and all resolvable accesses.
   *
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

  /**
   * Renames a local variable declaration and all resolvable uses in its method.
   *
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

  /**
   * Renames a method parameter declaration and all resolvable uses.
   *
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

  /**
   * Rewrites references to a CD type that has no Java declaration in the handwritten model.
   *
   * @param cdType type  in the reference class diagram to update in the reference code.
   * @param newName new name of the type.
   */
  void updateCDType(ASTCDType cdType, String newName);
  /**
   * Sets the destination used by {@link #printCode()}.
   *
   * @param outputPath the output directory
   */
  void setOutputDirectory(Path outputPath);

  /**
   * Renames one declared superclass or interface reference on a Java type.
   *
   * @param type Java type that owns the relationship
   * @param supertype source supertype AST
   * @param newName concrete supertype name
   */
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

  /** Sets whether a generated or adapted Java type is abstract. */
  default void setTypeAbstract(ASTTypeDeclaration targetType, boolean isAbstract) {
    throw unsupported("setTypeAbstract");
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

  /**
   * Immutable description of the small generated method-body forms supported by the updater.
   *
   * <p>For example, {@code assignFieldAndReturnThis("name", "value")} represents {@code this.name =
   * value; return this;}, while {@code returnNew("Person", List.of("name", "age"))} represents
   * {@code return new Person(name, age);}.
   */
  final class MethodBodySpec {
    /** Supported generated method-body shapes. */
    public enum Kind {
      /** Preserve a cloned body or synthesize a safe empty/default body. */
      EMPTY,
      /** Replace a cloned implementation with a compilable default body. */
      SAFE_DEFAULT,
      /** Preserve an implementation or synthesize a public, safe interface-contract body. */
      INTERFACE_CONTRACT,
      /** Assign one parameter to a field and return {@code this}. */
      ASSIGN_FIELD_AND_RETURN_THIS,
      /** Return a newly constructed value using selected fields as arguments. */
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

    /** Returns a specification that requests no structured replacement body. */
    public static MethodBodySpec empty() {
      return new MethodBodySpec(Kind.EMPTY, null, null, null, java.util.List.of());
    }

    /** Returns a specification that discards a cloned body and uses Java default values. */
    public static MethodBodySpec safeDefault() {
      return new MethodBodySpec(Kind.SAFE_DEFAULT, null, null, null, java.util.List.of());
    }

    /** Returns a specification for a concrete implementation of an interface contract. */
    public static MethodBodySpec interfaceContract() {
      return new MethodBodySpec(
          Kind.INTERFACE_CONTRACT, null, null, null, java.util.List.of());
    }

    /** Returns a builder-style field-assignment specification. */
    public static MethodBodySpec assignFieldAndReturnThis(String fieldName, String parameterName) {
      return new MethodBodySpec(
          Kind.ASSIGN_FIELD_AND_RETURN_THIS, fieldName, parameterName, null, java.util.List.of());
    }

    /** Returns a specification that constructs and returns the requested type. */
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
