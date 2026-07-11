package de.monticore.codeAdaption.updater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.monticore.codeAdaption.updater.spoonUpdater.SpoonUpdater;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CodeUpdaterMillTest {

  @AfterEach
  void restoreDefaultProvider() {
    CodeUpdaterMill.init();
  }

  @Test
  void reusesCurrentUpdaterUntilReset() {
    AtomicInteger createdUpdaters = new AtomicInteger();
    CodeUpdaterMill.init(
        () -> {
          createdUpdaters.incrementAndGet();
          return new SpoonUpdater();
        });

    CodeUpdater first = CodeUpdaterMill.getUpdater();

    assertSame(first, CodeUpdaterMill.getUpdater());
    assertEquals(1, createdUpdaters.get());

    CodeUpdaterMill.reset();
    CodeUpdater second = CodeUpdaterMill.getUpdater();

    assertNotSame(first, second);
    assertEquals(2, createdUpdaters.get());
  }

  @Test
  void initRestoresDefaultSpoonProviderAndClearsCurrentUpdater() {
    CodeUpdater customUpdater = new SpoonUpdater();
    CodeUpdaterMill.init(() -> customUpdater);
    assertSame(customUpdater, CodeUpdaterMill.getUpdater());

    CodeUpdaterMill.init();
    CodeUpdater defaultUpdater = CodeUpdaterMill.getUpdater();

    assertInstanceOf(SpoonUpdater.class, defaultUpdater);
    assertNotSame(customUpdater, defaultUpdater);
  }
}
