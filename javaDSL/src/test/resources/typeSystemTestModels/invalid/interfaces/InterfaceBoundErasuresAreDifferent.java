/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.interfaces;

import java.util.List;
import java.lang.Integer;
import java.lang.String;

public interface InterfaceBoundErasuresAreDifferent<T extends List<String> & List<Integer>> {

}
