package de.monticore.codeAdaption.updater;

import de.monticore.codeAdaption.updater.spoonUpdater.SpoonUpdater;

/**
 * Creates fresh {@link CodeUpdater} instances for one isolated adaptation run.
 *
 * <p>The adapter writes every mapping or incarnation into its own temporary directory. Factory
 * implementations must therefore return a new updater instance for every call. Reusing an updater
 * would leak parser/model/output state between temporary directories.
 */
@FunctionalInterface
public interface CodeUpdaterFactory {

  CodeUpdater createUpdater();

  /** Returns the default Spoon-backed updater factory. */
  static CodeUpdaterFactory spoon() {
    return SpoonUpdater::new;
  }
}
