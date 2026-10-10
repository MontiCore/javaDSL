/* (c) https://github.com/MontiCore/monticore */
package invalid;

import java.util.List;

class ForEach {

  void loops(List<String> names, int[] numbers, int count) {
    for (String name : names) { }
    for (int number : numbers) { }
    for (int name : names) { }                  // error: 0xA0908
    for (String number : numbers) { }           // error: 0xA0908
    for (int i : count) { }                     // error: 0xA0907
  }
}
