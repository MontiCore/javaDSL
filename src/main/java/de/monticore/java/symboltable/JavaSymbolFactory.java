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

import de.monticore.types.JTypeSymbolsHelper.JTypeFactory;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @since TODO: add version number
 */
public class JavaSymbolFactory implements JTypeFactory<JavaTypeSymbol> {
  
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
