package de.monticore.codeAdaption.utils.visitors;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mccommonstatements._visitor.MCCommonStatementsVisitor2;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.*;

/***
 * this visitor collect Type, field,methods, local-variable and formal parameters
 * from a ASTOrdinaryCompilationUnit
 */

public class JavaAstElemCollector implements JavaDSLVisitor2 {
  private final List<ASTTypeDeclaration> typeDeclarations = new ArrayList<>();
  private final Map<ASTTypeDeclaration, TypeElementCollector> typeElements = new LinkedHashMap<>();

  @Override
  public void visit(ASTClassDeclaration node) {
    TypeElementCollector collector = visitType(node);

    collector.supertypesDeclarations.addAll(node.getImplementedInterfaceList());
    if (node.isPresentSuperClass()) {
      collector.supertypesDeclarations.add(node.getSuperClass());
    }

    typeElements.put(node, collector);
  }

  @Override
  public void visit(ASTInterfaceDeclaration node) {
    TypeElementCollector collector = visitType(node);
    collector.supertypesDeclarations.addAll(node.getExtendedInterfaceList());
    typeElements.put(node, collector);
  }

  @Override
  public void visit(ASTEnumDeclaration node) {
    TypeElementCollector collector = visitType(node);
    typeElements.put(node, collector);
  }

  private TypeElementCollector visitType(ASTTypeDeclaration node) {
    typeDeclarations.add(node);
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    TypeElementCollector collector = new TypeElementCollector();
    traverser.add4JavaDSL(collector);
    traverser.add4JavaLight(collector);

    node.accept(traverser);

    return collector;
  }

  public List<ASTTypeDeclaration> getAllTypeDeclarations() {
    return new ArrayList<>(typeDeclarations);
  }

  public List<ASTMethodDeclaration> getAllMethodDeclarations(ASTTypeDeclaration type) {
    if (typeElements.containsKey(type)) {
      return typeElements.get(type).methodDeclarations;
    }
    return new ArrayList<>();
  }

  public List<ASTFieldDeclaration> getAllFieldDeclarations(ASTTypeDeclaration type) {
    if (typeElements.containsKey(type)) {
      return typeElements.get(type).fieldDeclarations;
    }
    return new ArrayList<>();
  }

  public List<ASTMCType> getAllFSuperTypeDeclarations(ASTTypeDeclaration type) {
    if (typeElements.containsKey(type)) {
      return typeElements.get(type).supertypesDeclarations;
    }
    return new ArrayList<>();
  }

  public List<ASTFormalParameter> getAllParameters(
      ASTTypeDeclaration type, ASTMethodDeclaration method) {
    if (typeElements.containsKey(type)
        && typeElements.get(type).formalParamsMap.containsKey(method)) {
      return typeElements.get(type).formalParamsMap.get(method);
    }
    return new ArrayList<>();
  }

  public List<ASTLocalVariableDeclaration> getAllLocVariables(
      ASTTypeDeclaration type, ASTMethodDeclaration method) {
    if (typeElements.containsKey(type) && typeElements.get(type).localVarsMap.containsKey(method)) {
      return typeElements.get(type).localVarsMap.get(method);
    }
    return new ArrayList<>();
  }
}

/***
 * this class collect element of a type.
 */
class TypeElementCollector implements JavaDSLVisitor2, JavaLightVisitor2 {

  List<ASTMethodDeclaration> methodDeclarations = new ArrayList<>();
  List<ASTFieldDeclaration> fieldDeclarations = new ArrayList<>();
  List<ASTMCType> supertypesDeclarations = new ArrayList<>();

  Map<ASTMethodDeclaration, List<ASTLocalVariableDeclaration>> localVarsMap = new LinkedHashMap<>();
  Map<ASTMethodDeclaration, List<ASTFormalParameter>> formalParamsMap = new LinkedHashMap<>();

  @Override
  public void visit(ASTMethodDeclaration node) {
    methodDeclarations.add(node);

    List<ASTLocalVariableDeclaration> localVars = new ArrayList<>();
    List<ASTFormalParameter> formalParams = new ArrayList<>();

    JavaDSLTraverser traverser = JavaDSLMill.traverser();

    JavaDSLVisitor2 localVarCollector =
        new JavaDSLVisitor2() {
          @Override
          public void visit(ASTLocalVariableDeclaration node) {
            localVars.add(node);
          }
        };

    MCCommonStatementsVisitor2 formalParamVisitor =
        new MCCommonStatementsVisitor2() {
          @Override
          public void visit(ASTFormalParameter node) {
            formalParams.add(node);
          }
        };

    traverser.add4JavaDSL(localVarCollector);
    traverser.add4MCCommonStatements(formalParamVisitor);
    node.accept(traverser);
    localVarsMap.put(node, localVars);
    formalParamsMap.put(node, formalParams);
  }

  @Override
  public void visit(ASTFieldDeclaration node) {
    fieldDeclarations.add(node);
  }
}
