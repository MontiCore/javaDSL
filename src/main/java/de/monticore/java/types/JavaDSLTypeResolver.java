/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.java.types;

import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;

/**
 * Type system is implemented in two steps. First step is to resolve type for AST nodes
 * and second step is to check type rules in CoCos by using resolved types.
 * To implement type resolving for AST nodes this abstract class must be extended by a handwritten class.
 * This abstract class extends de.monticore.types.TypeResolver<T>  and implements Visitor
 * All visit, endVisit, traverse methods of Visitor interface
 * is overwritten as final with empty body to prevent user from using those methods.
 * Only handle methods for each AST node should be overwritten in
 * the handwritten class to provide type resolving.
 */

abstract class JavaDSLTypeResolver<T> extends GenericTypeResolver<T> implements JavaDSLVisitor{

  @Override
  public final void visit(ASTNode node) {}

  @Override
  public final void endVisit(ASTNode node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTCompilationUnit node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTCompilationUnit node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTCompilationUnit node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTCompilationUnit node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTPackageDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTPackageDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTPackageDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTPackageDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTImportDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTImportDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTImportDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTImportDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTPrimitiveModifier node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTPrimitiveModifier node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTPrimitiveModifier node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTPrimitiveModifier node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTEmptyDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTEmptyDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTEmptyDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTEmptyDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTClassDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTClassDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTClassDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTClassDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTClassBody node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTClassBody node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTClassBody node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTClassBody node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTInterfaceDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTInterfaceDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTInterfaceDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTInterfaceDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTInterfaceBody node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTInterfaceBody node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTInterfaceBody node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTInterfaceBody node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTEnumDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTEnumDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTEnumDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTEnumDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTEnumBody node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTEnumBody node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTEnumBody node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTEnumBody node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTClassBlock node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTClassBlock node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTClassBlock node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTClassBlock node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTMethodDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTMethodDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTMethodDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTMethodDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTConstructorDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTConstructorDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTConstructorDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTConstructorDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTFieldDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTFieldDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTFieldDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTFieldDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTConstDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTConstDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTConstDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTConstDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTConstantDeclarator node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTConstantDeclarator node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTConstantDeclarator node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTConstantDeclarator node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTInterfaceMethodDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTInterfaceMethodDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTInterfaceMethodDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTInterfaceMethodDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTMethodSignature node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTMethodSignature node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTMethodSignature node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTMethodSignature node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTThrows node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTThrows node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTThrows node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTThrows node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTVariableDeclarator node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTVariableDeclarator node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTVariableDeclarator node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTVariableDeclarator node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTDeclaratorId node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTDeclaratorId node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTDeclaratorId node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTDeclaratorId node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTArrayInitializer node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTArrayInitializer node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTArrayInitializer node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTArrayInitializer node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTFormalParameters node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTFormalParameters node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTFormalParameters node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTFormalParameters node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTFormalParameterListing node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTFormalParameterListing node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTFormalParameterListing node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTFormalParameterListing node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTFormalParameter node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTFormalParameter node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTFormalParameter node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTFormalParameter node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTLastFormalParameter node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTLastFormalParameter node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTLastFormalParameter node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTLastFormalParameter node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnnotation node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnnotation node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnnotation node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTAnnotation node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnnotationPairArguments node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnnotationPairArguments node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnnotationPairArguments node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTAnnotationPairArguments node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTElementValuePair node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTElementValuePair node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTElementValuePair node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTElementValuePair node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTElementValueArrayInitializer node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTElementValueArrayInitializer node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTElementValueArrayInitializer node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTElementValueArrayInitializer node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnnotationTypeBody node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnnotationTypeBody node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnnotationTypeBody node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTAnnotationTypeBody node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnnotationMethod node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnnotationMethod node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnnotationMethod node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTAnnotationMethod node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnnotationConstant node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnnotationConstant node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnnotationConstant node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTAnnotationConstant node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTDefaultValue node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTDefaultValue node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTDefaultValue node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTDefaultValue node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTLocalVariableDeclarationStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTLocalVariableDeclarationStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTLocalVariableDeclarationStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTLocalVariableDeclarationStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTJavaBlock node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTJavaBlock node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTJavaBlock node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTJavaBlock node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAssertStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAssertStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAssertStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTAssertStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTIfStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTIfStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTIfStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTIfStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTForStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTForStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTForStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTForStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTCommonForControl node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTCommonForControl node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTCommonForControl node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTCommonForControl node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTForInitByExpressions node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTForInitByExpressions node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTForInitByExpressions node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTForInitByExpressions node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTEnhancedForControl node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTEnhancedForControl node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTEnhancedForControl node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTEnhancedForControl node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTWhileStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTWhileStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTWhileStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTWhileStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTDoWhileStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTDoWhileStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTDoWhileStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTDoWhileStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTTryStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTTryStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTTryStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTTryStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTCatchExceptionsHandler node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTCatchExceptionsHandler node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTCatchExceptionsHandler node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTCatchExceptionsHandler node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTFinallyBlockOnlyHandler node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTFinallyBlockOnlyHandler node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTFinallyBlockOnlyHandler node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTFinallyBlockOnlyHandler node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTTryStatementWithResources node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTTryStatementWithResources node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTTryStatementWithResources node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTTryStatementWithResources node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTResource node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTResource node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTResource node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTResource node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTIdentifierAndTypeArgument node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTIdentifierAndTypeArgument node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTIdentifierAndTypeArgument node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTIdentifierAndTypeArgument node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTCatchClause node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTCatchClause node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTCatchClause node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTCatchClause node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTCatchType node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTCatchType node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTCatchType node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTCatchType node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTSwitchStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTSwitchStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTSwitchStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTSwitchStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTSynchronizedStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTSynchronizedStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTSynchronizedStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTSynchronizedStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTReturnStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTReturnStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTReturnStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTReturnStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTThrowStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTThrowStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTThrowStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTThrowStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTBreakStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTBreakStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTBreakStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTBreakStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTContinueStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTContinueStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTContinueStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTContinueStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTEmptyStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTEmptyStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTEmptyStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTEmptyStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTExpressionStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTExpressionStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTExpressionStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTExpressionStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTLabeledStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTLabeledStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTLabeledStatement node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTLabeledStatement node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTSwitchBlockStatementGroup node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTSwitchBlockStatementGroup node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTSwitchBlockStatementGroup node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTSwitchBlockStatementGroup node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTConstantExpressionSwitchLabel node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTConstantExpressionSwitchLabel node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTConstantExpressionSwitchLabel node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTConstantExpressionSwitchLabel node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTEnumConstantSwitchLabel node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTEnumConstantSwitchLabel node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTEnumConstantSwitchLabel node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTEnumConstantSwitchLabel node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTDefaultSwitchLabel node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTDefaultSwitchLabel node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTDefaultSwitchLabel node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTDefaultSwitchLabel node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTExpression node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTExpression node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTExpression node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTExpression node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTPrimaryExpression node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTPrimaryExpression node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTPrimaryExpression node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTPrimaryExpression node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnonymousClass node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnonymousClass node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnonymousClass node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTAnonymousClass node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTArrayCreator node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTArrayCreator node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTArrayCreator node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTArrayCreator node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTArrayDimensionByInitializer node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTArrayDimensionByInitializer node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTArrayDimensionByInitializer node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTArrayDimensionByInitializer node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTCreatedName node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTCreatedName node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTCreatedName node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTCreatedName node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTInnerCreator node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTInnerCreator node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTInnerCreator node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTInnerCreator node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTClassCreatorRest node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTClassCreatorRest node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTClassCreatorRest node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTClassCreatorRest node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTExplicitGenericInvocation node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTExplicitGenericInvocation node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTExplicitGenericInvocation node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTExplicitGenericInvocation node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTExplicitGenericInvocationSuffix node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTExplicitGenericInvocationSuffix node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTExplicitGenericInvocationSuffix node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTExplicitGenericInvocationSuffix node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTSuperSuffix node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTSuperSuffix node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTSuperSuffix node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTSuperSuffix node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTArguments node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTArguments node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTArguments node) {}

  @Override
  public final void traverse(de.monticore.java.javadsl._ast.ASTArguments node) {}

  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTModifier node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTModifier node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTModifier node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTTypeDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTTypeDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTTypeDeclaration node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTClassBodyDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTClassBodyDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTClassBodyDeclaration node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTInterfaceBodyDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTInterfaceBodyDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTInterfaceBodyDeclaration node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTClassMemberDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTClassMemberDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTClassMemberDeclaration node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTMethodBody node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTMethodBody node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTMethodBody node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTConstructorBody node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTConstructorBody node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTConstructorBody node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTInterfaceMemberDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTInterfaceMemberDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTInterfaceMemberDeclaration node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTVariableInitializer node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTVariableInitializer node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTVariableInitializer node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnnotationArguments node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnnotationArguments node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnnotationArguments node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTElementValue node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTElementValue node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTElementValue node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTAnnotationTypeElementDeclaration node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTAnnotationTypeElementDeclaration node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTAnnotationTypeElementDeclaration node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTBlockStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTBlockStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTBlockStatement node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTStatement node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTStatement node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTStatement node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTForControl node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTForControl node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTForControl node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTForInit node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTForInit node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTForInit node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTExceptionHandler node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTExceptionHandler node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTExceptionHandler node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTFinallyBlock node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTFinallyBlock node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTFinallyBlock node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTSwitchLabel node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTSwitchLabel node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTSwitchLabel node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTCreator node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTCreator node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTCreator node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTArrayDimensionSpecifier node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTArrayDimensionSpecifier node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTArrayDimensionSpecifier node) {}
  @Override
  public final void visit(de.monticore.java.javadsl._ast.ASTJavaDSLNode node) {}

  @Override
  public final void endVisit(de.monticore.java.javadsl._ast.ASTJavaDSLNode node) {}

  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTJavaDSLNode node) {}
}