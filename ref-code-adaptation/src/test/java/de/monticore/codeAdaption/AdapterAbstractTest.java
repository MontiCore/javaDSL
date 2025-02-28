package de.monticore.codeAdaption;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.java.javadsl.JavaDSLMill;
import de.se_rwth.commons.logging.Log;

public abstract class AdapterAbstractTest {

  public static void initMills() {
    JavaDSLMill.init();
    JavaDSLMill.globalScope().clear();
    CD4CodeMill.init();
    CD4CodeMill.globalScope().clear();
    Log.init();
  }
}
