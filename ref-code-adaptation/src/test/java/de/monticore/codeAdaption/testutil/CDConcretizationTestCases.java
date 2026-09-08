package de.monticore.codeAdaption.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/** Registry of all cdconcretization adaptation test fixtures. */
public final class CDConcretizationTestCases {

  private static final Path RESOURCE_ROOT = Path.of(CDConcretizationTestCase.RESOURCE_ROOT);

  private static final Map<String, String> REF_OVERRIDES =
      Map.ofEntries(
          Map.entry(
              "evaluation/getter-setter/DataModelConc.cd",
              "evaluation/getter-setter/GetterRef.cd"),
          Map.entry(
              "evaluation/builder/DataModelConc.cd", "evaluation/builder/BuilderAndMillRef.cd"),
          Map.entry(
              "evaluation/mill/LanguageInfrastructureConc.cd", "evaluation/mill/MillRef.cd"),
          Map.entry(
              "methods/forEach/ForEachAttributeSameReturnTypeClassMIConc.cd",
              "methods/forEach/ForEachAttributeSameReturnTypeRef.cd"),
          Map.entry(
              "types/forEach/ForEachTypeNoTargetIncConc.cd", "types/forEach/ForEachTypeRef.cd"),
          Map.entry(
              "attributes/forEach/ForEachAttributeInheritanceConc.cd",
              "attributes/forEach/ForEachAttributeRef.cd"),
          Map.entry(
              "attributes/forEach/ForEachAttributeDifferentNameClassMIConc.cd",
              "attributes/forEach/ForEachAttributeDifferentNameRef.cd"),
          Map.entry(
              "evaluation/banking/singleInc/BankingConc.cd", "evaluation/banking/BankingRef.cd"),
          Map.entry(
              "evaluation/banking/multiInc/BankingConc.cd", "evaluation/banking/BankingRef.cd"),
          Map.entry(
              "evaluation/banking/usageByExtension/BankingConc.cd",
              "evaluation/banking/BankingRef.cd"),
          Map.entry(
              "evaluation/staticDelegator/attrWorkaround/StaticExistsConc.cd",
              "evaluation/staticDelegator/attrWorkaround/StaticDelegatorRef.cd"),
          Map.entry(
              "evaluation/staticDelegator/attrWorkaround/InstanceMethodExistsConc.cd",
              "evaluation/staticDelegator/attrWorkaround/StaticDelegatorRef.cd"),
          Map.entry(
              "evaluation/observer/mutualObservers/MutualObserversConc.cd",
              "evaluation/observer/ObserverRef.cd"),
          Map.entry(
              "attributes/underspecified/AttributeTypeUnderspecifiedDifferentIncTypesConc.cd",
              "attributes/underspecified/AttributeTypeUnderspecifiedRef.cd"),
          Map.entry(
              "attributes/underspecified/AttributeTypeUnderspecifiedNoIncConc.cd",
              "attributes/underspecified/AttributeTypeUnderspecifiedRef.cd"),
          Map.entry(
              "methods/underspecified/ParameterTypeUnderspecifiedIncarnatedConc.cd",
              "methods/underspecified/ParameterTypeUnderspecifiedRef.cd"),
          Map.entry(
              "methods/underspecified/ParameterTypeUnderspecifiedNoIncConc.cd",
              "methods/underspecified/ParameterTypeUnderspecifiedRef.cd"),
          Map.entry(
              "methods/underspecified/ReturnTypeUnderspecifiedIncarnatedConc.cd",
              "methods/underspecified/ReturnTypeUnderspecifiedRef.cd"),
          Map.entry(
              "methods/underspecified/ReturnTypeUnderspecifiedNoIncConc.cd",
              "methods/underspecified/ReturnTypeUnderspecifiedRef.cd"),
          Map.entry(
              "multipleIncarnation/BothAssocSidesMIOneAssocExistsConc.cd",
              "multipleIncarnation/BothAssocSidesMIRef.cd"));

  private static final Set<String> EXPECTED_FAILURE_CASES =
      Set.of(
          "attributes/underspecified/AttributeTypeUnderspecifiedNoIncConc.cd",
          "methods/underspecified/ParameterTypeUnderspecifiedNoIncConc.cd",
          "methods/underspecified/ReturnTypeUnderspecifiedNoIncConc.cd");

  private static final Map<String, String> UNSUPPORTED_CASES =
      Map.of(
          "attributes/forEach/ForEachAttributeInheritanceConc.cd",
              "Upstream cdconcretization does not derive forEach bindings across inherited attribute owners",
          "attributes/forEach/ForEachAttributeNoTargetIncConc.cd",
              "Upstream cdconcretization requires unimplemented matchStructure/optional-member semantics",
          "evaluation/staticDelegator/StaticDelegatorConc.cd",
              "Upstream cdconcretization does not implement method-target forEach completion");

  private static final Map<String, Set<String>> MAPPING_OVERRIDES =
      Map.of(
          "evaluation/observer/mutualObservers/MutualObserversConc.cd",
          Set.of("ref1", "ref2"));

  private static final Map<String, Boolean> STRICT_PARAMETER_ORDER =
      Map.ofEntries(
          Map.entry("evaluation/builder/DataModelConc.cd", true),
          Map.entry("evaluation/getter-setter/DataModelConc.cd", false),
          Map.entry("evaluation/visitor/VisitorConc.cd", true),
          Map.entry("evaluation/banking2/BankingConc.cd", true),
          Map.entry("evaluation/mill/LanguageInfrastructureConc.cd", true),
          Map.entry("evaluation/repository/DomainModel.cd", true),
          Map.entry("evaluation/crud-backend/DomainModel.cd", true),
          Map.entry("evaluation/paper-examples/repository/EcommerceDomain.cd", true),
          Map.entry("methods/multiIncarnation/ParameterTypeMIConc.cd", true),
          Map.entry("methods/multiIncarnation/ParameterAndReturnTypeMIConc.cd", true),
          Map.entry("methods/forEach/ForEachAttributeConc.cd", true),
          Map.entry("methods/forEach/ForEachAttributeMultipleParametersConc.cd", true),
          Map.entry("methods/forEach/ForEachTypeSameParameterTypeConc.cd", true),
          Map.entry("methods/forEach/ForEachTypeSameParameterTypeNoNameMatchConc.cd", true),
          Map.entry("evaluation/staticDelegator/StaticDelegatorConc.cd", true),
          Map.entry("evaluation/staticDelegator/attrWorkaround/StaticExistsConc.cd", true),
          Map.entry("evaluation/staticDelegator/attrWorkaround/InstanceMethodExistsConc.cd", true),
          Map.entry("evaluation/transitiveDependencies/TransitiveDependenciesConc.cd", true),
          Map.entry("evaluation/crud-backend/CRUDBackendConc.cd", true));

  private static final Map<String, String> EXPECTED_OUTPUT_OVERRIDES =
      Map.of(
          "evaluation/builder/DataModelConc.cd", "evaluation/builder/BuilderAndMillOut.cd",
          "evaluation/getter-setter/DataModelConc.cd", "evaluation/getter-setter/GetterOut.cd",
          "evaluation/macoco/EmptyConc.cd", "evaluation/macoco/EmptyConcOut.cd",
          "evaluation/mill/LanguageInfrastructureConc.cd", "evaluation/mill/MillOut.cd");

  /** Upstream asserts these completed CDs directly against their reference CDs. */
  private static final Set<String> REFERENCE_AS_EXPECTED_OUTPUT_CASES =
      Set.of(
          "attributes/valid/AttributesMissingConc.cd",
          "attributes/valid/TwoAttributesMissingConc.cd",
          "attributes/valid/TwoAttributesMissingOneMatchConc.cd",
          "inheritance/MissingInheritanceConc.cd",
          "methods/basic/valid/ClassEmptyConc.cd",
          "methods/basic/valid/ClassMissingConc.cd",
          "methods/basic/valid/MethodMissingConc.cd",
          "methods/basic/valid/MultipleMethodsMissingConc.cd");

  private static final Map<String, String> STRUCTURAL_ORACLE_EXCLUSIONS =
      Map.ofEntries(
          Map.entry(
              "associations/AssociationMissingSimpleConc.cd",
              "Upstream test is disabled until its association semantics issue is clarified"),
          Map.entry(
              "inheritance/AttributeTypeMismatchConc.cd",
              "Upstream cdconcretization incorrectly accepts an inherited attribute with an incompatible type"),
          Map.entry(
              "evaluation/banking/multiInc/BankingConc.cd",
              "Upstream test is disabled because bind mappings are not considered while "
                  + "completing associations"),
          Map.entry(
              "evaluation/cross-references/MicroserviceConc.cd",
              "Upstream test is disabled because forEach cannot express global cross-incarnation references"),
          Map.entry(
              "evaluation/staticDelegator/attrWorkaround/InstanceMethodExistsConc.cd",
              "Upstream test is disabled because forEach completion is not bidirectional"));

  private CDConcretizationTestCases() {}

  public static List<CDConcretizationTestCase> allCases() {
    try {
      List<CDConcretizationTestCase> cases = new ArrayList<>();
      try (Stream<Path> paths = Files.walk(RESOURCE_ROOT)) {
        paths
            .filter(Files::isRegularFile)
            .map(RESOURCE_ROOT::relativize)
            .map(path -> path.toString().replace('\\', '/'))
            .filter(path -> path.endsWith("Conc.cd"))
            .sorted()
            .forEach(path -> cases.add(buildCase(path)));
      }
      cases.sort(Comparator.comparing(CDConcretizationTestCase::id));
      return List.copyOf(cases);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to discover cdconcretization test cases", e);
    }
  }

  static CDConcretizationTestCase buildCase(String concRelativePath) {
    String refRelativePath = resolveRefPath(concRelativePath);
    String caseBase = concRelativePath.substring(concRelativePath.lastIndexOf('/') + 1);
    caseBase = caseBase.substring(0, caseBase.length() - "Conc.cd".length());
    String id = concRelativePath.replace('/', '_').replace(".cd", "");

    Path refCd = RESOURCE_ROOT.resolve(refRelativePath);
    Path concCd = RESOURCE_ROOT.resolve(concRelativePath);

    boolean sharedRef = !parent(refRelativePath).equals(parent(concRelativePath));
    boolean sharedReferenceFile = refRelativePath.endsWith("Reference.cd");

    Path adapterPath;
    if (sharedRef || sharedReferenceFile) {
      adapterPath = resolveDirectory(parent(refRelativePath)).resolve("adapter");
    } else {
      adapterPath =
          resolveDirectory(parent(concRelativePath)).resolve("adapter").resolve(caseBase);
    }

    Path concretePath;
    if (sharedRef) {
      concretePath = resolveDirectory(parent(concRelativePath)).resolve("concrete").resolve(caseBase);
    } else if (sharedReferenceFile) {
      concretePath =
          resolveDirectory(parent(concRelativePath)).resolve("concrete").resolve(caseBase);
    } else {
      concretePath = resolveDirectory(parent(concRelativePath)).resolve("concrete");
    }

    Path outputPath = Path.of(CDConcretizationTestCase.OUTPUT_ROOT + id);

    return new CDConcretizationTestCase(
        id,
        concRelativePath.replace("Conc.cd", ""),
        refCd,
        concCd,
        adapterPath,
        concretePath,
        outputPath,
        MAPPING_OVERRIDES.getOrDefault(concRelativePath, Set.of("ref")),
        STRICT_PARAMETER_ORDER.getOrDefault(concRelativePath, false),
        !EXPECTED_FAILURE_CASES.contains(concRelativePath)
            && !UNSUPPORTED_CASES.containsKey(concRelativePath));
  }

  public static List<CDConcretizationTestCase> enabledCases() {
    String requestedCase = System.getenv("CDCONCRETIZATION_CASE");
    Set<String> requestedCases =
        requestedCase == null || requestedCase.isBlank()
            ? Set.of()
            : Set.of(requestedCase.split(","));
    return allCases().stream()
        .filter(CDConcretizationTestCase::enabled)
        .filter(
            testCase ->
                requestedCases.isEmpty()
                    || requestedCases.contains(testCase.id())
                    || requestedCases.contains(testCase.displayName()))
        .toList();
  }

  public static List<CDConcretizationTestCase> expectedFailureCases() {
    return allCases().stream()
        .filter(testCase -> EXPECTED_FAILURE_CASES.contains(relativeConcretePath(testCase)))
        .toList();
  }

  public static List<CDConcretizationTestCase> unsupportedCases() {
    return allCases().stream()
        .filter(testCase -> UNSUPPORTED_CASES.containsKey(relativeConcretePath(testCase)))
        .toList();
  }

  public static String unsupportedReason(CDConcretizationTestCase testCase) {
    return UNSUPPORTED_CASES.get(relativeConcretePath(testCase));
  }

  public static Optional<Path> expectedOutputModel(CDConcretizationTestCase testCase) {
    String concretePath = relativeConcretePath(testCase);
    if (STRUCTURAL_ORACLE_EXCLUSIONS.containsKey(concretePath)) {
      return Optional.empty();
    }
    if (REFERENCE_AS_EXPECTED_OUTPUT_CASES.contains(concretePath)) {
      return Optional.of(testCase.refCd());
    }
    String expectedPath =
        EXPECTED_OUTPUT_OVERRIDES.getOrDefault(
            concretePath, concretePath.replace("Conc.cd", "Out.cd"));
    Path expected = RESOURCE_ROOT.resolve(expectedPath);
    return Files.isRegularFile(expected) ? Optional.of(expected) : Optional.empty();
  }

  public static Optional<String> noExpectedOutputReason(CDConcretizationTestCase testCase) {
    return Optional.ofNullable(STRUCTURAL_ORACLE_EXCLUSIONS.get(relativeConcretePath(testCase)));
  }

  static String resolveRefPath(String concRelativePath) {
    if (REF_OVERRIDES.containsKey(concRelativePath)) {
      return REF_OVERRIDES.get(concRelativePath);
    }

    String concDir = parent(concRelativePath);
    String caseBase =
        concRelativePath
            .substring(concRelativePath.lastIndexOf('/') + 1)
            .replace("Conc.cd", "");

    String pairedRef = concDir.isEmpty() ? caseBase + "Ref.cd" : concDir + "/" + caseBase + "Ref.cd";
    if (Files.exists(RESOURCE_ROOT.resolve(pairedRef))) {
      return pairedRef;
    }

    String localReference = concDir.isEmpty() ? "Reference.cd" : concDir + "/Reference.cd";
    if (Files.exists(RESOURCE_ROOT.resolve(localReference))) {
      return localReference;
    }

    String parentReference =
        parent(concDir).isEmpty() ? "Reference.cd" : parent(concDir) + "/Reference.cd";
    if (Files.exists(RESOURCE_ROOT.resolve(parentReference))) {
      return parentReference;
    }

    String singleRefInDir = findSingleRefInDirectory(concDir);
    if (singleRefInDir != null) {
      return singleRefInDir;
    }

    throw new IllegalStateException(
        "Could not resolve reference CD for concrete fixture: " + concRelativePath);
  }

  private static String findSingleRefInDirectory(String concDir) {
    Path directory = resolveDirectory(concDir);
    if (!Files.isDirectory(directory)) {
      return null;
    }
    try (Stream<Path> refs = Files.list(directory)) {
      List<String> refFiles =
          refs.map(Path::getFileName)
              .map(Path::toString)
              .filter(name -> name.endsWith("Ref.cd"))
              .sorted()
              .toList();
      if (refFiles.size() == 1) {
        return concDir.isEmpty() ? refFiles.get(0) : concDir + "/" + refFiles.get(0);
      }
    } catch (IOException ignored) {
      return null;
    }
    return null;
  }

  private static String parent(String relativePath) {
    int slash = relativePath.lastIndexOf('/');
    return slash < 0 ? "" : relativePath.substring(0, slash);
  }

  private static String relativeConcretePath(CDConcretizationTestCase testCase) {
    return testCase.concCd().toString()
        .replace('\\', '/')
        .replace(CDConcretizationTestCase.RESOURCE_ROOT, "");
  }

  private static Path resolveDirectory(String relativeDir) {
    return relativeDir.isEmpty() ? RESOURCE_ROOT : RESOURCE_ROOT.resolve(relativeDir);
  }
}
