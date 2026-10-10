package de.monticore.java.javadsl._symboltable;

import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTAnnotation;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.statements.mcarraystatements._ast.ASTArrayDeclaratorId;
import de.monticore.statements.mccommonstatements._ast.*;
import de.monticore.statements.mcstatementsbasis._ast.ASTMCModifier;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfObject;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaDSLScopesGenitorP2 implements JavaDSLVisitor2, JavaLightVisitor2 {
  
  @Override
  public void endVisit(ASTEnumDeclaration node) {
    TypeDeclarationSymbol symbol = node.getSymbol();
    
    List<SymTypeExpression> supertypes =
        node.getImplementedInterfaceList().stream().map(TypeCheck3::symTypeFromAST)
            .collect(Collectors.toList());
    symbol.setSuperTypesList(supertypes);
  }
  
  @Override
  public void endVisit(ASTRecordDeclaration node) {
    TypeDeclarationSymbol symbol = node.getSymbol();
    
    List<SymTypeExpression> supertypes =
        node.getImplementedInterfaceList().stream().map(TypeCheck3::symTypeFromAST)
            .collect(Collectors.toList());
    symbol.setSuperTypesList(supertypes);
    
    List<ASTRecordComponent> components = node.getRecordHeader().getRecordComponentList();
    addImplicitAccessors(symbol, components);
    addImplicitCanonicalConstructor(node, components);
    addImplicitObjectMethods(symbol);
  }
  
  /** Record components are private final fields (JLS 8.10.3). */
  @Override
  public void endVisit(ASTRecordComponent node) {
    FieldSymbol symbol = node.getSymbol();
    SymTypeExpression type = TypeCheck3.symTypeFromAST(node.getMCType());
    if (node.isHasVariableArity()) {
      type = SymTypeExpressionFactory.createTypeArray(type, 1);
    }
    symbol.setType(type);
    symbol.setIsPrivate(true);
    symbol.setIsFinal(true);
  }
  
  /**
   * Each record component has a public accessor without parameters, unless it is declared
   * explicitly (JLS 8.10.3).
   */
  protected void addImplicitAccessors(TypeDeclarationSymbol record,
      List<ASTRecordComponent> components) {
    for (ASTRecordComponent component : components) {
      if (!declaresMethod(record, component.getName(), List.of())) {
        ImplicitMembers.addMethod(record, component.getName(), component.getSymbol().getType())
            .setIsPublic(true);
      }
    }
  }
  
  /**
   * A record has a canonical constructor whose parameters are the record components. It is
   * implicitly declared with the access modifier of the record, if it is not declared explicitly;
   * a compact constructor declares it with its own access modifier (JLS 8.10.4).
   */
  protected void addImplicitCanonicalConstructor(ASTRecordDeclaration node,
      List<ASTRecordComponent> components) {
    TypeDeclarationSymbol record = node.getSymbol();
    List<String> componentTypes = components.stream()
        .map(c -> getParameterType(c).printFullName()).toList();
    boolean declared = getDeclaredMethods(record).stream()
        .filter(JavaMethodSymbol::isIsConstructor)
        .anyMatch(c -> c.getParameterList().stream()
            .map(p -> p.getType().printFullName()).toList().equals(componentTypes));
    if (declared) {
      return;
    }
    
    JavaMethodSymbol constructor = ImplicitMembers.addConstructor(record);
    List<ASTCompactConstructorDeclaration> compact =
        node.getRecordBody().getCompactConstructorDeclarationList();
    if (compact.isEmpty()) {
      constructor.setIsPublic(record.isIsPublic());
      constructor.setIsProtected(record.isIsProtected());
      constructor.setIsPrivate(record.isIsPrivate());
    }
    else {
      for (ASTJavaModifier modifier : compact.get(0).getJavaModifierList()) {
        switch (modifier) {
          case ASTModifierPublic m -> constructor.setIsPublic(true);
          case ASTModifierProtected m -> constructor.setIsProtected(true);
          case ASTModifierPrivate m -> constructor.setIsPrivate(true);
          default -> { } // ignore
        }
      }
    }
    for (ASTRecordComponent component : components) {
      ImplicitMembers.addParameter(constructor, component.getName(), getParameterType(component));
    }
    if (!components.isEmpty() && components.get(components.size() - 1).isHasVariableArity()) {
      constructor.setIsElliptic(true);
    }
  }
  
  /**
   * A record has the public methods {@code equals(Object)}, {@code hashCode()}, and
   * {@code toString()}, unless they are declared explicitly (JLS 8.10.3).
   */
  protected void addImplicitObjectMethods(TypeDeclarationSymbol record) {
    IJavaDSLScope scope = record.getSpannedScope();
    SymTypeExpression object =
        SymTypeExpressionFactory.createTypeObjectViaSurrogate("java.lang.Object", scope);
    SymTypeExpression string =
        SymTypeExpressionFactory.createTypeObjectViaSurrogate("java.lang.String", scope);
    if (!declaresMethod(record, "equals", List.of(object))) {
      JavaMethodSymbol equals = ImplicitMembers.addMethod(record, "equals",
          SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN));
      ImplicitMembers.addParameter(equals, "obj", object);
      equals.setIsPublic(true);
    }
    if (!declaresMethod(record, "hashCode", List.of())) {
      ImplicitMembers.addMethod(record, "hashCode",
          SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.INT)).setIsPublic(true);
    }
    if (!declaresMethod(record, "toString", List.of())) {
      ImplicitMembers.addMethod(record, "toString", string).setIsPublic(true);
    }
  }
  
  /** Whether the type declares a method with the given name and parameter types. */
  protected boolean declaresMethod(TypeDeclarationSymbol type, String name,
      List<SymTypeExpression> parameterTypes) {
    List<String> expected = parameterTypes.stream().map(SymTypeExpression::printFullName).toList();
    return getDeclaredMethods(type).stream()
        .filter(m -> !m.isIsConstructor() && m.getName().equals(name))
        .anyMatch(m -> m.getParameterList().stream()
            .map(p -> p.getType().printFullName()).toList().equals(expected));
  }
  
  /**
   * The type of the parameter of the canonical constructor for the component. As for declared
   * methods, the type of a variable arity parameter is its element type.
   */
  protected SymTypeExpression getParameterType(ASTRecordComponent component) {
    SymTypeExpression type = component.getSymbol().getType();
    return component.isHasVariableArity() ? type.asArrayType().getArgument() : type;
  }
  
  /** The methods and constructors that are declared in the model. */
  protected List<JavaMethodSymbol> getDeclaredMethods(TypeDeclarationSymbol type) {
    return type.getSpannedScope().getLocalJavaMethodSymbols().stream()
        .filter(JavaMethodSymbol::isPresentAstNode)
        .toList();
  }
  
  @Override
  public void endVisit(ASTEnumConstantDeclaration node) {
    EnumConstantDeclarationSymbol symbol = node.getSymbol();
    symbol.setIsStatic(true);
    symbol.setIsFinal(true);
    symbol.setIsReadOnly(true);
    symbol.setIsPublic(true);
    symbol.setIsEnumConstant(true);
    
    final String enumName = node.getEnclosingScope().getName();
    final Optional<TypeDeclarationSymbol> enumDeclaration =
        node.getEnclosingScope().getEnclosingScope().resolveTypeDeclarationLocally(enumName);
    if (enumDeclaration.isPresent()) {
      final SymTypeOfObject typeObject =
          SymTypeExpressionFactory.createTypeObject(enumDeclaration.get());
      symbol.setType(typeObject);
    }
    else {
      Log.error("0x0x7A003: Could not resolve enclosing enum declaration",
          node.get_SourcePositionStart());
    }
  }

  @Override
  public void endVisit(ASTConstructorDeclaration node) {
    JavaMethodSymbol symbol = node.getSymbol();
    IJavaDSLScope enclosingScope = (IJavaDSLScope) node.getEnclosingScope();
    ASTNode enclosingScopeNode = enclosingScope.getAstNode();
    switch (enclosingScopeNode) {
      case ASTClassDeclaration enclosingClass ->
          symbol.setType(SymTypeExpressionFactory.createFromSymbol(enclosingClass.getSymbol()));
      case ASTEnumDeclaration enclosingEnum ->
          symbol.setType(SymTypeExpressionFactory.createFromSymbol(enclosingEnum.getSymbol()));
      case ASTRecordDeclaration enclosingRecord ->
          symbol.setType(SymTypeExpressionFactory.createFromSymbol(enclosingRecord.getSymbol()));
      default -> Log.error(
          "0x7A004: Could not set ASTConstructorDeclaration type as it is not a direct child of a class, enum, or record declaration");
    }
  }
  
  @Override
  public void endVisit(ASTClassDeclaration node) {
    TypeDeclarationSymbol symbol = node.getSymbol();
    
    List<SymTypeExpression> supertypes = new ArrayList<>();
    if (node.isPresentSuperClass()) {
      supertypes.add(TypeCheck3.symTypeFromAST(node.getSuperClass()));
    }
    supertypes.addAll(node.getImplementedInterfaceList().stream().map(TypeCheck3::symTypeFromAST)
        .toList());
    symbol.setSuperTypesList(supertypes);
  }
  
  @Override
  public void endVisit(ASTFieldDeclaration node) {
    for (ASTVariableDeclarator v : node.getVariableDeclaratorList()) {
      SymTypeExpression declaratorType = TypeCheck3.symTypeFromAST(node.getMCType());
      if (v.getDeclarator() instanceof ASTArrayDeclaratorId arrayDeclaratorId) {
        declaratorType = SymTypeRelations.normalize(
            SymTypeExpressionFactory.createTypeArray(declaratorType, arrayDeclaratorId.sizeDim()));
      }
      v.getDeclarator().getSymbol().setType(declaratorType);
      addModifiersToField(v.getDeclarator().getSymbol(), node.getJavaModifierList());
    }
  }
  
  @Override
  public void endVisit(ASTAnnotationMethod node) {
    // adapted from endVisit(ASTMethodDeclaration) in JavaLightSTCompleteTypes
    JavaMethodSymbol symbol = node.getSymbol();
    addModifiersToMethOrConstr(symbol, node.getJavaModifierList());
    symbol.setType(TypeCheck3.symTypeFromAST(node.getMCType()));
  }
  
  @Override
  public void endVisit(ASTTryLocalVariableDeclaration node) {
    if (!node.isVar()) {
      FieldSymbol symbol = node.getDeclaratorId().getSymbol();
      symbol.setType(TypeCheck3.symTypeFromAST(node.getMCType()));
    }
  }
  
  protected void addModifiersToField(FieldSymbol fieldSymbol,
      Iterable<? extends ASTMCModifier> astModifierList) {
    for (ASTMCModifier modifier : astModifierList) {
      switch (modifier) {
        case ASTAnnotation annotation -> {
          //fieldSymbol.addAnnotations(TypeCheck3.symTypeFromAST(annotation.getAnnotationName()));
          // TODO: FieldSymbols do not support annotations yet -> annotations are lost
        }
        case ASTJavaModifier javaModifier -> {
          switch (javaModifier) {
            case ASTModifierPublic m -> fieldSymbol.setIsPublic(true);
            case ASTModifierProtected m -> fieldSymbol.setIsProtected(true);
            case ASTModifierPrivate m -> fieldSymbol.setIsPrivate(true);
            case ASTModifierStatic m -> fieldSymbol.setIsStatic(true);
            case ASTModifierFinal m -> fieldSymbol.setIsFinal(true);
            default -> {
            } // ignore
          }
        }
        default -> {
        } // ignore
      }
    }
  }
  
  protected void addModifiersToMethOrConstr(JavaMethodSymbol javaMethodSymbol,
      Iterable<? extends ASTMCModifier> astModifierList) {
    for (ASTMCModifier modifier : astModifierList) {
      switch (modifier) {
        case ASTAnnotation annotation -> javaMethodSymbol.addAnnotations(
            TypeCheck3.symTypeFromAST(annotation.getAnnotationName()));
        case ASTJavaModifier javaModifier -> {
          switch (javaModifier) {
            case ASTModifierPublic m -> javaMethodSymbol.setIsPublic(true);
            case ASTModifierProtected m -> javaMethodSymbol.setIsProtected(true);
            case ASTModifierPrivate m -> javaMethodSymbol.setIsPrivate(true);
            case ASTModifierAbstract m -> javaMethodSymbol.setIsAbstract(true);
            case ASTModifierStatic m -> javaMethodSymbol.setIsStatic(true);
            case ASTModifierFinal m -> javaMethodSymbol.setIsFinal(true);
            case ASTModifierNative m -> javaMethodSymbol.setIsNative(true);
            case ASTModifierStrictFp m -> javaMethodSymbol.setIsStrictfp(true);
            case ASTModifierSynchronized m -> javaMethodSymbol.setIsSynchronized(true);
            case ASTModifierDefault m -> javaMethodSymbol.setIsDefault(true);
            default -> {
            } // ignore
          }
        }
        default -> {
        } // ignore
      }
    }
  }
}
