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

import com.google.common.collect.ImmutableList;
import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.types.CommonJMethodSymbol;
import de.monticore.symboltable.types.JMethodSymbolKind;

public class JavaMethodSymbol extends CommonJMethodSymbol<JavaTypeSymbol, JavaTypeSymbolReference, JavaFieldSymbol> {
  public static final JavaMethodSymbolKind KIND = new JavaMethodSymbolKind();

  private boolean isNative = false;

  private boolean isSynchronized;

  private boolean isStrictfp;

  private List<JavaTypeSymbolReference> annotations = new ArrayList<>();

  public JavaMethodSymbol(String name, JMethodSymbolKind kind) {
    super(name, kind);
  }

  public boolean isNative() {
    return this.isNative;
  }

  public void setNative(boolean isNative) {
    this.isNative = isNative;
  }

  public boolean isSynchronized() {
    return this.isSynchronized;
  }

  public void setSynchronized(boolean isSynchronized) {
    this.isSynchronized = isSynchronized;
  }

  public boolean isStrictfp() {
    return this.isStrictfp;
  }

  public void setStrictfp(boolean isStrictfp) {
    this.isStrictfp = isStrictfp;
  }

  public void addAnnotation(JavaTypeSymbolReference annotation) {
    annotations.add(annotation);
  }

  public List<JavaTypeSymbolReference> getAnnotations() {
    return ImmutableList.copyOf(annotations);
  }

  public static class JavaMethodSymbolKind extends JMethodSymbolKind {

    private static final String NAME = "de.monticore.java.symboltable.JavaMethodSymbolKind";

    protected JavaMethodSymbolKind(){}

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
