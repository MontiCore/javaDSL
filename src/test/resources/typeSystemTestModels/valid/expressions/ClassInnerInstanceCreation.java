/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.expressions;

public class ClassInnerInstanceCreation {
  String.InnerString s = new String.InnerString(){
    @Override public int getNumber() {
      return 1;
    }
  };

}
