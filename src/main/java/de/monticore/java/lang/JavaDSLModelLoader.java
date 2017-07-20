/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
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
package de.monticore.java.lang;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.symboltable.JavaSymbolTableCreator;
import de.monticore.modelloader.ModelingLanguageModelLoader;
import de.monticore.symboltable.ArtifactScope;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

public class JavaDSLModelLoader extends ModelingLanguageModelLoader<ASTCompilationUnit> {

  public JavaDSLModelLoader(JavaDSLLanguage language) {
    super(language);
  }

  @Override
  protected void createSymbolTableFromAST(final ASTCompilationUnit ast, final String modelName,
      final MutableScope enclosingScope, final ResolvingConfiguration resolvingConfiguration) {
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
