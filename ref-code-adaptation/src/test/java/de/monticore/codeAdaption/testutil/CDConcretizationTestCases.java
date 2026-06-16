package de.monticore.codeAdaption.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

  /** Not yet implemented/broken/more complex cases */
  private static final Set<String> DISABLED_CASES =
      Set.of(
          "evaluation/staticDelegator/StaticDelegatorConc.cd",
          "evaluation/staticDelegator/attrWorkaround/InstanceMethodExistsConc.cd",
          "evaluation/macoco/EmptyConc.cd",
          "methods/multiIncarnation/ReturnTypeMIOneExistsConc.cd",
          "methods/underspecified/ParameterTypeUnderspecifiedNoIncConc.cd",
          "methods/underspecified/ReturnTypeUnderspecifiedNoIncConc.cd",
          "associations/AssocSubtypeTargetConc.cd",
          "multipleIncarnation/InterfaceMIConc.cd");

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
        STRICT_PARAMETER_ORDER.getOrDefault(concRelativePath, false),
        !DISABLED_CASES.contains(concRelativePath));
  }

  public static List<CDConcretizationTestCase> enabledCases() {
    return allCases().stream().filter(CDConcretizationTestCase::enabled).toList();
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

  private static Path resolveDirectory(String relativeDir) {
    return relativeDir.isEmpty() ? RESOURCE_ROOT : RESOURCE_ROOT.resolve(relativeDir);
  }
}
