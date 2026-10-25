package de.monticore.codeAdaption.context;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.JavaLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupingMappingServiceTest {
  @TempDir Path temporaryDirectory;

  @BeforeEach
  void initializeMills() {
    AdapterAbstractTest.initMills();
  }

  @Test
  void mapsConcreteSimpleNamesToGroupingSimpleNamesAndOmitsIdentityMappings()
      throws IOException {
    Path modelFile = temporaryDirectory.resolve("Model.cd");
    Files.writeString(
        modelFile, "classdiagram Model { class Concrete; class Group; class Identity; }");
    ASTCDCompilationUnit model = JavaLoader.parseCD(modelFile.toString());
    var types = model.getCDDefinition().getCDClassesList();
    var group = types.stream().filter(type -> "Group".equals(type.getName())).findFirst().orElseThrow();
    var identity =
        types.stream().filter(type -> "Identity".equals(type.getName())).findFirst().orElseThrow();
    IncarnationContext context =
        new IncarnationContext(
            "mapping",
            Map.of(),
            Map.of(
                StableElementKey.type("Concrete"),
                new IncarnationContext.MappedElement(StableElementKey.type("Group"), group.getSymbol()),
                StableElementKey.type("Identity"),
                new IncarnationContext.MappedElement(
                    StableElementKey.type("Identity"), identity.getSymbol())));

    assertEquals(Map.of("Concrete", "Group"), new GroupingMappingService().compute(context));
  }
}
