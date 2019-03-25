/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.lang;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.symboltable.JavaSymbolTableCreator;
import de.monticore.modelloader.ModelingLanguageModelLoader;
import de.monticore.symboltable.ArtifactScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

public class JavaDSLModelLoader extends ModelingLanguageModelLoader<ASTCompilationUnit> {

  public JavaDSLModelLoader(JavaDSLLanguage language) {
    super(language);
  }

  @Override
  protected void createSymbolTableFromAST(final ASTCompilationUnit ast, final String modelName,
      final Scope enclosingScope, final ResolvingConfiguration resolvingConfiguration) {
    final JavaSymbolTableCreator symbolTableCreator = getModelingLanguage()
        .getSymbolTableCreator(resolvingConfiguration, enclosingScope).orElse(null);

    if (symbolTableCreator != null) {
      Log.debug("Start creation of symbol table for model \"" + modelName + "\".",
          JavaDSLModelLoader.class.getSimpleName());
      final Scope scope = symbolTableCreator.createFromAST(ast);

      if (!(scope instanceof ArtifactScope)) {
        Log.warn("Top scope of model " + modelName + " is expected to be an artifact scope, but"
            + " is scope \"" + scope.getName() + "\"");
      }

      Log.debug("Created symbol table for model \"" + modelName + "\".", JavaDSLModelLoader.class
          .getSimpleName());
    }
    else {
      Log.warn("No symbol created, because '" + getModelingLanguage().getName()
          + "' does not define a symbol table creator.");
    }
  }

  @Override
  public JavaDSLLanguage getModelingLanguage() {
    return (JavaDSLLanguage) super.getModelingLanguage();
  }
}
