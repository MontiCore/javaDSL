/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.expressions;

public class Condition {

  boolean b = (1==2)? true : false;
  boolean c = (1==2)? getBoolean(): getBoolean();

  public boolean getBoolean(){

  }


}
