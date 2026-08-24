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
   * @param srcType handwritten Java declaration to rename
   * @param newName concrete Java type name
   */
  void updateType(ASTTypeDeclaration srcType, String newName);

  /**
   * Renames a method declaration and the invocations resolved to that method.
   *
   * @param srcType handwritten Java type containing the method declaration
   * @param srcMethod handwritten Java method to rename
   * @param newName the new name of the method
   */
  void updateMethod(ASTTypeDeclaration srcType, ASTMethodDeclaration srcMethod, String newName);

  /**
   * Renames a field declaration and all resolvable accesses.
   *
   * @param srcType handwritten Java type containing the field declaration
   * @param srcField handwritten Java field to rename
   * @param newName concrete field name
   */
  void updateField(ASTTypeDeclaration srcType, ASTFieldDeclaration srcField, String newName);

  /**
   * Renames accesses to a field generated from a CD association role.
   *
   * <p>Unlike {@link #updateField}, this operation has no handwritten field declaration to rename:
   * the declaration will be generated from the concrete CD. The source type scopes the rewrite so
   * a role such as {@code children} on one owner does not affect another owner's same-named role.
   */
  default void updateAssociationRole(
      ASTTypeDeclaration srcType, String sourceRole, String concreteRole) {
    throw unsupported("updateAssociationRole");
  }

  /**
   * Renames a local variable declaration and all resolvable uses in its method.
   *
   * @param srcType handwritten Java type containing the local variable
   * @param srcMethod handwritten Java method containing the local variable
   * @param sourceVar local variable to rename
   * @param newName concrete local-variable name
   */
  void updateLocalVariable(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTLocalVariableDeclaration sourceVar,
      String newName);

  /**
   * Renames a method parameter declaration and all resolvable uses.
   *
   * @param srcType handwritten Java type containing the parameter
   * @param srcMethod handwritten Java method containing the parameter
   * @param srcParam formal parameter to rename
   * @param newName concrete parameter name
   */
  void updateMethodParameter(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTFormalParameter srcParam,
      String newName);

  /**
   * Rewrites uses of a reference-CD type that has no declaration in handwritten Java.
   *
   * <p>For example, handwritten code may declare {@code List<Role>} without declaring {@code Role}
   * because that type is generated from the CD. If {@code Role} maps to {@code Permission}, this
   * operation rewrites the type use even though {@link #updateType} has no Java declaration to
   * target.
   *
   * @param cdType reference-CD type whose handwritten Java uses are rewritten
   * @param newName concrete Java type name
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
   * Cleans generated Java and repairs explicit self values moved into TOP implementations.
   *
   * @param topToPublicSelfTypes qualified generated TOP type to its qualified public HWC subtype
   */
  default void cleanCode(Path codePath, Map<String, String> topToPublicSelfTypes) {
    if (topToPublicSelfTypes == null || topToPublicSelfTypes.isEmpty()) {
      cleanCode(codePath);
      return;
    }
    throw unsupported("cleanCode with TOP self-type bindings");
  }

  /**
   * Configures incarnation-type to grouping-type replacements for the current updater pass.
   *
   * <p>Keys identify individual concrete incarnations and values identify the common concrete type
   * representing their incarnation group. For example, {@code {CreditCard=PaymentMethod,
   * Invoice=PaymentMethod}} allows a shared {@code CreditCard} or {@code Invoice} parameter to be
   * printed as {@code PaymentMethod}. Implementations apply these replacements during final model
   * repair; they do not rename the incarnation declarations themselves.
   *
   * @param mappings incarnation simple name to grouping-type simple name; empty disables grouping
   */
  default void setGroupingMappings(Map<String, String> mappings) {
    if (mappings != null && !mappings.isEmpty()) {
      throw unsupported("setGroupingMappings");
    }
  }

  /**
   * Registers an owner-independent concrete method signature for legacy invocation repair.
   *
   * <p>For example, registering {@code send(String, boolean)} allows an adapted {@code send(label)}
   * invocation to receive a compatible enclosing {@code boolean} parameter or a Java default value.
   * Because no owner is supplied, repair is performed only when the method name has one
   * unambiguous registered signature. New code should prefer {@link #registerMethodRewrite}.
   *
   * @param methodName concrete method name
   * @param parameterTypes concrete parameter types in declaration order
   */
  default void registerConcreteMethodSignature(String methodName, List<String> parameterTypes) {
    throw unsupported("registerConcreteMethodSignature");
  }

  /**
   * Registers an owner- and signature-aware method rewrite for invocation repair.
   *
   * <p>The reference key identifies the method before adaptation; the concrete key identifies its
   * renamed owner, name, and parameters afterward. For example, {@code Port.send(String) ->
   * ShippingPort.ship(String, boolean)} both renames matching calls and supplies the additional
   * argument without changing unrelated {@code send} overloads.
   *
   * @param referenceMethod owner- and signature-aware reference-CD method key
   * @param concreteMethod owner- and signature-aware concrete-CD method key
   */
  default void registerMethodRewrite(StableElementKey referenceMethod, StableElementKey concreteMethod) {
    throw unsupported("registerMethodRewrite");
  }

  /**
   * Registers a method rewrite anchored to the exact Java source owner. Implementations should use
   * the source declaration to retain package identity even when the CD owner is unqualified.
   */
  default void registerMethodRewrite(
      ASTTypeDeclaration sourceOwner,
      StableElementKey referenceMethod,
      StableElementKey concreteMethod) {
    registerMethodRewrite(referenceMethod, concreteMethod);
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
