package de.monticore.java.visitors;
//package dmularski.javagrammar.visitors;
//
//import dmularski.javagrammar._ast.*;
//import dmularski.javagrammar._visitor.JavaVisitor;
//
//public class PrintVisitOrderVisitor extends AbstractPrintVisitOrderVisitor implements
//    JavaVisitor {
//
//  @Override
//  public void visit(ASTVisibilityModifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVisibilityModifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVariableModifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVariableModifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassBodyDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassBodyDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceBodyDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceBodyDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassMemberDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassMemberDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTMethodBody node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTMethodBody node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstructorBody node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstructorBody node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceMemberDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceMemberDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationTypeElementDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationTypeElementDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTForControl node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTForControl node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFinallyBlock node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFinallyBlock node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSwitchLabel node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSwitchLabel node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCreator node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCreator node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayDimensionSpecifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayDimensionSpecifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTNonWildcardTypeArgumentsOrDiamond node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTNonWildcardTypeArgumentsOrDiamond node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeArgumentsOrDiamond node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeArgumentsOrDiamond node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCompilationUnit node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCompilationUnit node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTPackageDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTPackageDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTImportDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTImportDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTModifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTModifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassOrInterfaceModifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassOrInterfaceModifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTPublicVisibilityModifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTPublicVisibilityModifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTProtectedVisibilityModifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTProtectedVisibilityModifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTPrivateVisibilityModifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTPrivateVisibilityModifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFinalVariableModifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFinalVariableModifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEmptyDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEmptyDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassBody node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassBody node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceBody node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceBody node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumConstants node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumConstants node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumConstant node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumConstant node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumBodyDeclarations node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumBodyDeclarations node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeParameters node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeParameters node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeParameter node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeParameter node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeBound node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeBound node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassBlock node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassBlock node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTMethodDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTMethodDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstructorDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstructorDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFieldDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFieldDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstantDeclarator node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstantDeclarator node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceMethodDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceMethodDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTMethodSignature node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTMethodSignature node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVariableDeclarator node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVariableDeclarator node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVariableDeclaratorId node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVariableDeclaratorId node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVariableInitializer node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVariableInitializer node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayInitializer node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayInitializer node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumConstantName node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumConstantName node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassOrInterfaceType node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassOrInterfaceType node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTIdentifierAndTypeArguments node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTIdentifierAndTypeArguments node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeArguments node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeArguments node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTQualifiedNameListing node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTQualifiedNameListing node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFormalParameters node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFormalParameters node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFormalParameterListing node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFormalParameterListing node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFormalParameter node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFormalParameter node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTLastFormalParameter node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTLastFormalParameter node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotation node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotation node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationName node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationName node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTElementValuePairs node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTElementValuePairs node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTElementValuePair node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTElementValuePair node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTElementValue node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTElementValue node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTElementValueArrayInitializer node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTElementValueArrayInitializer node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationTypeDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationTypeDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationTypeBody node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationTypeBody node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationMethod node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationMethod node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationConstant node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationConstant node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTDefaultValue node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTDefaultValue node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTBlockStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTBlockStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTLocalVariableDeclarationStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTLocalVariableDeclarationStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTLocalVariableDeclaration node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTLocalVariableDeclaration node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTBlock node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTBlock node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAssertStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAssertStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTIfStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTIfStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTForStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTForStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCommonForControl node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCommonForControl node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTForInit node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTForInit node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTForUpdate node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTForUpdate node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnhancedForControl node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnhancedForControl node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTWhileStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTWhileStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTDoWhileStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTDoWhileStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTryStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTryStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTryStatementWithResources node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTryStatementWithResources node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTResource node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTResource node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCatchClause node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCatchClause node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCatchType node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCatchType node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSwitchStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSwitchStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSynchronizedStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSynchronizedStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTReturnStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTReturnStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTThrowStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTThrowStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTBreakStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTBreakStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTContinueStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTContinueStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEmptyStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEmptyStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTExpressionStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTExpressionStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTLabeledStatement node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTLabeledStatement node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSwitchBlockStatementGroup node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSwitchBlockStatementGroup node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstantExpressionSwitchLabel node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstantExpressionSwitchLabel node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumConstantSwitchLabel node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumConstantSwitchLabel node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTDefaultSwitchLabel node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTDefaultSwitchLabel node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTExpression node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTExpression node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTPrimaryExpression node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTPrimaryExpression node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnonymousClass node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnonymousClass node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayCreator node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayCreator node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayDimensionByInitializer node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayDimensionByInitializer node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayDimensionByExpression node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayDimensionByExpression node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCreatedName node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCreatedName node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTIdentifierAndTypeArgumentsOrDiamond node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTIdentifierAndTypeArgumentsOrDiamond node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInnerCreator node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInnerCreator node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassCreatorRest node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassCreatorRest node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTExplicitGenericInvocation node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTExplicitGenericInvocation node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTNonWildcardTypeArguments node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTNonWildcardTypeArguments node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTDiamond node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTDiamond node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSuperSuffix node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSuperSuffix node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTExplicitGenericInvocationSuffix node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTExplicitGenericInvocationSuffix node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArguments node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArguments node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTIdentifier node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTIdentifier node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCompilationUnitList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCompilationUnitList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTPackageDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTPackageDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTImportDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTImportDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTModifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTModifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassOrInterfaceModifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassOrInterfaceModifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTPublicVisibilityModifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTPublicVisibilityModifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTProtectedVisibilityModifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTProtectedVisibilityModifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTPrivateVisibilityModifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTPrivateVisibilityModifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFinalVariableModifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFinalVariableModifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEmptyDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEmptyDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassBodyList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassBodyList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceBodyList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceBodyList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumConstantsList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumConstantsList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumConstantList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumConstantList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumBodyDeclarationsList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumBodyDeclarationsList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeParametersList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeParametersList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeParameterList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeParameterList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeBoundList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeBoundList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassBlockList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassBlockList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTMethodDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTMethodDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstructorDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstructorDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFieldDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFieldDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstantDeclaratorList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstantDeclaratorList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceMethodDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceMethodDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTMethodSignatureList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTMethodSignatureList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVariableDeclaratorList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVariableDeclaratorList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVariableDeclaratorIdList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVariableDeclaratorIdList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVariableInitializerList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVariableInitializerList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayInitializerList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayInitializerList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumConstantNameList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumConstantNameList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassOrInterfaceTypeList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassOrInterfaceTypeList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTIdentifierAndTypeArgumentsList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTIdentifierAndTypeArgumentsList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeArgumentsList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeArgumentsList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTQualifiedNameListingList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTQualifiedNameListingList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFormalParametersList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFormalParametersList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFormalParameterListingList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFormalParameterListingList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFormalParameterList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFormalParameterList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTLastFormalParameterList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTLastFormalParameterList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationNameList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationNameList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTElementValuePairsList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTElementValuePairsList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTElementValuePairList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTElementValuePairList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTElementValueList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTElementValueList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTElementValueArrayInitializerList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTElementValueArrayInitializerList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationTypeDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationTypeDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationTypeBodyList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationTypeBodyList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationMethodList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationMethodList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationConstantList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationConstantList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTDefaultValueList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTDefaultValueList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTBlockStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTBlockStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTLocalVariableDeclarationStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTLocalVariableDeclarationStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTLocalVariableDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTLocalVariableDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTBlockList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTBlockList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAssertStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAssertStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTIfStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTIfStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTForStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTForStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCommonForControlList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCommonForControlList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTForInitList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTForInitList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTForUpdateList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTForUpdateList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnhancedForControlList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnhancedForControlList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTWhileStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTWhileStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTDoWhileStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTDoWhileStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTryStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTryStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTryStatementWithResourcesList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTryStatementWithResourcesList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTResourceList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTResourceList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCatchClauseList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCatchClauseList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCatchTypeList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCatchTypeList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSwitchStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSwitchStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSynchronizedStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSynchronizedStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTReturnStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTReturnStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTThrowStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTThrowStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTBreakStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTBreakStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTContinueStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTContinueStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEmptyStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEmptyStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTExpressionStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTExpressionStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTLabeledStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTLabeledStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSwitchBlockStatementGroupList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSwitchBlockStatementGroupList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstantExpressionSwitchLabelList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstantExpressionSwitchLabelList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTEnumConstantSwitchLabelList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTEnumConstantSwitchLabelList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTDefaultSwitchLabelList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTDefaultSwitchLabelList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTExpressionList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTExpressionList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTPrimaryExpressionList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTPrimaryExpressionList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnonymousClassList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnonymousClassList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayCreatorList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayCreatorList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayDimensionByInitializerList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayDimensionByInitializerList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayDimensionByExpressionList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayDimensionByExpressionList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCreatedNameList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCreatedNameList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTIdentifierAndTypeArgumentsOrDiamondList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTIdentifierAndTypeArgumentsOrDiamondList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInnerCreatorList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInnerCreatorList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassCreatorRestList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassCreatorRestList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTExplicitGenericInvocationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTExplicitGenericInvocationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTNonWildcardTypeArgumentsList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTNonWildcardTypeArgumentsList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTDiamondList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTDiamondList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSuperSuffixList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSuperSuffixList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTExplicitGenericInvocationSuffixList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTExplicitGenericInvocationSuffixList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArgumentsList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArgumentsList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTIdentifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTIdentifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVisibilityModifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVisibilityModifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTVariableModifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTVariableModifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassBodyDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassBodyDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceBodyDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceBodyDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTClassMemberDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTClassMemberDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTMethodBodyList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTMethodBodyList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTConstructorBodyList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTConstructorBodyList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTInterfaceMemberDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTInterfaceMemberDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTAnnotationTypeElementDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTAnnotationTypeElementDeclarationList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTStatementList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTStatementList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTForControlList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTForControlList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTFinallyBlockList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTFinallyBlockList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTSwitchLabelList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTSwitchLabelList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTCreatorList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTCreatorList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTArrayDimensionSpecifierList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTArrayDimensionSpecifierList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTNonWildcardTypeArgumentsOrDiamondList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTNonWildcardTypeArgumentsOrDiamondList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//
//  @Override
//  public void visit(ASTTypeArgumentsOrDiamondList node) {
//    // TODO Auto-generated method stub
//    super.visit(node);
//  }
//
//  @Override
//  public void endVisit(ASTTypeArgumentsOrDiamondList node) {
//    // TODO Auto-generated method stub
//    super.endVisit(node);
//  }
//  
//}
