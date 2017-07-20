package parserBugs;

import java.util.HashSet;
import java.util.Set;

public class ExplicitGenericInvocations  {
  public void foll() {
      ExplicitGenericInvocations.super.<Integer>explicitGenericInvocation(123);
      // The parser fails here         ^
  }
}
