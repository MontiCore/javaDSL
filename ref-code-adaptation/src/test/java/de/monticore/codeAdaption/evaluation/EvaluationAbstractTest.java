package de.monticore.codeAdaption.evaluation;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.AdapterParam;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.io.TempDir;

public abstract class EvaluationAbstractTest extends AdapterAbstractTest {
  @TempDir protected Path temporaryDirectory;

  protected final String resourcesPath = "src/test/resources/de/monticore/codeAdaption/evaluation/";
  protected File referenceCD;
  protected File concreteCD;

  protected Set<AdapterParam> adapterParams;
  protected Set<CDConfParameter> confParameters;

  protected Path refCodePath;
  protected Path conCodePath;
  protected Path output;
}
