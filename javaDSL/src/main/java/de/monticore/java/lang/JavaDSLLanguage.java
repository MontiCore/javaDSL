/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.lang;

import de.monticore.CommonModelingLanguage;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaSymbolTableCreator;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.resolving.CommonResolvingFilter;

import java.util.Optional;

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
      ResolvingConfiguration resolvingConfiguration, Scope enclosingScope) {
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
