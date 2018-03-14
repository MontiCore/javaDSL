/* (c) https://github.com/MontiCore/monticore */

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
