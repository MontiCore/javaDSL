package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTPackageDeclaration;

public final class JavaDSLScopesGenitor extends JavaDSLScopesGenitorTOP {

  @Override
  public IJavaDSLArtifactScope createFromAST(ASTCompilationUnit rootNode) {
    IJavaDSLArtifactScope artifactScope = super.createFromAST(rootNode);

    if (rootNode instanceof ASTOrdinaryCompilationUnit) {
      ASTOrdinaryCompilationUnit ordinaryCompilationUnit = (ASTOrdinaryCompilationUnit) rootNode;

      if (ordinaryCompilationUnit.isPresentPackageDeclaration()) {
        ASTPackageDeclaration packageDeclaration = ordinaryCompilationUnit.getPackageDeclaration();
        artifactScope.setPackageName(packageDeclaration.getMCQualifiedName().getQName());
      }
    }

    // TODO figure out how to map static imports

    return artifactScope;
  }

}
