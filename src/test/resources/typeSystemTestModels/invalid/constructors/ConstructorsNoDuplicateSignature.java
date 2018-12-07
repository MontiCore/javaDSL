/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.constructors;

import java.util.List;
import java.lang.String;

public class ConstructorsNoDuplicateSignature {

  public ConstructorsNoDuplicateSignature(List<String> list){

  }

  private ConstructorsNoDuplicateSignature(List<Integer> list){

  }


  public ConstructorsNoDuplicateSignature(String s){

  }

  private ConstructorsNoDuplicateSignature(String string){

  }

}
