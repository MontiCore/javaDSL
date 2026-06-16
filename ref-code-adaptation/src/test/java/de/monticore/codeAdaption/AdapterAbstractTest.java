package de.monticore.codeAdaption;

import de.monticore.cd._symboltable.BuiltInTypes;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdconcretization.UnderspecifiedPlaceholderType;
import de.monticore.java.javadsl.JavaDSLMill;
import de.se_rwth.commons.logging.Log;

public abstract class AdapterAbstractTest {

  public static void initMills() {
    JavaDSLMill.init();
    JavaDSLMill.globalScope().clear();
    CD4CodeMill.init();
    CD4CodeMill.globalScope().clear();
    BuiltInTypes.addBuiltInTypes(CD4CodeMill.globalScope());
    UnderspecifiedPlaceholderType.addPlaceholderType(CD4CodeMill.globalScope());
    Log.init();
  }
}
