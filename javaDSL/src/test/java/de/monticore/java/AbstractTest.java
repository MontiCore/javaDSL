package de.monticore.java;

import de.se_rwth.commons.logging.LogStub;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * An abstract class supposed to be implemented by test classes to simplify
 * global state management.
 */
public abstract class AbstractTest {

  @BeforeAll
  public static void setup() {
    LogStub.init();
    LogStub.enableFailQuick(false);
  }

  @BeforeEach
  public void reset() {
    LogStub.init();
  }

}