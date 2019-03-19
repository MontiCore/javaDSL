/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.symboltable;


import de.monticore.types.MCTypesJTypeSymbolsHelper;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @since TODO: add version number
 */
public class JavaSymbolFactory implements MCTypesJTypeSymbolsHelper.JTypeFactory<JavaTypeSymbol> {
  
  // ----------------------
  // attributes
  // ----------------------
  
  public JavaFieldSymbol createFormalParameterSymbol(String name, JavaTypeSymbolReference type) {
    JavaFieldSymbol javaFormalParameterSymbol = new JavaFieldSymbol(name,
        JavaFieldSymbol.KIND, type);
    
    // init
    javaFormalParameterSymbol.setParameter(true);
    
    return javaFormalParameterSymbol;
  }
  
  public JavaFieldSymbol createFieldSymbol(String name, JavaTypeSymbolReference javaTypeSymbol) {
    JavaFieldSymbol javaFieldSymbol = new JavaFieldSymbol(name, JavaFieldSymbol.KIND,
        javaTypeSymbol);
    
    // init (no init yet)
    
    return javaFieldSymbol;
  }
  
  public JavaFieldSymbol createLocalVariableSymbol(String name,
      JavaTypeSymbolReference javaTypeSymbol) {
    JavaFieldSymbol javaLocalVariableSymbol = new JavaFieldSymbol(name,
        JavaFieldSymbol.KIND, javaTypeSymbol);
    
    // init (no init yet)
    
    return javaLocalVariableSymbol;
  }
  
  // ----------------------
  // methods
  // ----------------------
  public JavaMethodSymbol createMethod(String name) {
    JavaMethodSymbol javaMethodSymbol = new JavaMethodSymbol(name, JavaMethodSymbol.KIND);
    return javaMethodSymbol;
  }
  
  public JavaMethodSymbol createConstructor(String name) {
    JavaMethodSymbol javaConstructorSymbol = new JavaMethodSymbol(name, JavaMethodSymbol.KIND);
    
    // init
    javaConstructorSymbol.setConstructor(true);
    
    return javaConstructorSymbol;
  }
  
  // ----------------------
  // types
  // ----------------------
  public JavaTypeSymbol createClassSymbol(String name) {
    JavaTypeSymbol javaTypeVariableSymbol = new JavaTypeSymbol(name);
    
    // class init (nothing here yet)
    
    return javaTypeVariableSymbol;
  }
  
  public JavaTypeSymbol createInterfaceSymbol(String name) {
    JavaTypeSymbol javaInterfaceSymbol = new JavaTypeSymbol(name);
    
    // interface init
    javaInterfaceSymbol.setInterface(true);
    javaInterfaceSymbol.setAbstract(true);
    
    return javaInterfaceSymbol;
  }
  
  @Override
  public JavaTypeSymbol createTypeVariable(String name) {
    JavaTypeSymbol javaTypeVariableSymbol = new JavaTypeSymbol(name);
    
    // type variable init
    // TODO do these serve the same purpose? if yes type variable is redundant
    javaTypeVariableSymbol.setFormalTypeParameter(true);
    javaTypeVariableSymbol.setTypeVariable(true);
    
    return javaTypeVariableSymbol;
  }
  
  public JavaTypeSymbol createEnumSymbol(String name) {
    JavaTypeSymbol javaEnumTypeSymbol = new JavaTypeSymbol(name);
    
    // enum init
    javaEnumTypeSymbol.setEnum(true);
    
    return javaEnumTypeSymbol;
  }
  
  public JavaTypeSymbol createAnnotation(String name) {
    JavaTypeSymbol javaAnnotationTypeSymbol = new JavaTypeSymbol(name);
    
    // Annotation init
    javaAnnotationTypeSymbol.setAnnotation(true);
    
    return javaAnnotationTypeSymbol;
  }
}
