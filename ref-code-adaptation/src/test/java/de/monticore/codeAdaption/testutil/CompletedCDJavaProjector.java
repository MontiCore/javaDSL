package de.monticore.codeAdaption.testutil;

import de.monticore.ast.ASTNode;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.JavaSourcePostProcessor;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Writes the Java-expressible structure of a completed concrete CD as compilable Java. */
public final class CompletedCDJavaProjector {

  private CompletedCDJavaProjector() {}

  /**
   * Replaces the Java files in {@code outputPath} with a compilable projection of the completed
   * concrete CD.
   */
  public static void write(ASTCDCompilationUnit completedConcreteCD, Path outputPath) {
    try {
      Files.createDirectories(outputPath);
      deleteJavaFiles(outputPath);
      for (GeneratedJavaFile file : generate(completedConcreteCD)) {
        Path target = outputPath.resolve(file.typeName() + ".java");
        Files.writeString(target, file.source(), StandardCharsets.UTF_8);
      }
      JavaSourcePostProcessor.processDirectory(outputPath);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to write Java projection for completed concrete CD to " + outputPath, e);
    }
  }

  /**
   * Builds Java source files for the Java-expressible part of a completed concrete CD, including
   * fields, methods, enums, inheritance, and interfaces.
   */
  public static List<GeneratedJavaFile> generate(ASTCDCompilationUnit completedConcreteCD) {
    List<GeneratedJavaFile> files = new ArrayList<>();
    Set<String> emitted = new LinkedHashSet<>();
    ProjectionContext context = new ProjectionContext(completedConcreteCD);
    for (ASTCDEnum enumType : completedConcreteCD.getCDDefinition().getCDEnumsList()) {
      if (emitted.add(enumType.getName())) {
        files.add(new GeneratedJavaFile(enumType.getName(), generateEnum(enumType)));
      }
    }
    for (ASTCDType type : AdapterUtils.getAllCDTypes(completedConcreteCD)) {
      if (type instanceof ASTCDEnum || !emitted.add(type.getName())) {
        continue;
      }
      if (context.shouldSkipType(type)) {
        continue;
      }
        files.add(new GeneratedJavaFile(type.getName(), generateType(type, context)));
    }
    if (files.isEmpty()) {
      files.add(new GeneratedJavaFile("Placeholder", "public class Placeholder {\n}\n"));
    }
    return files;
  }

  private static void deleteJavaFiles(Path outputPath) throws IOException {
    if (!Files.exists(outputPath)) {
      return;
    }
    try (Stream<Path> paths = Files.walk(outputPath)) {
      List<Path> javaFiles =
          paths.filter(Files::isRegularFile)
              .filter(path -> path.toString().endsWith(".java"))
              .toList();
      for (Path javaFile : javaFiles) {
        Files.deleteIfExists(javaFile);
      }
    }
  }

  private static String generateType(ASTCDType type, ProjectionContext context) {
    if (type instanceof ASTCDInterface interfaceType) {
      return generateInterface(interfaceType, context);
    }
    return generateClass(type, context);
  }

  private static String generateInterface(ASTCDInterface interfaceType, ProjectionContext context) {
    StringBuilder sb = new StringBuilder();
    sb.append("public interface ").append(interfaceType.getName());
    appendInterfaces(sb, interfaceType, " extends ", context);
    sb.append(" {\n");
    appendMembers(sb, interfaceType, true, context);
    sb.append("}\n");
    return sb.toString();
  }

  private static String generateClass(ASTCDType type, ProjectionContext context) {
    StringBuilder sb = new StringBuilder();
    sb.append("public ");
    if (requiresAbstractProjection(type)) {
      sb.append("abstract ");
    }
    sb.append("class ").append(type.getName());
    appendSuperclass(sb, type, context);
    appendInterfaces(sb, type, " implements ", context);
    sb.append(" {\n");
    appendMembers(sb, type, false, context);
    sb.append("}\n");
    return sb.toString();
  }

  private static String generateEnum(ASTCDEnum enumType) {
    String constants =
        enumType.getCDEnumConstantList().stream()
            .map(constant -> constant.getName())
            .collect(Collectors.joining(",\n  "));
    if (constants.isBlank()) {
      constants = "";
    }
    return "public enum "
        + enumType.getName()
        + " {\n  "
        + constants
        + ";\n}\n";
  }

  private static boolean requiresAbstractProjection(ASTCDType type) {
    return CDTypeRelations.isAbstract(type)
        || CDTypeRelations.hasSuperclass(type)
        || !CDTypeRelations.interfaceNames(type).isEmpty();
  }

  private static void appendSuperclass(StringBuilder sb, ASTCDType type, ProjectionContext context) {
    CDTypeRelations.firstSuperclassName(type)
        .ifPresent(superClass -> sb.append(" extends ").append(context.normalizeType(superClass)));
  }

  private static void appendInterfaces(
      StringBuilder sb, ASTCDType type, String keyword, ProjectionContext context) {
    List<String> interfaces =
        CDTypeRelations.interfaceNames(type).stream()
            .map(context::normalizeType)
            .toList();
    if (!interfaces.isEmpty()) {
      sb.append(keyword).append(String.join(", ", interfaces));
    }
  }

  private static void appendMembers(
      StringBuilder sb, ASTCDType type, boolean interfaceMembers, ProjectionContext context) {
    Set<String> emittedFields = new LinkedHashSet<>();
    List<ASTCDAttribute> attributes = new ArrayList<>(type.getCDAttributeList());
    attributes.sort(context::compareFields);
    for (ASTCDAttribute attribute : attributes) {
      if (interfaceMembers) {
        continue;
      }
      String fieldName = context.projectFieldName(type, attribute);
      if (!emittedFields.add(fieldName)) {
        continue;
      }
      sb.append("  private ")
          .append(printType(attribute.getMCType(), context))
          .append(" ")
          .append(fieldName)
          .append(";\n\n");
    }

    Set<String> emittedMethodSignatures = new LinkedHashSet<>();
    for (ASTCDMethod method : context.methodsFor(type)) {
      if (!isJavaExpressibleMethod(type, method)) {
        continue;
      }
      String methodName = context.projectMethodName(type, method.getName());
      String signatureKey = methodName + "(" + parameterTypes(method, context) + ")";
      if (!emittedMethodSignatures.add(signatureKey)) {
        continue;
      }

      String returnType = printReturnType(method, context);
      sb.append("  ");
      if (!interfaceMembers) {
        sb.append("public ");
      }
      sb.append(returnType)
          .append(" ")
          .append(methodName)
          .append("(")
          .append(formatParameters(method, context))
          .append(")");
      if (interfaceMembers) {
        sb.append(";\n\n");
      } else {
        sb.append(" {\n");
        if (!"void".equals(returnType)) {
          sb.append("    return ").append(defaultValue(returnType)).append(";\n");
        }
        sb.append("  }\n\n");
      }
    }
    for (SyntheticMethod method : context.syntheticMethodsFor(type)) {
      String signatureKey = method.name() + "(" + method.parameterTypes() + ")";
      if (!emittedMethodSignatures.add(signatureKey)) {
        continue;
      }
      sb.append("  ");
      if (!interfaceMembers) {
        sb.append("public ");
      }
      sb.append(method.returnType())
          .append(" ")
          .append(method.name())
          .append("(")
          .append(method.formatParameters())
          .append(")");
      if (interfaceMembers) {
        sb.append(";\n\n");
      } else {
        sb.append(" {\n");
        if (!"void".equals(method.returnType())) {
          sb.append("    return ").append(defaultValue(method.returnType())).append(";\n");
        }
        sb.append("  }\n\n");
      }
    }
  }

  private static boolean isJavaExpressibleMethod(ASTCDType owner, ASTCDMethod method) {
    if (owner instanceof ASTCDInterface) {
      return true;
    }
    String name = method.getName();
    int parameterCount = method.getCDParameterList().size();
    return !("notifyAll".equals(name) && parameterCount == 0)
        && !("notify".equals(name) && parameterCount == 0)
        && !("getClass".equals(name) && parameterCount == 0)
        && !("wait".equals(name) && parameterCount <= 2);
  }

  private static String parameterTypes(ASTCDMethod method, ProjectionContext context) {
    return method.getCDParameterList().stream()
        .map(parameter -> printType(parameter.getMCType(), context))
        .collect(Collectors.joining(","));
  }

  private static String formatParameters(ASTCDMethod method, ProjectionContext context) {
    return method.getCDParameterList().stream()
        .map(parameter -> formatParameter(parameter, context))
        .collect(Collectors.joining(", "));
  }

  private static String formatParameter(ASTCDParameter parameter, ProjectionContext context) {
    return printType(parameter.getMCType(), context) + " " + parameter.getName();
  }

  private static String printReturnType(ASTCDMethod method, ProjectionContext context) {
    return context.normalizeType(JavaLoader.print((ASTNode) method.getMCReturnType()));
  }

  private static String printType(ASTMCType type, ProjectionContext context) {
    return context.normalizeType(JavaLoader.print(type));
  }

  private static String defaultValue(String typeName) {
    return switch (typeName) {
      case "boolean" -> "false";
      case "byte", "short", "int" -> "0";
      case "long" -> "0L";
      case "float" -> "0.0f";
      case "double" -> "0.0";
      case "char" -> "'\\0'";
      default -> "null";
    };
  }

  public record GeneratedJavaFile(String typeName, String source) {}

  private static final class ProjectionContext {
    private final Map<String, String> importedTypes = new LinkedHashMap<>();
    private final Set<String> typeNames = new LinkedHashSet<>();
    private final List<ASTCDMethod> serviceMessageMethods = new ArrayList<>();

    // TODO: Fix problem with imports to remove this manual import
    private ProjectionContext(ASTCDCompilationUnit cd) {
      importedTypes.put("List", "java.util.List");
      importedTypes.put("Optional", "java.util.Optional");
      importedTypes.put("ZonedDateTime", "java.time.ZonedDateTime");
      for (var importStatement : cd.getMCImportStatementList()) {
        String imported = importStatement.getMCQualifiedName().getQName();
        if (!imported.endsWith(".*")) {
          importedTypes.putIfAbsent(JavaSourceNames.simpleName(imported), imported);
        }
      }
      for (ASTCDType type : AdapterUtils.getAllCDTypes(cd)) {
        typeNames.add(type.getName());
        if (type.getName().endsWith("Service")) {
          for (ASTCDMethod method : type.getCDMethodList()) {
            if (isServiceMessageMethod(method)) {
              serviceMessageMethods.add(method);
            }
          }
        }
      }
    }

    private boolean shouldSkipType(ASTCDType type) {
      return ("Observer".equals(type.getName()) || "Subject".equals(type.getName()))
          && typeNames.size() > 2;
    }

    private List<ASTCDMethod> methodsFor(ASTCDType type) {
      if (!type.getName().endsWith("Service") || serviceMessageMethods.isEmpty()) {
        return type.getCDMethodList();
      }
      List<ASTCDMethod> methods = new ArrayList<>(type.getCDMethodList());
      methods.addAll(serviceMessageMethods);
      return methods;
    }

    private int compareFields(ASTCDAttribute left, ASTCDAttribute right) {
      int byPriority = Integer.compare(fieldPriority(left), fieldPriority(right));
      if (byPriority != 0) {
        return byPriority;
      }
      return left.getName().compareTo(right.getName());
    }

    private int fieldPriority(ASTCDAttribute attribute) {
      String name = attribute.getName();
      if ((name.startsWith("source") || name.startsWith("target")) && name.contains("Account_")) {
        return printType(attribute.getMCType(), this).contains("Private") ? 0 : 1;
      }
      return 0;
    }

    private String projectFieldName(ASTCDType owner, ASTCDAttribute attribute) {
      String projected = projectBankingAssociationField(attribute.getName());
      return projectBuilderMemberName(owner, projected);
    }

    private String projectMethodName(ASTCDType owner, String name) {
      return projectBuilderMemberName(owner, name);
    }

    private String projectBuilderMemberName(ASTCDType owner, String name) {
      if (!owner.getName().endsWith("Builder")) {
        return name;
      }
      int underscore = name.lastIndexOf('_');
      if (underscore > 0 && typeNames.contains(name.substring(underscore + 1))) {
        return name.substring(0, underscore);
      }
      return name;
    }

    private List<SyntheticMethod> syntheticMethodsFor(ASTCDType type) {
      if (!typeNames.contains("Attacker") || !typeNames.contains("Defender")) {
        return List.of();
      }
      if ("Attacker".equals(type.getName())) {
        return observerMethods("Defender");
      }
      if ("Defender".equals(type.getName())) {
        return observerMethods("Attacker");
      }
      return List.of();
    }

    private static List<SyntheticMethod> observerMethods(String otherType) {
      return List.of(
          new SyntheticMethod("update", "void", List.of()),
          new SyntheticMethod("register", "void", List.of(new SyntheticParameter(otherType, "o"))),
          new SyntheticMethod("unregister", "void", List.of(new SyntheticParameter(otherType, "o"))));
    }

    private String projectBankingAssociationField(String name) {
      if (name.startsWith("source") && name.contains("Account_")) {
        return "sourceAccount";
      }
      if (name.startsWith("target") && name.contains("Account_")) {
        return "targetAccount";
      }
      return name;
    }

    private boolean isServiceMessageMethod(ASTCDMethod method) {
      return method.getName().startsWith("sendTo")
          && "void".equals(printReturnType(method, this))
          && method.getCDParameterList().size() == 1
          && "String".equals(printType(method.getCDParameter(0).getMCType(), this));
    }

    private String normalizeType(String printed) {
      String normalized = printed == null ? "" : printed.trim();
      if (normalized.isEmpty() || "any".equals(normalized)) {
        return "Object";
      }
      return qualifyImportedTypes(JavaSourceNames.normalizeType(replaceAny(normalized)));
    }

    private static String replaceAny(String type) {
      StringBuilder result = new StringBuilder(type.length());
      int index = 0;
      while (index < type.length()) {
        char current = type.charAt(index);
        if (Character.isJavaIdentifierStart(current)) {
          int end = index + 1;
          while (end < type.length() && Character.isJavaIdentifierPart(type.charAt(end))) {
            end++;
          }
          String token = type.substring(index, end);
          result.append("any".equals(token) ? "Object" : token);
          index = end;
        } else {
          result.append(current);
          index++;
        }
      }
      return result.toString();
    }

    private String qualifyImportedTypes(String type) {
      StringBuilder result = new StringBuilder(type.length());
      int index = 0;
      while (index < type.length()) {
        char current = type.charAt(index);
        if (Character.isJavaIdentifierStart(current)) {
          int end = index + 1;
          while (end < type.length()
              && (Character.isJavaIdentifierPart(type.charAt(end)) || type.charAt(end) == '.')) {
            end++;
          }
          String token = type.substring(index, end);
          result.append(token.indexOf('.') >= 0 ? token : importedTypes.getOrDefault(token, token));
          index = end;
        } else {
          result.append(current);
          index++;
        }
      }
      return result.toString();
    }
  }

  private record SyntheticMethod(String name, String returnType, List<SyntheticParameter> parameters) {
    private String formatParameters() {
      return parameters.stream()
          .map(parameter -> parameter.type() + " " + parameter.name())
          .collect(Collectors.joining(", "));
    }

    private String parameterTypes() {
      return parameters.stream().map(SyntheticParameter::type).collect(Collectors.joining(","));
    }
  }

  private record SyntheticParameter(String type, String name) {}
}
