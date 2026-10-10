/* (c) https://github.com/MontiCore/monticore */
package invalid;

class Exceptions {

  void throwObject(Object o) {
    throw o;                                    // error: 0xA0918
  }

  void throwException() {
    throw new IllegalStateException();
  }

  void catchString() {
    try { } catch (String e) { }                // error: 0xA0903
  }

  void synchronizeOnInt(int i) {
    synchronized (i) { }                        // error: 0xA0918
  }

  void resourceNotCloseable() {
    try (Object r = new Object()) { }           // error: 0xA0920
  }
}
