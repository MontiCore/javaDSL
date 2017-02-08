package simpleTestClasses.types;

enum EmptyEnum {
}

enum OneSemicolon {
  ;
}

enum OneElement {
  ONE
}

enum OneElementAndSemicolon {
  ONE;
}

enum OneElementWithArgs {
  ONE(1);

  OneElementWithArgs(int i) {
  }
}

enum TwoElements {
  ONE, TWO
}

enum TwoElementsAndSemicolon {
  ONE, TWO;
}

enum TwoElementsWithArgs {
  ONE(1),
  TWO(2);

  TwoElementsWithArgs(int i) {
  }
}
