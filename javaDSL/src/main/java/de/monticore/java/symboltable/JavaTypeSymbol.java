/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.symboltable;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.types.CommonJTypeSymbol;
import de.monticore.symboltable.types.JAttributeSymbolKind;
import de.monticore.symboltable.types.JMethodSymbolKind;
import de.monticore.symboltable.types.JTypeSymbolKind;

public class JavaTypeSymbol extends
    CommonJTypeSymbol<JavaTypeSymbol, JavaFieldSymbol, JavaMethodSymbol, JavaTypeSymbolReference> {
  public static final JavaTypeSymbolKind KIND = new JavaTypeSymbolKind();
  
  private boolean isStatic;

  private boolean isStrictfp;

  private boolean isAnnotation;

  private boolean isTypeVariable;

  private List<JavaTypeSymbolReference> annotations = new ArrayList<>();

  protected JavaTypeSymbol(
      String name,
      JTypeSymbolKind typeKind,
      JAttributeSymbolKind attributeKind,
      JMethodSymbolKind methodKind) {
    super(name, typeKind, attributeKind, methodKind);
  }

  public JavaTypeSymbol(String name) {
    this(name, JavaTypeSymbol.KIND, JavaFieldSymbol.KIND, JavaMethodSymbol.KIND);
  }

  public boolean isStatic() {
    return this.isStatic;
  }

  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  public boolean isStrictfp() {
    return this.isStrictfp;
  }

  public void setStrictfp(boolean isStrictfp) {
    this.isStrictfp = isStrictfp;
  }

  public boolean isAnnotation() {
    return this.isAnnotation;
  }

  public void setAnnotation(boolean isAnnotation) {
    this.isAnnotation = isAnnotation;
  }

  public boolean isTypeVariable() {
    return this.isTypeVariable;
  }

  public void setTypeVariable(boolean isTypeVariable) {
    this.isTypeVariable = isTypeVariable;
  }

  public void addAnnotation(JavaTypeSymbolReference annotation) {
    annotations.add(annotation);
  }

  public List<JavaTypeSymbolReference> getAnnotations() {
    return ImmutableList.copyOf(annotations);
  }
  
  /**
   * @see de.monticore.symboltable.types.CommonJTypeSymbol#isClass()
   */
  @Override
  public boolean isClass() {
    return !isInterface() && !isEnum() && !isAnnotation();
  }

  public static class JavaTypeSymbolKind extends JTypeSymbolKind {

    private static final String NAME = "de.monticore.java.symboltable.JavaTypeSymbolKind";

    protected JavaTypeSymbolKind() {
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
