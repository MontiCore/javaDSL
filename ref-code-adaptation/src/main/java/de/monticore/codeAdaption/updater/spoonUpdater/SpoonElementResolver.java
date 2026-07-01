package de.monticore.codeAdaption.updater.spoonUpdater;

import static de.monticore.codeAdaption.utils.JavaLoader.print;

import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

final class SpoonElementResolver {

  private final Supplier<CtModel> model;
  private final Map<ASTTypeDeclaration, CtType<?>> typeMap = new LinkedHashMap<>();
  private final Map<ASTMethodDeclaration, CtMethod<?>> methodMap = new LinkedHashMap<>();

  SpoonElementResolver(Supplier<CtModel> model) {
    this.model = model;
  }

  CtType<?> getSpoonType(ASTTypeDeclaration mcType) {
    if (typeMap.containsKey(mcType)) {
      return typeMap.get(mcType);
    }
    Optional<CtType<?>> type =
        model.get().getAllTypes().stream().filter(t -> compare(mcType, t)).findFirst();
    assert type.isPresent();
    typeMap.put(mcType, type.get());
    return type.get();
  }

  CtMethod<?> getSpoonMethod(ASTTypeDeclaration mcType, ASTMethodDeclaration mcMethod) {
    if (methodMap.containsKey(mcMethod)) {
      return methodMap.get(mcMethod);
    }

    CtType<?> spoonType = getSpoonType(mcType);
    Optional<CtMethod<?>> method =
        spoonType.getAllMethods().stream()
            .filter(spMethod -> compare(mcMethod, spMethod))
            .findFirst();

    assert method.isPresent();
    methodMap.put(mcMethod, method.get());
    return method.get();
  }

  void cacheType(ASTTypeDeclaration mcType, CtType<?> spoonType) {
    typeMap.put(mcType, spoonType);
  }

  boolean compare(ASTTypeDeclaration type, CtType<?> spoonType) {
    String fileName = type.get_SourcePositionStart().getFileName().orElse(type.getName());
    String mcName = type.getName();
    String spoonName = spoonType.getSimpleName();

    String normalizedFileName = fileName.replace('\\', '/');
    int lastSlash = normalizedFileName.lastIndexOf('/');
    String leafFileName =
        lastSlash >= 0 ? normalizedFileName.substring(lastSlash + 1) : normalizedFileName;
    if ((spoonName + ".java").equals(leafFileName)) {
      return true;
    }
    return mcName.equals(spoonName);
  }

  boolean compare(ASTMethodDeclaration mcMethod, CtMethod<?> spoonMethod) {
    if (!mcMethod.getName().endsWith(spoonMethod.getSimpleName())) {
      return false;
    }
    if (!mcMethod.getFormalParameters().isPresentFormalParameterListing()) {
      return spoonMethod.getParameters().isEmpty();
    }
    List<ASTFormalParameter> mcParams =
        mcMethod.getFormalParameters().getFormalParameterListing().getFormalParameterList();
    if (spoonMethod.getParameters().size() != mcParams.size()) {
      return false;
    }
    for (int i = 0; i < spoonMethod.getParameters().size(); i++) {
      if (!(spoonMethod.getParameters().get(i).getType().getSimpleName())
          .equals(print(mcParams.get(i).getMCType()))) {
        return false;
      }
    }

    return true;
  }
}
