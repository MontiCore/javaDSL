package typeSystemTestModels.valid.interfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface InterfaceMethodSignatureNoOverrideEquivalent {

  public String getName();

  public void setName(String s);

  public void setList(List<Integer> list, ArrayList<Integer> array);

  public <T extends List<?>> T get(T t);

  public void setNaming(List<Integer> n);

}