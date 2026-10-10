/* (c) https://github.com/MontiCore/monticore */
package invalid;

class Returns {

  int noValue() {                               // error: 0xA0911
    return;
  }

  void value() {                                // error: 0xA0910
    return 1;
  }

  String wrongType() {                          // error: 0xA0912
    return 42;
  }

  long widening(int i) {
    return i;
  }

  Object subType() {
    return "text";
  }
}
