package de.monticore.java.javadsl;

import de.monticore.java.javadsl.types3.JavaDSLTypeCheck3;

public class JavaDSLMill extends JavaDSLMillTOP {
  
  /** additionally inits the TypeCheck */
  public static void init() {
    JavaDSLMillTOP.init();
    JavaDSLTypeCheck3.init();
  }
  
  public static void reset() {
    JavaDSLMillTOP.reset();
    JavaDSLTypeCheck3.reset();
  }
}
