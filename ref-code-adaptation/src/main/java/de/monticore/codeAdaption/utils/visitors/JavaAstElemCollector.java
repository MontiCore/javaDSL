package de.monticore.codeAdaption.utils.visitors;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTFormalParameterListing;
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
  private int typeNestingDepth;

  @Override
  public void visit(ASTClassDeclaration node) {
    if (typeNestingDepth == 0) {
      TypeElementCollector collector = visitType(node);
      collector.supertypesDeclarations.addAll(node.getImplementedInterfaceList());
      if (node.isPresentSuperClass()) {
        collector.supertypesDeclarations.add(node.getSuperClass());
      }
      typeElements.put(node, collector);
    }
    typeNestingDepth++;
  }

  @Override
  public void endVisit(ASTClassDeclaration node) {
    typeNestingDepth--;
  }

  @Override
  public void visit(ASTInterfaceDeclaration node) {
    if (typeNestingDepth == 0) {
      TypeElementCollector collector = visitType(node);
      collector.supertypesDeclarations.addAll(node.getExtendedInterfaceList());
      typeElements.put(node, collector);
    }
    typeNestingDepth++;
  }

  @Override
  public void endVisit(ASTInterfaceDeclaration node) {
    typeNestingDepth--;
  }

  @Override
  public void visit(ASTEnumDeclaration node) {
    if (typeNestingDepth == 0) {
      TypeElementCollector collector = visitType(node);
      typeElements.put(node, collector);
    }
    typeNestingDepth++;
  }

  @Override
  public void endVisit(ASTEnumDeclaration node) {
    typeNestingDepth--;
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
  private int typeNestingDepth;

  @Override
  public void visit(ASTClassDeclaration node) {
    typeNestingDepth++;
  }

  @Override
  public void endVisit(ASTClassDeclaration node) {
    typeNestingDepth--;
  }

  @Override
  public void visit(ASTInterfaceDeclaration node) {
    typeNestingDepth++;
  }

  @Override
  public void endVisit(ASTInterfaceDeclaration node) {
    typeNestingDepth--;
  }

  @Override
  public void visit(ASTEnumDeclaration node) {
    typeNestingDepth++;
  }

  @Override
  public void endVisit(ASTEnumDeclaration node) {
    typeNestingDepth--;
  }

  @Override
  public void visit(ASTMethodDeclaration node) {
    if (typeNestingDepth != 1) {
      return;
    }
    methodDeclarations.add(node);

    List<ASTLocalVariableDeclaration> localVars = new ArrayList<>();
    List<ASTFormalParameter> formalParams = new ArrayList<>();

    // Collect method signature parameters directly from the method declaration.
    // This avoids collecting for-each loop variables (which are also ASTFormalParameter
    // in the grammar) as formal parameters — they should be treated as local variables.
    Set<ASTFormalParameter> signatureParams = new HashSet<>();
    if (node.getFormalParameters().isPresentFormalParameterListing()) {
      ASTFormalParameterListing paramListing = node.getFormalParameters().getFormalParameterListing();
      formalParams.addAll(paramListing.getFormalParameterList());
      signatureParams.addAll(paramListing.getFormalParameterList());
    }

    JavaDSLTraverser traverser = JavaDSLMill.traverser();

    JavaDSLVisitor2 localVarCollector =
        new JavaDSLVisitor2() {
          @Override
          public void visit(ASTLocalVariableDeclaration node) {
            localVars.add(node);
          }
        };

    // For-each loop variables are ASTFormalParameter nodes in the method body.
    // Collect them as local variables instead of formal parameters,
    // since they have no CD-level parameter mapping.
    // Filter out method signature parameters to avoid double-counting.
    MCCommonStatementsVisitor2 forEachVarCollector =
        new MCCommonStatementsVisitor2() {
          @Override
          public void visit(ASTFormalParameter node) {
            if (!signatureParams.contains(node)) {
              localVars.add(wrapAsLocalVariable(node));
            }
          }
        };

    traverser.add4JavaDSL(localVarCollector);
    traverser.add4MCCommonStatements(forEachVarCollector);
    node.accept(traverser);
    localVarsMap.put(node, localVars);
    formalParamsMap.put(node, formalParams);
  }

  /**
   * Wraps an ASTFormalParameter like from a for-each loop as an ASTLocalVariableDeclaration
   * so it can be handled by the local variable matching path instead of the formal parameter path.
   */
  private ASTLocalVariableDeclaration wrapAsLocalVariable(ASTFormalParameter param) {
    ASTLocalVariableDeclaration localVar = JavaDSLMill.localVariableDeclarationBuilder()
        .setMCType(param.getMCType())
        .build();
    localVar.set_SourcePositionStart(param.get_SourcePositionStart());
    localVar.set_SourcePositionEnd(param.get_SourcePositionEnd());
    var variableDeclarator =
        JavaDSLMill.variableDeclaratorBuilder().setDeclarator(param.getDeclarator()).build();
    variableDeclarator.set_SourcePositionStart(param.get_SourcePositionStart());
    variableDeclarator.set_SourcePositionEnd(param.get_SourcePositionEnd());
    localVar.getVariableDeclaratorList().add(variableDeclarator);
    return localVar;
  }

  @Override
  public void visit(ASTFieldDeclaration node) {
    if (typeNestingDepth == 1) {
      fieldDeclarations.add(node);
    }
  }
}
