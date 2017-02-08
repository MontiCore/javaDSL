package parserBugs;

import java.util.HashSet;
import java.util.Set;

class SomeSuperClass {
  <T extends Set<?>> SomeSuperClass(int x) {
  }

  protected <T extends Number> void explicitGenericInvocation(int i) {
  }
}

public class ExplicitGenericInvocations extends SomeSuperClass {
  public ExplicitGenericInvocations() {
    <HashSet<?>>super(123);
    this.<Integer>explicitGenericInvocation(123);
  }

  class InnerClass {
    InnerClass() {
      // The following line is a corner case that can not be parsed
      ExplicitGenericInvocations.super.<Integer>explicitGenericInvocation(123);
      // The parser fails here         ^
    }
  }
}
