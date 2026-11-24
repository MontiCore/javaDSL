package de.monticore.codeAdaption.cdconcretization.evaluation.mill.adapter.languageinfrastructure;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Mill"}, template = "${}")
public class Mill {
  @Adapt(ref = {"Mill.init"}, template = "${}")
  public void init() {
  }

  @Adapt(ref = {"Mill.initMe"}, template = "${}")
  public void initMe(Mill me) {
  }

  @Adapt(ref = {"Mill._MillProduct"}, template = "${}")
  public MillProduct _MillProduct() {
    return null;
  }

  @Adapt(ref = {"Mill.millProduct"}, template = "${}")
  public MillProduct millProduct() {
    return null;
  }

}
