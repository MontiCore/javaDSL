/* (c) https://github.com/MontiCore/monticore */

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
