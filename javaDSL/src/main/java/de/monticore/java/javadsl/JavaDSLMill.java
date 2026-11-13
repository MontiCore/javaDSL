package de.monticore.java.javadsl;

import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitorP2;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitorP2Delegator;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitorP3;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitorP3Delegator;
import de.monticore.java.javadsl.types3.JavaDSLTypeCheck3;

public class JavaDSLMill extends JavaDSLMillTOP {
  
  protected static JavaDSLMill millJavaDSLScopesGenitorP2;
  
  protected static JavaDSLMill millJavaDSLScopesGenitorP2Delegator;
  
  protected static JavaDSLMill millJavaDSLScopesGenitorP3;
  
  protected static JavaDSLMill millJavaDSLScopesGenitorP3Delegator;
  
  public static JavaDSLScopesGenitorP2 scopesGenitorP2() {
    if (millJavaDSLScopesGenitorP2 == null) {
      millJavaDSLScopesGenitorP2 = getMill();
    }
    return millJavaDSLScopesGenitorP2._scopesGenitorP2();
  }
  
  protected JavaDSLScopesGenitorP2 _scopesGenitorP2() {
    return new JavaDSLScopesGenitorP2();
  }
  
  public static JavaDSLScopesGenitorP2Delegator scopesGenitorP2Delegator() {
    if (millJavaDSLScopesGenitorP2Delegator == null) {
      millJavaDSLScopesGenitorP2Delegator = getMill();
    }
    return millJavaDSLScopesGenitorP2Delegator._scopesGenitorP2Delegator();
  }
  
  protected JavaDSLScopesGenitorP2Delegator _scopesGenitorP2Delegator() {
    return new JavaDSLScopesGenitorP2Delegator();
  }
  
  public static JavaDSLScopesGenitorP3 scopesGenitorP3() {
    if (millJavaDSLScopesGenitorP3 == null) {
      millJavaDSLScopesGenitorP3 = getMill();
    }
    return millJavaDSLScopesGenitorP3._scopesGenitorP3();
  }
  
  protected JavaDSLScopesGenitorP3 _scopesGenitorP3() {
    return new JavaDSLScopesGenitorP3();
  }
  
  public static JavaDSLScopesGenitorP3Delegator scopesGenitorP3Delegator() {
    if (millJavaDSLScopesGenitorP3Delegator == null) {
      millJavaDSLScopesGenitorP3Delegator = getMill();
    }
    return millJavaDSLScopesGenitorP3Delegator._scopesGenitorP3Delegator();
  }
  
  protected JavaDSLScopesGenitorP3Delegator _scopesGenitorP3Delegator() {
    return new JavaDSLScopesGenitorP3Delegator();
  }
  
  /** additionally inits the TypeCheck */
  public static void init() {
    JavaDSLMillTOP.init();
    JavaDSLTypeCheck3.init();
  }
  
  public static void initMe(JavaDSLMill a) {
    JavaDSLMillTOP.initMe(a);
    millJavaDSLScopesGenitorP3 = a;
    millJavaDSLScopesGenitorP3Delegator = a;
  }
  
  public static void reset() {
    JavaDSLMillTOP.reset();
    JavaDSLTypeCheck3.reset();
    millJavaDSLScopesGenitorP3 = null;
    millJavaDSLScopesGenitorP3Delegator = null;
  }
}
