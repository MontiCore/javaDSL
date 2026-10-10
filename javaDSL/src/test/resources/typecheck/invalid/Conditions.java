/* (c) https://github.com/MontiCore/monticore */
package invalid;

class Conditions {

  void conditions(int i, String s, boolean flag, Boolean boxed) {
    if (flag) { }
    if (boxed) { }
    if (i) { }                                  // error: 0xA0909
    while (s) { }                               // error: 0xA0919
    do { } while (i + 1);                       // error: 0xA0905
    for (int j = 0; j; j++) { }                 // error: 0xA0906
    assert i;                                   // error: 0xA0901
    assert flag : "message";
  }
}
