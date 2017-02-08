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
package de.monticore.java.lang;

import java.util.Optional;

import de.monticore.CommonModelingLanguage;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaSymbolTableCreator;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.resolving.CommonResolvingFilter;

public class JavaDSLLanguage extends CommonModelingLanguage {

  public static final String FILE_ENDING = "java";

  public JavaDSLLanguage() {
    super("Java", FILE_ENDING);

    addResolvingFilter(CommonResolvingFilter.create(JavaTypeSymbol.KIND));
    addResolvingFilter(CommonResolvingFilter.create(JavaFieldSymbol.KIND));
    addResolvingFilter(CommonResolvingFilter.create(JavaMethodSymbol.KIND));

    setModelNameCalculator(new JavaDSLModelNameCalculator());
  }

  @Override
  public JavaDSLParser getParser() {
    return new JavaDSLParser();
  }

  @Override
  public Optional<JavaSymbolTableCreator> getSymbolTableCreator(
      ResolvingConfiguration resolvingConfiguration, MutableScope enclosingScope) {
    return Optional
        .of(new JavaSymbolTableCreator(resolvingConfiguration, enclosingScope));
  }

  @Override
  protected JavaDSLModelLoader provideModelLoader() {
    return new JavaDSLModelLoader(this);
  }

  @Override
  public JavaDSLModelLoader getModelLoader() {
    return (JavaDSLModelLoader) super.getModelLoader();
  }

}
