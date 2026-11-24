package de.monticore.codeAdaption.updater;

import de.monticore.codeAdaption.updater.spoonUpdater.SpoonUpdater;
import java.util.Objects;
import java.util.function.Supplier;

/** Owns the updater lifecycle for one isolated adaptation pass. */
public final class CodeUpdaterMill {

  private static Supplier<? extends CodeUpdater> updaterProvider = SpoonUpdater::new;
  private static CodeUpdater updater;

  private CodeUpdaterMill() {}

  /** Restores the default Spoon-backed updater provider and clears the current updater. */
  public static synchronized void init() {
    init(SpoonUpdater::new);
  }

  /** Configures an interchangeable updater provider and clears the current updater. */
  public static synchronized void init(Supplier<? extends CodeUpdater> provider) {
    updaterProvider = Objects.requireNonNull(provider, "provider");
    updater = null;
  }

  /** Returns the updater for the current isolated adaptation pass. */
  public static synchronized CodeUpdater getUpdater() {
    if (updater == null) {
      updater = Objects.requireNonNull(updaterProvider.get(), "updaterProvider.get()");
    }
    return updater;
  }

  /** Discards the current updater while retaining the configured provider. */
  public static synchronized void reset() {
    updater = null;
  }
}
