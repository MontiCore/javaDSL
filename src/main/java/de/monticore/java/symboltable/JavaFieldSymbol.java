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
package de.monticore.java.symboltable;

import java.util.ArrayList;
import java.util.List;

import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.types.CommonJFieldSymbol;
import de.monticore.symboltable.types.JAttributeSymbolKind;

public class JavaFieldSymbol extends CommonJFieldSymbol<JavaTypeSymbolReference> {

  public static final JavaFieldSymbolKind KIND = new JavaFieldSymbolKind();

  final List<JavaTypeSymbolReference> annotations = new ArrayList<>();

  private boolean isVolatile = false;

  private boolean isTransient = false;

  public JavaFieldSymbol(String name, JAttributeSymbolKind kind, JavaTypeSymbolReference type) {
    super(name, kind, type);
  }

  public void addAnnotation(JavaTypeSymbolReference annotation) {
    annotations.add(annotation);
  }

  public List<JavaTypeSymbolReference> getAnnotations() {
    return annotations;
  }

  public boolean isVolatile() {
    return this.isVolatile;
  }

  public void setVolatile(boolean isVolatile) {
    this.isVolatile = isVolatile;
  }

  public boolean isTransient() {
    return this.isTransient;
  }

  public void setTransient(boolean isTransient) {
    this.isTransient = isTransient;
  }

  public static class JavaFieldSymbolKind extends JAttributeSymbolKind {

    private static final String NAME = "de.monticore.java.symboltable.JavaFieldSymbolKind";

    protected JavaFieldSymbolKind() {
    }

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    public boolean isKindOf(SymbolKind kind) {
      return NAME.equals(kind.getName()) || super.isKindOf(kind);
    }

  }
}
