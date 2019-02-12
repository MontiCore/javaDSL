/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.types;

import de.monticore.expressions.commonexpressions._ast.ASTCallExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.javaclassexpressions._ast.ASTGenericInvocationExpression;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.monticore.symboltable.types.TypeSymbol;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.symboltable.types.references.TypeReference;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.*;

/**
 * TODO
 *
 * @author (last commit) $$Author$$
 * @since TODO
 */
public class JavaDSLHelper {


  public static boolean isString(JavaTypeSymbolReference type) {
    if(type.getName().equals("String") || type.getName().equals("java.lang.String")){
      return true;
    }
    return false;
  }

  /**
   * Resolves type for expressions
   * @param firstOperand type of the first operand
   * @param secondOperand type of the second operand
   * @param operation the operation String
   */
  public static Optional<JavaTypeSymbolReference> resolveTypeForExpressions(
      JavaTypeSymbolReference firstOperand, JavaTypeSymbolReference secondOperand,
      String operation) {
    if ("+".equals(operation) && (isString(firstOperand) || isString(secondOperand))) {
      return Optional.of(new JavaTypeSymbolReference("String",
          firstOperand.getEnclosingScope(), 0));
    }
    if ("+".equals(operation) || "-".equals(operation) || "multiplicativeOp".equals(operation)) {
      if (isNumericType(firstOperand) && isNumericType(secondOperand)) {
        return Optional.of(getBinaryNumericPromotion(unbox(firstOperand), unbox(secondOperand)));
      }
    }
    if ("<<".equals(operation)) {
      if (isIntegralType(firstOperand) && isIntegralType(secondOperand)) {
        return Optional.of(new JavaTypeSymbolReference("int", firstOperand.getEnclosingScope(), 0));
      }
    }
    if (">>".equals(operation)) {
      if (isIntegralType(firstOperand) && isIntegralType(secondOperand)) {
        return Optional.of(new JavaTypeSymbolReference("int", firstOperand.getEnclosingScope(), 0));
      }
    }
    if (">>>".equals(operation)) {
      if (isIntegralType(firstOperand) && isIntegralType(secondOperand)) {
        return Optional.of(new JavaTypeSymbolReference("int", firstOperand.getEnclosingScope(), 0));
      }
    }

    if ("comparison".equals(operation)) {
      if (isNumericType(firstOperand) && isNumericType(secondOperand)) {
        JavaTypeSymbolReference booleanType = new JavaTypeSymbolReference("boolean",
            firstOperand.getEnclosingScope(), 0);
        return Optional.of(booleanType);
      }
    }
    if ("identityTest".equals(operation)) {
      JavaTypeSymbolReference booleanType = new JavaTypeSymbolReference("boolean",
          firstOperand.getEnclosingScope(), 0);
      if (isNumericType(firstOperand) && isNumericType(secondOperand)) {
        return Optional.of(booleanType);
      }
      if (isBooleanType(firstOperand) && isBooleanType(secondOperand)) {
        return Optional.of(booleanType);
      }
      if (anIdentityConversionAvailable(firstOperand, secondOperand)) {
        return Optional.of(booleanType);
      }
      if (isSuperType(firstOperand, secondOperand) || isSuperType(secondOperand, firstOperand)) {
        return Optional.of(booleanType);
      }
    }
    if ("bitwiseOp".equals(operation)) {
      if (isIntegralType(firstOperand) && isIntegralType(secondOperand)) {
        return Optional.of(getBinaryNumericPromotion(unbox(firstOperand), unbox(secondOperand)));
      }
      if (isBooleanType(firstOperand) && isBooleanType(secondOperand)) {
        return Optional
            .of(new JavaTypeSymbolReference("boolean", firstOperand.getEnclosingScope(), 0));
      }
    }
    if ("booleanOp".equals(operation)) {
      if (isBooleanType(firstOperand) && isBooleanType(secondOperand)) {
        return Optional.of(unbox(firstOperand));
      }
    }
    if ("typeCast".equals(operation)) {
      if (wideningPrimitiveConversionAvailable(firstOperand, secondOperand)
          || narrowingPrimitiveConversionAvailable(firstOperand, secondOperand)
          || wideningReferenceConversionAvailable(firstOperand, secondOperand)
          || narrowingReferenceConversionAvailable(firstOperand, secondOperand)) {
        return Optional.of(firstOperand);
      }
      else {
        return Optional.of(secondOperand);
      }
    }
    if ("instanceOf".equals(operation)) {
      if ("null".equals(firstOperand.getName()) || !isPrimitiveType(firstOperand)) {
        if (isReifiableType(secondOperand) && !isPrimitiveType(secondOperand)) {
          if (isSubType(firstOperand, secondOperand)) {
            return Optional
                .of(new JavaTypeSymbolReference("boolean", firstOperand.getEnclosingScope(), 0));
          }
        }
      }
    }
    if ("condition".equals(operation)) {
      if (anIdentityConversionAvailable(firstOperand, secondOperand)) {
        return Optional.of(firstOperand);
      }
      if (isBooleanType(firstOperand) && isBooleanType(secondOperand)) {
        return Optional
            .of(new JavaTypeSymbolReference("boolean", firstOperand.getEnclosingScope(), 0));
      }
      if (isNullType(firstOperand) && !isNullType(secondOperand)) {
        return Optional.of(box(secondOperand));
      }
      if (!isNullType(firstOperand) && isNullType(secondOperand)) {
        return Optional.of(box(firstOperand));
      }
      if (isShortType(firstOperand) && isByteType(secondOperand)) {
        return Optional.of(firstOperand);
      }
      if (isByteType(firstOperand) || isShortType(secondOperand)) {
        return Optional.of(secondOperand);
      }
      if (isIntType(firstOperand) && "int".equals(getUnaryNumericPromotionType(secondOperand).getName())) {
        return Optional.of(firstOperand);
      }
      if (isIntType(secondOperand) && "int".equals(getUnaryNumericPromotionType(firstOperand).getName())) {
        return Optional.of(secondOperand);
      }
      if (isNumericType(firstOperand) && isNumericType(secondOperand)) {
        return Optional.of(getBinaryNumericPromotion(firstOperand, secondOperand));
      }
      if(!isPrimitiveType(box(firstOperand)) && !isPrimitiveType(box(secondOperand))) {
        Set<JavaTypeSymbolReference> set = new HashSet<>();
        set.add(box(firstOperand));
        set.add(box(firstOperand));
        return Optional.of(lub(set));
      }
    }
    return Optional.empty();
  }

  /**
   *
   * @param methodSymbol
   * @return list of the formal parameter types
   */
  public static List<JavaTypeSymbolReference> getParameterTypes (JavaMethodSymbol methodSymbol) {
    List<JavaTypeSymbolReference> parameters = new ArrayList<>();
    for(JavaFieldSymbol fieldSymbol : methodSymbol.getParameters()) {
     JavaTypeSymbolReference type = convertToWildCard(fieldSymbol.getType());
     String completeName = getCompleteName(type);
     JavaTypeSymbolReference completeType = new JavaTypeSymbolReference(completeName,
         type.getEnclosingScope(), type.getDimension());
      completeType.setActualTypeArguments(type.getActualTypeArguments());
      parameters.add(completeType);
    }
    return parameters;
  }

  /**
   *
   * @param type
   * @return the type, that is converted to wildCard.
   * as in the type system wildcard is named as ASTWildcardType but in the symbol creation
   * only unbounded wildcard is created as ?, the upper and lower bounded are created as bounds.
   * thus, there is a need to convert wilcard to how it is expected in the type system
   */
  public static JavaTypeSymbolReference convertToWildCard(JavaTypeSymbolReference type) {
    String completeName = getCompleteName(type);
    JavaTypeSymbolReference returnType = new JavaTypeSymbolReference(completeName,
        type.getEnclosingScope(), type.getDimension());
    List<ActualTypeArgument> arguments = new ArrayList<>();
    for(ActualTypeArgument actualTypeArgument : type.getActualTypeArguments()) {
      if("?".equals(actualTypeArgument.getType().getName())) {
        JavaTypeSymbolReference wildcard = new JavaTypeSymbolReference("ASTWildcardType", actualTypeArgument.getType().getEnclosingScope(), 0);
        arguments.add(new ActualTypeArgument(false, false, wildcard));
      }
      else if(actualTypeArgument.isUpperBound() || actualTypeArgument.isLowerBound()) {
        List<ActualTypeArgument> bounds = new ArrayList<>();
        JavaTypeSymbolReference wildcard = new JavaTypeSymbolReference("ASTWildcardType", actualTypeArgument.getType().getEnclosingScope(), 0);
        JavaTypeSymbolReference bound = new JavaTypeSymbolReference(actualTypeArgument.getType().getName(),
            actualTypeArgument.getType().getEnclosingScope(), actualTypeArgument.getType().getDimension());
        bound.setActualTypeArguments(actualTypeArgument.getType().getActualTypeArguments());
        bounds.add(new ActualTypeArgument(actualTypeArgument.isLowerBound(), actualTypeArgument.isUpperBound(), bound));
        wildcard.setActualTypeArguments(bounds);
        arguments.add(new ActualTypeArgument(false, false, wildcard));
      }
      else {
        ActualTypeArgument arg = new ActualTypeArgument(actualTypeArgument.isLowerBound(),
            actualTypeArgument.isUpperBound(), convertToWildCard((JavaTypeSymbolReference) actualTypeArgument.getType()));
        arguments.add(arg);
      }
    }
    returnType.setActualTypeArguments(arguments);
    return returnType;
  }

  /**
   *
   * @param leftType
   * @param rightType
   * @return if the given two types are equal
   */
  public static boolean areEqual(JavaTypeSymbolReference leftType,
      JavaTypeSymbolReference rightType) {
    if (!areEqualWithoutArgs(leftType, rightType)) {
      return false;
    }
    return areEqualWithArgs(leftType, rightType);
  }

  /**
   *
   * @param a a type variable
   * @param b a type variable
   * @return returns if the two type variables are equal
   */
  public static boolean areEqual(TypeReference<? extends TypeSymbol> a,
      TypeReference<? extends TypeSymbol> b) {
    if (a == null || b == null) {
      return false;
    }
    if (a == b) {
      return true;
    }
    if (!a.getName().equals(b.getName())) {
      return false;
    }
    if ((getLowerBound(a) == null && getLowerBound(b) == null) || areEqual(getLowerBound(a),
        getLowerBound(b))) {
      if (getUpperBounds(a).size() == getUpperBounds(b).size()) {
        List<JavaTypeSymbolReference> listA = getUpperBounds(a);
        List<JavaTypeSymbolReference> listB = getUpperBounds(b);
        if (listA.isEmpty() && listB.isEmpty()) {
          if (areEqual((JavaTypeSymbolReference) a, (JavaTypeSymbolReference) b)) {
            return true;
          }
        }
        for (int i = 0; i < listA.size(); i++) {
          if (areEqual(listA.get(i), listB.get(i))) {
            if (i == listA.size() - 1) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  /**
   *
   * @param leftType
   * @param rightType
   * @return returns if the given two types are equal inclusive the actual type arguments
   */
  public static boolean areEqualWithArgs(JavaTypeSymbolReference leftType,
      JavaTypeSymbolReference rightType) {
    if (leftType == rightType) {
      return true;
    }
    if (leftType == null || rightType == null) {
      return false;
    }
    if (leftType.getActualTypeArguments().size() != rightType.getActualTypeArguments().size()) {
      return false;
    }
    if (leftType.getDimension() != rightType.getDimension()) {
      return false;
    }
    for (int i = 0; i < leftType.getActualTypeArguments().size(); i++) {
      JavaTypeSymbolReference leftArg = box((JavaTypeSymbolReference) leftType.getActualTypeArguments()
          .get(i).getType());
      JavaTypeSymbolReference rightArg = box((JavaTypeSymbolReference) rightType
          .getActualTypeArguments().get(i).getType());
      if (!areEqual(leftArg, rightArg)) {
        return false;
      }
    }
    return true;
  }

  /**
   *
   * @param leftType
   * @param rightType
   * @return returns if given two types are equal exclusive the actual type arguments
   */
  public static boolean areEqualWithoutArgs(JavaTypeSymbolReference leftType,
      JavaTypeSymbolReference rightType) {
    if (leftType == null || rightType == null) {
      return false;
    }
    if (leftType.getDimension() != rightType.getDimension()) {
      return false;
    }
    String leftComplete = getCompleteName(leftType);
    String rightComplete = getCompleteName(rightType);
    return leftComplete.equals(rightComplete);
  }

  /**
   *
   * @param type
   * @return returns the complete name, this is useful for example Map.Entry
   * to resolve this symbol, first Map has to be resolved and then Entry has to be resolved
   * and for such cases, this method returns the complete name java.util.Map.Entry
   */
  public static String getCompleteName(JavaTypeSymbolReference type) {
    if(!isPrimitiveType(type) && !"?".equals(type.getName())) {
      if(type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
        JavaTypeSymbol symbol = (JavaTypeSymbol)type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).get();
        if(symbol.isTypeVariable()) {
          return type.getName();
        }
        else {
          String fullName = symbol.getFullName();
          return fullName;
        }
      }
      if(type.getName().contains(".")) {
          String[] splitted = type.getName().split("\\.");
          String typeName = splitted[splitted.length - 1];
          String outerTypeName = "";
          for(int i = 0 ; i < splitted.length - 1 ; i ++) {
            if(outerTypeName.length()==0) {
              outerTypeName = splitted[i];
            }
            else {
              outerTypeName = outerTypeName + "."+splitted[i];
            }
          }
          if(type.getEnclosingScope().resolve(outerTypeName, JavaTypeSymbol.KIND).isPresent()) {
            JavaTypeSymbol outerSymbol = (JavaTypeSymbol) type.getEnclosingScope().resolve(outerTypeName, JavaTypeSymbol.KIND).get();
            if(outerTypeName.equals(outerSymbol.getFullName())) {
              return outerTypeName;
            }
            else {
              String packageName = outerSymbol.getPackageName();
              return packageName + "." + outerTypeName + "." +typeName;
            }
          }
      }
    }
    return type.getName();
  }

  /**
   *
   * @param type
   * @return the type, on which the unary numeric promotion has been applied
   */
  public  static JavaTypeSymbolReference getUnaryNumericPromotionType(JavaTypeSymbolReference type) {
    if ("byte".equals(unbox(type).getName()) ||
        "short".equals(unbox(type).getName()) ||
            "char".equals(unbox(type).getName()) ||
                "int".equals(unbox(type).getName())) {
      return new JavaTypeSymbolReference("int", type.getEnclosingScope(), 0);
    }
    if ("long".equals(unbox(type).getName()) ||
        "double".equals(unbox(type).getName()) ||
            "float".equals(unbox(type).getName())) {
      return unbox(type);
    }
    return type;
  }

  /**
   *
   * @param leftType
   * @param rightType
   * @return returns the result type by applying binary numeric promotion on the given
   * two types
   */
  public static JavaTypeSymbolReference getBinaryNumericPromotion(JavaTypeSymbolReference leftType,
      JavaTypeSymbolReference rightType) {
    if ("double".equals(leftType.getName()) || "double".equals(rightType.getName())) {
      return new JavaTypeSymbolReference("double", leftType.getEnclosingScope(), 0);
    }
    if ("float".equals(leftType.getName()) || "float".equals(rightType.getName())) {
      return new JavaTypeSymbolReference("float", leftType.getEnclosingScope(), 0);
    }
    if ("long".equals(leftType.getName()) || "long".equals(rightType.getName())) {
      return new JavaTypeSymbolReference("long", leftType.getEnclosingScope(), 0);
    }
    return new JavaTypeSymbolReference("int", leftType.getEnclosingScope(), 0);

  }

  /**
   *
   * @param type
   * @return true if the given type is an integral type
   */
  public static boolean isIntegralType(JavaTypeSymbolReference type) {
    if ("int".equals(unbox(type).getName()))
      return true;
    if ("byte".equals(unbox(type).getName()))
      return true;
    if ("short".equals(unbox(type).getName()))
      return true;
    if ("long".equals(unbox(type).getName()))
      return true;
    if ("char".equals(unbox(type).getName()))
      return true;
    return false;
  }

  /**
   *
   * @param type
   * @return true if the given type is a numeric type
   */
  public static boolean isNumericType(JavaTypeSymbolReference type) {
    if (isIntegralType(type))
      return true;
    if ("float".equals(unbox(type).getName()))
      return true;
    if ("double".equals(unbox(type).getName()))
      return true;
    return false;
  }


  /**
   *
   * @param type
   * @return true if the given type is an enum
   */
  public  static boolean isEnum(JavaTypeSymbolReference type) {
    if (type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
          .resolve(type.getName(), JavaTypeSymbol.KIND).get();
      return typeSymbol.isEnum();
    }
    return false;
  }

  /**
   *
   * @param type
   * @return true if the given type is an annotation
   */
  public  static boolean isAnnotation(JavaTypeSymbolReference type) {
    if (type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
          .resolve(type.getName(), JavaTypeSymbol.KIND).get();
      return typeSymbol.isAnnotation();
    }
    return false;
  }

  /**
   *
   * @param enumType
   * @param enumConstant
   * @return true if the given enumConstant is the member of the given enumType
   */
  public  static boolean isEnumMember(JavaTypeSymbolReference enumType,
      JavaTypeSymbolReference enumConstant) {
    if (isEnum(enumType)) {
      JavaTypeSymbol enumSymbol = (JavaTypeSymbol) enumType.getEnclosingScope()
          .resolve(enumType.getName(), JavaTypeSymbol.KIND).get();
      for (JavaFieldSymbol fieldSymbol : enumSymbol.getFields()) {
        if (fieldSymbol.getName().equals(enumConstant.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * JLS-4.7
   *
   * A type is reifiable if and only if one of the following holds:
   *
   * (1) It refers to a non-generic class or interface type declaration.
   * (2) It is a parameterized type in which all type arguments are unbounded wildcards
   * (3) It is a raw type
   * (4) It is a primitive type
   * (5) It is an array type whose element type is reifiable
   * (6) It is a nested type where, for each type T separated by a ".", T itself is reifiable
   *
   * @param type
   * @return checks if the given type is a reifiable type JLS3_4.7
   */
  public  static boolean isReifiableType(JavaTypeSymbolReference type) {
    return isPrimitiveType(type) || type.getActualTypeArguments().isEmpty()
            || allArgsAreBoundlessWildCards(type.getActualTypeArguments())
            || isRawType(type);
  }

  /**
   *
   * @param type
   * @return true if the given type is either a java.lang.Cloneable or a subtype of it
   */
  public  static boolean isCloneable(JavaTypeSymbolReference type) {
    if (type.getName().equals("Cloneable") || type.getName().equals("java.lang.Cloneable")) {
      return true;
    }
    JavaTypeSymbolReference cloneable = new JavaTypeSymbolReference("java.lang.Cloneable",type.getEnclosingScope(),0);
    if(isSubType(type, cloneable)) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param type
   * @return true if the given type is java.lang.Object
   */
  public  static boolean isObjectType(JavaTypeSymbolReference type) {
    if (type.getName().equals("Object") || type.getName().equals("java.lang.Object")) {
      return true;
    }
    return false;
  }


  /**
   *
   * @param type
   * @return true if the given type is a wildcard type
   */
  public  static boolean isWildCardType(TypeReference<? extends TypeSymbol> type) {
    return type.getName().equals("ASTWildcardType");
  }

  /**
   *
   * @param list
   * @return true, if all actual type arguments are boundless wildcards
   */
  public static  boolean allArgsAreBoundlessWildCards(List<ActualTypeArgument> list) {
    int counter = 0;
    if (!list.isEmpty()) {
      for (ActualTypeArgument actualTypeArgument : list) {
        if (isBoundlessWildCardType(actualTypeArgument.getType())) {
          counter = counter + 1;
        }
      }
      if (counter == list.size()) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param typeArg
   * @return true if the given type argument is a boundless wildcard type
   */
  public  static boolean isBoundlessWildCardType(TypeReference<? extends TypeSymbol> typeArg) {
    JavaTypeSymbolReference type = (JavaTypeSymbolReference) typeArg;
    if (type.getName().equals("ASTWildcardType")) {
      return type.getActualTypeArguments().isEmpty();
    }
    return false;
  }


  /**
   *
   * @param type
   * @return true if the given type is a java.io.Serializable or a subtype of it
   */
  public  static boolean isSerializable(JavaTypeSymbolReference type) {
    if (type.getName().equals("java.io.Serializable") || type.getName().equals("Serializable")) {
      return true;
    }
    JavaTypeSymbolReference ser = new JavaTypeSymbolReference("java.io.Serializable", type.getEnclosingScope(), 0);
    if(isSubType(type, ser)) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param type
   * @return true if the given type is a boolean
   */
  public static boolean isBooleanType(JavaTypeSymbolReference type) {
    return "boolean".equals(unbox(type).getName());
  }

  /**
   *
   * @param type
   * @return true if the given type is a char type
   */
  public static boolean isCharType(JavaTypeSymbolReference type) {
    return "char".equals(unbox(type).getName());
  }

  /**
   *
   * @param type
   * @return true if the given type is a null type
   */
  public static boolean isNullType(JavaTypeSymbolReference type) {
    return type.getName().equals("null");
  }

  /**
   *
   * @param type
   * @return true if the given type is a byte
   */
  public static boolean isByteType(JavaTypeSymbolReference type) {
    return "byte".equals(unbox(type).getName());
  }

  /**
   *
   * @param type
   * @return true if the given type is an int type
   */
  public static boolean isIntType(JavaTypeSymbolReference type) {
    return "int".equals(unbox(type).getName());
  }

  /**
   *
   * @param type
   * @return true if the given type is a short type
   */
  public static boolean isShortType(JavaTypeSymbolReference type) {
    return "short".equals(unbox(type).getName());
  }

  /**
   *
   * @param type
   * @return true if the given type is a double type
   */
  public static boolean isDouble(JavaTypeSymbolReference type) {
    return "double".equals(unbox(type).getName());
  }

  /**
   *
   * @param type
   * @return true if the given type is a float type
   */
  public static boolean isFloat(JavaTypeSymbolReference type) {
    return "float".equals(unbox(type).getName());
  }

  /**
   *
   * @param type
   * @return true, if the given type is a long type
   */
  public static boolean isLong(JavaTypeSymbolReference type) {
    return "long".equals(unbox(type).getName());
  }

  /**
   *
   * @param type
   * @return true if the given type is a void type
   */
  public static boolean isVoidType(JavaTypeSymbolReference type) {
    return "void".equals(type.getName());
  }

  /**
   *
   * @param type
   * @return returns true if the given type is java.lang.Iterable or a subtype of it
   */
  public static boolean isIterableType(JavaTypeSymbolReference type) {
    if(type.getName().equals("Iterable") || type.getName().equals("java.lang.Iterable")){
      return true;
    }
    else {
      JavaTypeSymbolReference iterable = new JavaTypeSymbolReference("java.lang.Iterable", type.getEnclosingScope(),0);
      if(isSubType(type, iterable)) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param type
   * @return true if the given type is a java.lang.Throwable or a subtype of it
   */
  public  static boolean isThrowable(JavaTypeSymbolReference type) {
    if (type.getName().equals("Throwable") || type.getName().equals("java.lang.Throwable")) {
      return true;
    }
    JavaTypeSymbolReference throwable = new JavaTypeSymbolReference("java.lang.Throwable", type.getEnclosingScope(), 0);
    if(isSubType(type, throwable)) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param type
   * @return true if the given type is a primitive type
   */
  public static boolean isPrimitiveType(JavaTypeSymbolReference type) {
    List<String> primitiveTypes = Arrays
        .asList("boolean", "byte", "char", "short", "int", "long", "float", "double");
    if (type != null && type.getDimension() == 0) {
      return primitiveTypes.contains(type.getName());
    }
    return false;
  }

  /**
   *
   * @param methodSymbol
   * @return true if the given method is a package access method, called default in later versions of java
   */
  public  static boolean isDefaultMethod(JavaMethodSymbol methodSymbol) {
    if (methodSymbol.isPublic() || methodSymbol.isPrivate() || methodSymbol.isProtected()) {
      return false;
    }
    return true;
  }

  /**
   *
   * @param node
   * @return false if the given expression is invoking a method
   */
  public  static boolean isVariable(ASTExpression node) {
    if(node instanceof ASTCallExpression) {
      return false;
    }
    if(node instanceof ASTGenericInvocationExpression) {
      return false;
    }
    return true;
  }

  /**
   *
   * @param typeSymbol
   * @return all inner types of the given type symbol, searches recursively, for example:
   * includes an inner type of the inner type
   */
  public  static List<JavaTypeSymbol> getAllInnerTypes(JavaTypeSymbol typeSymbol) {
    List<JavaTypeSymbol> list = new ArrayList<>();
    list.addAll(typeSymbol.getInnerTypes());
    for (JavaTypeSymbol innerType : typeSymbol.getInnerTypes()) {
      list.addAll(getAllInnerTypes(innerType));
    }
    return list;
  }

  /**
   *
   * @param classMethod
   * @param superType super type of the type which contains the class method
   * @return checks for the given method of class if there exists another method in
   * the given supertype or in supertypes of the given supertype,
   * which has different signature but same erasure as the given method. If such method is found then
   * the found method is returned with the supertype, in which it was found.
   * The reason for returning the supertype is to improve the error message.
   */
  public  static Optional<List<Symbol>> methodDifferentSignatureAndSameErasure(JavaMethodSymbol classMethod,
      JavaTypeSymbolReference superType) {
    if (superType.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
        .isPresent()) {
      JavaTypeSymbol superSymbol = (JavaTypeSymbol) superType.getEnclosingScope()
          .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
      for (JavaMethodSymbol superMethod : superSymbol.getMethods()) {
        if (classMethod.getName().equals(superMethod.getName())) {
          List<JavaTypeSymbolReference> classList = getSubstitutedFormalParameterTypes(classMethod);
          List<JavaTypeSymbolReference> superList = getSubstitutedFormalParameterTypes(superMethod);
          if (haveSameErasures(classList, superList)) {
            List<JavaTypeSymbolReference> subParameters = getSubstitutedFormalParameters(superType,
                getParameterTypes(superMethod));
            if (!isSubSignature(classMethod, superMethod, subParameters)) {
              List<Symbol> result = new ArrayList<>();
              result.add(superSymbol);
              result.add(superMethod);
              return Optional.of(result);
            }
          }
        }
      }
      List<JavaTypeSymbolReference> references = getReferencedSuperTypes(superSymbol);
      for (JavaTypeSymbolReference supType : references) {
        if (methodDifferentSignatureAndSameErasure(classMethod, supType).isPresent()) {
          return methodDifferentSignatureAndSameErasure(classMethod, supType);
        }
      }
    }
    return Optional.empty();
  }


  /**
   *
   * @param methodSymbol
   * @param superType
   * @return checks if the given method overrides another method in the given supertype
   * or in supertypes of the given supertype. If an overridden method is found, then
   * the method is returned with the supertype, in which the method was found. The reason for
   * returning the supertype is to improve the error message.
   */
  public  static Optional<List<Symbol>> overriddenMethodFoundInSuperTypes(JavaMethodSymbol methodSymbol,
      JavaTypeSymbolReference superType) {
    if (superType.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
        .isPresent()) {
      JavaTypeSymbol superSymbol = (JavaTypeSymbol) superType.getEnclosingScope()
          .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
      for (JavaMethodSymbol superMethod : superSymbol.getMethods()) {
        List<JavaTypeSymbolReference> formalParameters = getParameterTypes(superMethod);
        if (methodSymbol.getName().equals(superMethod.getName())) {
          List<JavaTypeSymbolReference> parameters = getSubstitutedFormalParameters(superType,
              formalParameters);
          if (overrides(methodSymbol, superMethod, parameters)) {
            List<Symbol> list = new ArrayList<>();
            list.add(superSymbol);
            list.add(superMethod);
            return Optional.of(list);
          }
        }
      }
      for (JavaTypeSymbolReference supType : superSymbol.getSuperTypes()) {
        if (overriddenMethodFoundInSuperTypes(methodSymbol, supType).isPresent()) {
          return overriddenMethodFoundInSuperTypes(methodSymbol, supType);
        }
      }
    }
    return Optional.empty();
  }


  /**
   *
   * @param superType a type which contains the method
   * @param formalParameter formal parameters of a method
   * @return if the given supertype is an parametrized type and the given method formal parameters
   * involves a type parameter, then formal parameters which involves a type parameter is substituted
   * by the actual type arguments of the supertype.
   */
  public  static List<JavaTypeSymbolReference> getSubstitutedFormalParameters(JavaTypeSymbolReference superType, List<JavaTypeSymbolReference> formalParameter) {
    List<JavaTypeSymbolReference> result = new ArrayList<>();
    JavaTypeSymbol superSymbol = (JavaTypeSymbol) superType.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND).get();
    HashMap<String, JavaTypeSymbolReference> substituted = getSubstitutedTypes(superSymbol,
        superType);
    for (JavaTypeSymbolReference type : formalParameter) {
        result.add(applyTypeSubstitution(substituted, type));
      }
    return result;
  }

  /**
   *
   * @param superType
   * @param returnType
   * @return if the given supertype is an parametrized type and the given method return type
   * involves a type parameter, then the return type  is substituted
   * by the actual type arguments of the supertype.
   */
  public  static JavaTypeSymbolReference getSubstitutedReturnType(JavaTypeSymbolReference superType,
      JavaTypeSymbolReference returnType) {
    if(superType != null) {
      if (!superType.getActualTypeArguments().isEmpty()) {
        if (superType.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol superSymbol = (JavaTypeSymbol) superType.getEnclosingScope()
              .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
          HashMap<String, JavaTypeSymbolReference> substituted = getSubstitutedTypes(superSymbol,
              superType);
          return applyTypeSubstitution(substituted, returnType);
        }
      }
    }
    return returnType;
  }

  /**
   *
   * @param substituted contains the name of the type parameter and the corresponding actual type arguments
   * @param type a type to be substituted
   * @return a type, by substituting the given type from the substituted. If the given type name
   * or actual type argument contains a type parameter and if the type parameter is contained in
   * the substituted then it is substituted
   */
  public  static JavaTypeSymbolReference applyTypeSubstitution(
      HashMap<String, JavaTypeSymbolReference> substituted, JavaTypeSymbolReference type) {
    List<ActualTypeArgument> args = new ArrayList<>();
    if (substituted != null && substituted.containsKey(type.getName())) {
      type = substituted.get(type.getName());
      Optional<JavaTypeSymbol> newSubType = type.getEnclosingScope()
              .resolve(type.getName(), JavaTypeSymbol.KIND);
      if(newSubType.isPresent()) {
        substituted = getSubstitutedTypes(newSubType.get(), type);
      }
    }
    else if(substituted != null) {
      //  if type is already substituted, and it contains other types to be substituted
      for(Map.Entry entry : substituted.entrySet()) {
        if(areEqual((JavaTypeSymbolReference) entry.getValue(), type)) {
          Optional<JavaTypeSymbol> newSubType = type.getEnclosingScope()
                  .resolve(type.getName(), JavaTypeSymbol.KIND);
          if(newSubType.isPresent()) {
            substituted = getSubstitutedTypes(newSubType.get(), type);
          }
        }
      }
    }
    if(isWildCardType(type)) {
      return type;
    }
    for(ActualTypeArgument actualTypeArgument : type.getActualTypeArguments()) {
      JavaTypeSymbolReference newSub = applyTypeSubstitution(substituted,
              (JavaTypeSymbolReference) actualTypeArgument.getType());
      ActualTypeArgument newArg = new ActualTypeArgument(actualTypeArgument.isLowerBound(),
              actualTypeArgument.isUpperBound(), newSub);
      args.add(newArg);
    }
    type.setActualTypeArguments(args);
    return type;
  }

  /**
   * checks if the given superMethod (a method in a supertype) is overridden by one of the methods in
   * the classMethods, superMethodParameters are substituted formal parameters of the superMethod.
   *
   * @param classMethods the methods of the class
   * @param superMethod a method from a super type of the class
   * @param superMethodParameters a list of formal parameters of the method from a super type, because some substitutions or type
   *                              inference needs to be performed if the method is a generic method
   * @return true if there exists a method in classMethods which overrides the superMethod
   */
  public  static boolean overrides(List<JavaMethodSymbol> classMethods, JavaMethodSymbol superMethod,
      List<JavaTypeSymbolReference> superMethodParameters) {
    for (JavaMethodSymbol methodSymbol : classMethods) {
      if (overrides(methodSymbol, superMethod, superMethodParameters)) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param m1 a method in a class
   * @param m2 a method in a super type
   * @param m2Parameters list of types of formal parameters of the m2
   * @return true if m2 overrides m1
   */
  public  static boolean overrides(JavaMethodSymbol m1, JavaMethodSymbol m2,
      List<JavaTypeSymbolReference> m2Parameters) {
    if (!m1.getName().equals(m2.getName())) {
      return false;
    }
    if (isSubSignature(m1, m2, m2Parameters)) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param returnType1 a returntype of a method
   * @param returnType2 a returntype of a method
   * @return true if returnType1 is return type substitutable for returnType2
   */
  public static  boolean isReturnTypeSubstitutable(JavaTypeSymbolReference returnType1,
      JavaTypeSymbolReference returnType2) {
    if (returnType1 == null || returnType2 == null) {
      return false;
    }
    if (isPrimitiveType(returnType1) && isPrimitiveType(returnType2)) {
      return areEqual(returnType1, returnType2);
    }
    if (returnType1.getName().equals("void") && returnType2.getName().equals("void")) {
      return true;
    }
    if (areEqual(returnType1, getRawType(returnType2))) {
      return true;
    }
    if (isSubType(returnType1, returnType2)) {
      return true;
    }
    if (isRawType(returnType1)) {
      if (isSubType(returnType1, getRawType(returnType2))) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param m1 a methodSymbol
   * @param m2 a methodSymbol
   * @param m2Parameters list of types of formal parameters of m2
   * @return true if m1 has a signature which is a subSignature of m2 signature
   */
  public  static boolean isSubSignature(JavaMethodSymbol m1, JavaMethodSymbol m2,
      List<JavaTypeSymbolReference> m2Parameters) {
    if (!m1.getName().equals(m2.getName())) {
      return false;
    }
    if (hasSameSignature(m1, m2, m2Parameters)) {
      return true;
    }
    if(m1.isEllipsisParameterMethod() != m2.isEllipsisParameterMethod()) {
      return false;
    }
    List<JavaTypeSymbolReference> typeArgsMethod1 = getSubstitutedFormalParameterTypes(m1);
    if (m2Parameters == null) {
      m2Parameters = getSubstitutedFormalParameterTypes(m2);
    }
    if (typeArgsMethod1.size() == m2Parameters.size()) {
      for (int i = 0; i < typeArgsMethod1.size(); i++) {
        if (!areEqual(applyTypeErasure(typeArgsMethod1.get(i)),
            applyTypeErasure(m2Parameters.get(i)))) {
          return false;
        }
        if (areEqual(applyTypeErasure(typeArgsMethod1.get(i)),
            applyTypeErasure(m2Parameters.get(i)))
            && i == typeArgsMethod1.size() - 1) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   *
   * @param m1 a methodSymbol
   * @param m2 a methodSymbol
   * @param parameters a list of types of formal parameters of m2
   * @return true if two methods have same signature
   */
  public static  boolean hasSameSignature(JavaMethodSymbol m1, JavaMethodSymbol m2,
      List<JavaTypeSymbolReference> parameters) {
    if (!m1.getName().equals(m2.getName())) {
      return false;
    }
    if (!methodsHaveSameArgumentTypes(m1, m2, parameters)) {
      return false;
    }
    return true;
  }

  /**
   *
   * @param m1 a list of types of formal parameters of method1
   * @param m2 a list of types of formal parameters of method2
   * @return true if the given two list has types which have same type erasures
   */
  public  static boolean haveSameErasures(List<JavaTypeSymbolReference> m1,
      List<JavaTypeSymbolReference> m2) {
    if (m1.size() == m2.size()) {
      for (int i = 0; i < m1.size(); i++) {
        if (!areEqual(applyTypeErasure(m1.get(0)), applyTypeErasure(m2.get(0)))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   *
   * @param m1 a methodSymbol
   * @param m2 a methodSymbol
   * @param m2Parameters a list of types of formal parameters of m2
   * @return true if the two methodSymbols have formal parameters with same type.
   */
  public  static boolean methodsHaveSameArgumentTypes(JavaMethodSymbol m1, JavaMethodSymbol m2,
      List<JavaTypeSymbolReference> m2Parameters) {
    if(m1.isEllipsisParameterMethod() != m2.isEllipsisParameterMethod()) {
      return false;
    }
    if (m1.getFormalTypeParameters().size() != m2.getFormalTypeParameters().size()) {
      return false;
    }
    if (m1.getParameters().size() != m2.getParameters().size()) {
      return false;
    }
    if(m1.isEllipsisParameterMethod() != m2.isEllipsisParameterMethod()) {
      return false;
    }
    List<JavaTypeSymbolReference> typeArgsMethod1 = getSubstitutedFormalParameterTypes(m1);
    if (m2Parameters == null || m2Parameters.isEmpty()) {
      m2Parameters = getSubstitutedFormalParameterTypes(m2);
    }
    for (int i = 0; i < typeArgsMethod1.size(); i++) {
      if (!areEqual(typeArgsMethod1.get(i), m2Parameters.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   *
   * @param method a methodSymbol
   * @return a list of types of formal parameters, if the method is generic and formal parameter types
   *        of it are declared with upper bounds, then formal parameters which involves a
   *        formal type parameter is substituted by the formal type parameter. For example T is
   *        substituted by <T extends Serializable>. It is used to check if two methods have same signature.
   */
  public  static List<JavaTypeSymbolReference> getSubstitutedFormalParameterTypes(JavaMethodSymbol method) {
    List<JavaTypeSymbolReference> listOfParameters = new ArrayList<>();
    if (method.getFormalTypeParameters().isEmpty()) {
      for (JavaFieldSymbol fieldSymbol : method.getParameters()) {
        JavaTypeSymbolReference type = new JavaTypeSymbolReference(fieldSymbol.getType().getName(),
            fieldSymbol.getType().getEnclosingScope(), fieldSymbol.getType().getDimension());
        type.setActualTypeArguments(fieldSymbol.getType().getActualTypeArguments());
        listOfParameters.add(type);
      }
    }
    else {
      for (JavaFieldSymbol param : method.getParameters()) {
        listOfParameters
            .add(getSubstitutedFormalParameterTypes(method.getFormalTypeParameters(), param));
      }
    }
    return listOfParameters;
  }

  /**
   *
   * @param formalTypes list of formal type parameters
   * @param formalParameter a fieldSymbol of formal parameter
   * @return a type, which is created by substituting the formal parameter
   *         by the formal type and its bounds
   */
  public  static JavaTypeSymbolReference getSubstitutedFormalParameterTypes(
      List<JavaTypeSymbol> formalTypes, JavaFieldSymbol formalParameter) {
    JavaTypeSymbolReference type = new JavaTypeSymbolReference(formalParameter.getType().getName(),
        formalParameter.getType().getEnclosingScope(), formalParameter.getType().getDimension());
    type.setActualTypeArguments(formalParameter.getType().getActualTypeArguments());
    for (JavaTypeSymbol formalType : formalTypes) {
      type = getSubstitutedFormalParameterTypes(formalType, type,
          formalParameter.getEnclosingScope());
    }
    return type;
  }

  public  static JavaTypeSymbolReference getSubstitutedFormalParameterTypes(JavaTypeSymbol formalType,
      JavaTypeSymbolReference paramType, Scope scope) {
    if (paramType.getName().equals(formalType.getName())) {
      JavaTypeSymbolReference type = new JavaTypeSymbolReference(formalType.getName(),
          scope, 0);
      List<ActualTypeArgument> args = new ArrayList<>();
      for (JavaTypeSymbolReference superType : formalType.getSuperTypes()) {
        ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, true,
            superType);
        args.add(actualTypeArgument);
      }
      type.setActualTypeArguments(args);
      return type;
    }
    else {
      List<ActualTypeArgument> newAct = new ArrayList<>();
      for (ActualTypeArgument actualTypeArgument : paramType.getActualTypeArguments()) {
        ActualTypeArgument newArg = new ActualTypeArgument(actualTypeArgument.isLowerBound(),
            actualTypeArgument.isUpperBound(), getSubstitutedFormalParameterTypes(formalType,
            (JavaTypeSymbolReference) actualTypeArgument.getType(), scope));
        newAct.add(newArg);
      }
      paramType.setActualTypeArguments(newAct);
    }
    return paramType;
  }

  /**
   *
   * @param type
   * @return a raw type of the given type if the type is a parametrized type
   *        JLS3_4.8
   */
  public  static JavaTypeSymbolReference getRawType(JavaTypeSymbolReference type) {
    if (!isPrimitiveType(type) && !type.getActualTypeArguments().isEmpty()) {
      if(type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()){
        JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
            .resolve(type.getName(), JavaTypeSymbol.KIND).get();
        if (!typeSymbol.getFormalTypeParameters().isEmpty() && !type.getActualTypeArguments()
            .isEmpty()) {
          return applyTypeErasure(type);
        }
      }
    }
    return type;
  }

  /**
   *
   * @param type
   * @return type, which is created by applying type erasure on the given type, JLS3_4.6
   */
  public  static JavaTypeSymbolReference applyTypeErasure(TypeReference<? extends TypeSymbol> type) {
    //The erasure of an array type T[] is |T|[].
    if (type.getDimension() > 0) {
      applyTypeErasure(new JavaTypeSymbolReference(type.getName(), type.getEnclosingScope(), 0));
    }
    if(!isPrimitiveType((JavaTypeSymbolReference) type)) {
      if (type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
        JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
            .resolve(type.getName(), JavaTypeSymbol.KIND).get();
        //The erasure of a type variable (§4.4) is the erasure of its leftmost bound.
        if (typeSymbol.isTypeVariable() && type.getActualTypeArguments().isEmpty()) {
          return new JavaTypeSymbolReference("Object", typeSymbol.getEnclosingScope(), 0);
        }
        //The erasure of a type variable (§4.4) is the erasure of its leftmost bound.
        if (typeSymbol.isTypeVariable() && !type.getActualTypeArguments().isEmpty()) {
          JavaTypeSymbolReference typeErasure = (JavaTypeSymbolReference) type
              .getActualTypeArguments().get(0).getType();
          typeErasure.setActualTypeArguments(new ArrayList<>());
          return typeErasure;
        }
        if (!typeSymbol.isTypeVariable() && !type.getActualTypeArguments().isEmpty()) {
          return new JavaTypeSymbolReference(type.getName(), type.getEnclosingScope(), 0);
        }
      }
    }
    //The erasure of every other type is the type itself.
    return (JavaTypeSymbolReference) type;
  }

  /**
   *
   * @param superType
   * @param subType
   * @return true,  if the given supertype is a proper supertype of the given subtype,
   *         JLS3_4.10-1
   */
  public  static boolean isProperSuperType(JavaTypeSymbolReference superType,
      JavaTypeSymbolReference subType) {
    return (!areEqual(superType, subType) && isSuperType(superType, subType));
  }


  /**
   *
   * @param subType
   * @param superType
   * @return true, if the given subtype is a proper subtype of the given supertype, JLS3_4.10-2
   */
  public  static boolean isProperSubType(JavaTypeSymbolReference subType,
      JavaTypeSymbolReference superType) {
    return (!areEqual(subType, superType) && isSubType(subType, superType));
  }

  /**
   *
   * @param subType
   * @param superType
   * @return true, if the given subtype is a subtype of the given supertype, JLS3_4.10-2
   */
  public static boolean isSubType(JavaTypeSymbolReference subType, JavaTypeSymbolReference superType) {
    return isSuperType(superType, subType);
  }

  /**
   *
   * @param superType
   * @param subtype
   * @return true, if the given supertype is a supertype of the given subtype
   *         JLS3_4.10.1, JLS3_4.10.2, JLS3_4.10.3
   */
  public static boolean isSuperType(JavaTypeSymbolReference superType, JavaTypeSymbolReference subtype) {
    return isSuperType(superType, subtype, null);
  }

  /**
   *
   * @param superType
   * @param subType
   * @param substitutedTypes if the superType is a parametrized type, this hashmap contains the name of
   *                         type parameter as key and actual referenced type as the value
   * @return true, if the given superType is a super type of the given subtype
   */
  public static boolean isSuperType(JavaTypeSymbolReference superType, JavaTypeSymbolReference subType,
      HashMap<String, JavaTypeSymbolReference> substitutedTypes) {
    if (areEqual(superType, subType)) {
      return true;
    }
    if(areEqual(superType, getRawType(subType))) {
      return true;
    }
    //The direct supertypes of an intersection type are the types of intersection
    if("&".equals(subType.getName())) {
      for(ActualTypeArgument actualTypeArgument : subType.getActualTypeArguments()) {
        if(isSuperType(superType, (JavaTypeSymbolReference) actualTypeArgument.getType())) {
          return true;
        }
      }
      return false;
    }
    if("&".equals(superType.getName())) {
      for(ActualTypeArgument actualTypeArgument : superType.getActualTypeArguments()) {
        if(!isSuperType((JavaTypeSymbolReference) actualTypeArgument.getType(),subType)) {
          return false;
        }
      }
      return true;
    }
    //a type variable is a direct supertype of its lower bound.
    if ("ASTWildcardType".equals(superType.getName()) && areEqual(getLowerBound(superType),
        subType)) {
      return true;
    }
    List<JavaTypeSymbolReference> upperBounds = getUpperBounds(subType);
    for (JavaTypeSymbolReference upperBound : upperBounds) {
      if (areEqual(superType, upperBound)) {
        return true;
      }
    }
    //null type is subtype of all reference types, except null type
    if (!isPrimitiveType(superType) && !isNullType(superType) && isNullType(subType)) {
      return true;
    }
    //subtyping for arrays
    if (!isPrimitiveType(superType) && !isPrimitiveType(subType) && superType.getDimension() > 0
        && subType.getDimension() > 0) {
      //If S and T are both reference types, then S[] >1 T[] iff S >1 T.
      JavaTypeSymbolReference superComponent = new JavaTypeSymbolReference(superType.getName(),
          superType.getEnclosingScope(), 0);
      JavaTypeSymbolReference subComponent = new JavaTypeSymbolReference(subType.getName(),
          subType.getEnclosingScope(), 0);
      if (isSuperType(superComponent, subComponent)) {
        return true;
      }
    }
    if (superType.getDimension() == 0 && subType.getDimension() > 0) {
      JavaTypeSymbolReference componentType = new JavaTypeSymbolReference(subType.getName(), subType.getEnclosingScope(), 0);
      //Object >1 Object[]
      if (isObjectType(superType) && isObjectType(componentType)) {
        return true;
      }
      //Cloneable >1 Object[]
      if (isCloneable(superType) && isObjectType(componentType)) {
        return true;
      }
      //Serializable >1 Object[]
      if (isSerializable(superType) && isObjectType(componentType)) {
        return true;
      }
    }
    if (subType.getDimension() > 0) {
      JavaTypeSymbolReference componentType = new JavaTypeSymbolReference(subType.getName(), subType.getEnclosingScope(), 0);
      if (isPrimitiveType(componentType)) {
        if(superType.getName().equals("Object") || superType.getName().equals("java.lang.Object")) {
          return true;
        }
        if(superType.getName().equals("Cloneable") || superType.getName().equals("java.lang.Cloneable")) {
          return true;
        }
        if(superType.getName().equals("Serializable") || superType.getName().equals("java.io.Serializable")) {
          return true;
        }
      }
    }

    //for class and interfaces
    //raw type
    if (subType.getEnclosingScope().resolve(subType.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) subType.getEnclosingScope()
          .resolve(subType.getName(), JavaTypeSymbol.KIND).get();
      if (substitutedTypes == null && isRawType(subType)) {
        substitutedTypes = new HashMap<>();
      }
      if (substitutedTypes == null && !isRawType(subType)) {
        substitutedTypes = getSubstitutedTypes(typeSymbol, subType);
      }
      superType = applyTypeSubstitution(substitutedTypes,superType);
      subType = applyTypeSubstitutionAndCapture(subType, substitutedTypes);
      if (areEqual(superType, getRawType(subType))) {
        return true;
      }
      // The type Object, if C is an interface type with no direct super interfaces.
      if (isObjectType(superType)) {
        if (typeSymbol.isInterface() && typeSymbol.getInterfaces().isEmpty()) {
          return true;
        }
        if(typeSymbol.isTypeVariable()) {
          return true;
        }
      }
      if (areEqualWithoutArgs(superType, subType)) {
        if (containsWildCard(superType)) {
          if (captureConversionAvailable(superType, subType)) {
            return true;
          }
        }
        else if (contains(subType.getActualTypeArguments(), superType.getActualTypeArguments())) {
          return true;
        }
      }
      List<JavaTypeSymbolReference> superTypes = getReferencedSuperTypes(subType);
      for (int i = 0; i < superTypes.size(); i++) {
        if (isSuperType(superType, superTypes.get(i), substitutedTypes)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   *
   * @param methodName is the method name to invoke
   * @param isClassMethod shows if to search for class method (static method) or instance methods
   * @param currentType a type from which the method is to be searched (sometimes the type is not present)
   * @param currentSymbol a typesymbol from which the method is to be searched
   * @param actualTypes a list of actual types of the formal type parameters
   * @param actualParameters a list of actual types of the formal parameters
   * @return a hashmap which contains a invoked methodSymbol and its return type. It is hashmap
   * since many methodSymbol cans be invoked. Which is an error. The return type is also returned
   * as if the method being invoked is a generic method and the return type involves a type
   * parameter, the return type also has to be substituted.
   *
   * For given parameters, a method to invoke is searched in the spanned scope
   * of the given type symbol and supertypes of it.
   */
  public  static HashMap<JavaMethodSymbol, JavaTypeSymbolReference> resolveManyInSuperType(String methodName,
      boolean isClassMethod, JavaTypeSymbolReference currentType, JavaTypeSymbol currentSymbol, List<JavaTypeSymbolReference> actualTypes,
      List<JavaTypeSymbolReference> actualParameters) {
    HashMap<JavaMethodSymbol, JavaTypeSymbolReference> resolvedMethods = new HashMap<>();
      Collection<JavaMethodSymbol> methods = currentSymbol.getSpannedScope()
          .resolveMany(methodName, JavaMethodSymbol.KIND);
      HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodsFound = getApplicableMethods(currentType, null, methods, actualTypes,
          actualParameters);
      List<JavaTypeSymbolReference> superTypes = getReferencedSuperTypes(currentSymbol);
      if (methodsFound.isEmpty() && superTypes.isEmpty()) {
        return resolvedMethods;
      }
      if (!methodsFound.isEmpty()) {
        return methodsFound;
      }
      for (JavaTypeSymbolReference superType : superTypes) {
        Collection<JavaMethodSymbol> accessibleMethods = getAccessibleMethods(methodName,
            currentSymbol, superType, isClassMethod);
        HashMap<JavaMethodSymbol, JavaTypeSymbolReference> applicable = getApplicableMethods(null, superType, accessibleMethods, actualTypes, actualParameters);
        if(!applicable.isEmpty()) {
          return applicable;
        }
      }
      String outerType = getEnclosingTypeSymbolName(currentSymbol.getEnclosingScope());
      if(outerType != null) {
        JavaTypeSymbol outerSymbol = (JavaTypeSymbol) currentSymbol.getEnclosingScope().resolve(outerType, JavaTypeSymbol.KIND).get();
        //continue search in outer type
        resolvedMethods.putAll(resolveManyInSuperType(methodName, isClassMethod, null, outerSymbol,
            actualTypes,
            actualParameters));
      }
    return resolvedMethods;
  }

  /**
   *
   * @param methodName a name of the method to be searched
   * @param currentSymbol the symbol from which the method is being invoced,
   *                      it is needed to check the package name and access
   * @param superType a super type, in which the search is to be performed
   * @param isClassMethod indicates if to search for class methods or not
   * @return a list of methodSymbols of the accessible methods
   */
  public  static List<JavaMethodSymbol> getAccessibleMethods(String methodName,
      JavaTypeSymbol currentSymbol, JavaTypeSymbolReference superType, boolean isClassMethod) {
    List<JavaMethodSymbol> pMethods = new ArrayList<>();
    if (superType.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
        .isPresent()) {
      JavaTypeSymbol superSymbol = (JavaTypeSymbol) superType.getEnclosingScope()
          .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
      if (!superSymbol.getSpannedScope().resolveMany(methodName, JavaMethodSymbol.KIND).isEmpty()) {
        Collection<JavaMethodSymbol> methods = superSymbol.getSpannedScope()
            .resolveMany(methodName, JavaMethodSymbol.KIND);
        for (JavaMethodSymbol method : methods) {
          if (isRootPackageSame(superSymbol.getPackageName(), currentSymbol.getPackageName())) {
            if (isDefaultMethod(method) || method.isPublic() || method.isProtected()) {
              pMethods.add(method);
            }
          }
          else {
            if (method.isPublic() || method.isProtected()) {
              pMethods.add(method);
            }
          }
        }
      }
    }
    if (isClassMethod) {
      List<JavaMethodSymbol> staticMethods = new ArrayList<>();
      for (JavaMethodSymbol methodSymbol : pMethods) {
        if (methodSymbol.isStatic()) {
          staticMethods.add(methodSymbol);
        }
      }
      return staticMethods;
    }
    return pMethods;
  }

  /**
   *
   * @param packageName1 full package name of the first symbol.
   * @param packageName2 full package name of the second symbol.
   * @return true, if the root package of both symbols are the same.
   */
  public static boolean isRootPackageSame(String packageName1, String packageName2) {
    packageName1 = packageName1.substring(0, packageName1.indexOf('.'));
    packageName2 = packageName2.substring(0, packageName2.indexOf('.'));
    return packageName1.equals(packageName2);
  }

  /**
   *
   * @param currentType a type from which type the method is being possibly invoked, type is needed
   *                    to see how it is being referenced. Can be null
   * @param superType a supertype from which type method is to be possibly invoked
   * @param collection a collection of accessible methods
   * @param actualTypes a list of types of actual formal type parameters
   * @param actualParameters a list of types of actual formal parameters
   * @return a hashmap, which contains applicable methods
   */
  public  static HashMap<JavaMethodSymbol, JavaTypeSymbolReference> getApplicableMethods(
     JavaTypeSymbolReference currentType,  JavaTypeSymbolReference superType, Collection<JavaMethodSymbol> collection,
      List<JavaTypeSymbolReference> actualTypes, List<JavaTypeSymbolReference> actualParameters) {
    HashMap<JavaMethodSymbol, JavaTypeSymbolReference> phase0 = new HashMap<>();
    HashMap<JavaMethodSymbol, JavaTypeSymbolReference> phase1 = new HashMap<>();
    HashMap<JavaMethodSymbol, JavaTypeSymbolReference> phase2 = new HashMap<>();
    HashMap<JavaMethodSymbol, JavaTypeSymbolReference> phase3 = new HashMap<>();
    for (JavaMethodSymbol methodSymbol : collection) {
      List<JavaTypeSymbolReference> formalParameters = getParameterTypes(methodSymbol);
      JavaTypeSymbolReference returnType = convertToWildCard(methodSymbol.getReturnType());
      if (formalParameters.isEmpty() && actualParameters.isEmpty()) {
        phase0.put(methodSymbol, returnType);
      }
      if (!methodSymbol.getFormalTypeParameters().isEmpty() && actualTypes != null) {
        if (methodSymbol.getFormalTypeParameters().size() == actualTypes.size()) {
          HashMap<String, JavaTypeSymbolReference> substituted = getSubstitutedFormalTypes(
              methodSymbol.getFormalTypeParameters(), actualTypes);
          List<JavaTypeSymbolReference> substitutedParameters = new ArrayList<>();
          for (JavaTypeSymbolReference actualParam : formalParameters) {
            substitutedParameters.add(applyTypeSubstitution(substituted, actualParam));
          }
          formalParameters = new ArrayList<>(substitutedParameters);
          returnType = applyTypeSubstitution(substituted, getSubstitutedReturnType(superType, returnType));
        }
      }
      if (!methodSymbol.getFormalTypeParameters().isEmpty() && actualTypes == null) {
        //infer types
        HashMap<String, JavaTypeSymbolReference> inferredFormalTypes = inferFormalParameterTypes(methodSymbol.isEllipsisParameterMethod(), methodSymbol.getFormalTypeParameters(), formalParameters, actualParameters);
        if(inferredTypeValid(inferredFormalTypes, methodSymbol.getFormalTypeParameters())) {
          List<JavaTypeSymbolReference> substitutedParameters = new ArrayList<>();
          for (JavaTypeSymbolReference actualParam : formalParameters) {
            substitutedParameters.add(applyTypeSubstitution(inferredFormalTypes, actualParam));
          }
          formalParameters = new ArrayList<>(substitutedParameters);
          returnType = applyTypeSubstitution(inferredFormalTypes, getSubstitutedReturnType(superType, returnType));
        }
      }
      if(superType != null && !superType.getActualTypeArguments().isEmpty()) {
        formalParameters = getSubstitutedFormalParameters(superType, formalParameters);
        returnType = getSubstitutedReturnType(superType,returnType);
      }
      if(currentType != null) {
        JavaTypeSymbol currentSymbol = (JavaTypeSymbol) currentType.getEnclosingScope().resolve(currentType.getName(),
            JavaTypeSymbol.KIND).get();
        if(!currentSymbol.getFormalTypeParameters().isEmpty() && currentType.getActualTypeArguments().isEmpty()) {
          //then method is invoked through raw type
          HashMap<String, JavaTypeSymbolReference> inferredFormalTypes = inferFormalParameterTypes(methodSymbol.isEllipsisParameterMethod(), currentSymbol.getFormalTypeParameters(), formalParameters, actualParameters);
          List<JavaTypeSymbolReference> substitutedParameters = new ArrayList<>();
          for(JavaTypeSymbolReference paramType : formalParameters) {
            substitutedParameters.add(applyTypeSubstitution(inferredFormalTypes, paramType));
          }
          formalParameters = new ArrayList<>(substitutedParameters);
          returnType = applyTypeSubstitution(inferredFormalTypes, returnType);
        }
        else {
          HashMap<String, JavaTypeSymbolReference> substituted = getSubstitutedTypes(currentSymbol, currentType);
          List<JavaTypeSymbolReference> substitutedParameters = new ArrayList<>();
          for(JavaTypeSymbolReference paramType : formalParameters) {
            substitutedParameters.add(applyTypeSubstitution(substituted, paramType));
          }
          formalParameters = new ArrayList<>(substitutedParameters);
          returnType = applyTypeSubstitution(substituted, returnType);
        }
      }
      formalParameters = substituteByObject(formalParameters);
      returnType = substituteByObject(returnType);
      if(phase0(methodSymbol, formalParameters, actualParameters).isPresent()) {
        phase0.put(methodSymbol, returnType);
      }
      if(phase1(methodSymbol, formalParameters, actualParameters).isPresent()) {
        phase1.put(methodSymbol, returnType);
      }
      if(phase2(methodSymbol, formalParameters, actualParameters).isPresent()) {
        phase2.put(methodSymbol, returnType);
      }
      if(phase3(methodSymbol, formalParameters, actualParameters).isPresent()) {
        phase3.put(methodSymbol, returnType);
      }
    }
    if(!phase0.isEmpty()) {
      return phase0;
    }
    if(!phase1.isEmpty()){
      return phase1;
    }
    if(!phase2.isEmpty()){
      return phase2;
    }
    return phase3;
  }

  /**
   *
   * @param methodSymbol a methodSymbol
   * @param formalParameters a list of types of formal parameters of the methodSymbol
   * @param actualParameters a list of types of actual formal parameters
   * @return methodSymbol if there exists an identity conversion between formal parameters
   * and actual formal parameters.
   *
   * This phase is included in the specification as a phase which chooses, the most specific
   * method.
   */
  public static Optional<JavaMethodSymbol> phase0(JavaMethodSymbol methodSymbol, List<JavaTypeSymbolReference> formalParameters,
      List<JavaTypeSymbolReference> actualParameters){
    if(!methodSymbol.isEllipsisParameterMethod() && formalParameters.size() == actualParameters.size()) {
      for(int i = 0; i < formalParameters.size(); i++) {
        if(anIdentityConversionAvailable(actualParameters.get(i), formalParameters.get(i))){
          if(i == formalParameters.size()-1){
            return Optional.of(methodSymbol);
          }
        }
        else {
          break;
        }
      }

    }
    return Optional.empty();
  }

  /**
   *
   * @param methodSymbol a methodSymbol
   * @param formalParameters a list of types of formal parameters of the methodSymbol
   * @param actualParameters a list of types of actual formal parameters
   * @return a methodSymbol, if there exists an subtyping relation between types of
   * formal parameters and actual formal parameters
   *
   * as specified in the specification
   */
  public static Optional<JavaMethodSymbol> phase1(JavaMethodSymbol methodSymbol, List<JavaTypeSymbolReference> formalParameters,
      List<JavaTypeSymbolReference> actualParameters){
    if(!methodSymbol.isEllipsisParameterMethod() && formalParameters.size() == actualParameters.size()) {
      for(int i = 0; i < formalParameters.size(); i++) {
        if(isSubType(actualParameters.get(i), formalParameters.get(i))
            && wideningPrimitiveConversionAvailable(actualParameters.get(i), formalParameters.get(i))
            && anIdentityConversionAvailable(actualParameters.get(i), formalParameters.get(i))){
          if(i == formalParameters.size()-1){
            return Optional.of(methodSymbol);
          }
        }
        else {
          break;
        }
      }
    }

    return Optional.empty();
  }

  /**
   *
   * @param methodSymbol a methodSymbol
   * @param formalParameters a list of types of formal parameters of the methodSymbol
   * @param actualParameters a list of types of actual formal parameters
   * @return methodSymbol, if there exists an method invocation conversion
   * between types of the two list
   *
   * as specified in specification
   */
  public static Optional<JavaMethodSymbol> phase2(JavaMethodSymbol methodSymbol, List<JavaTypeSymbolReference> formalParameters,
      List<JavaTypeSymbolReference> actualParameters){
    if(!methodSymbol.isEllipsisParameterMethod() && formalParameters.size() == actualParameters.size()) {
      for(int i = 0; i < formalParameters.size(); i++) {
        if(methodInvocationConversionAvailable(actualParameters.get(i), formalParameters.get(i))){
          if(i == formalParameters.size()-1){
            return Optional.of(methodSymbol);
          }
        }
        else {
          break;
        }
      }
    }
    return Optional.empty();
  }


  /**
   *
   * @param methodSymbol a methodSymbol
   * @param formalParameters a list of types of formal parameters of the methodSymbol
   * @param actualParameters a list of types of actual formal parameters
   * @return methodSymbol, if there exists an method invocation conversion
   * between types of the two list. Also allows an variable arity methods
   *
   * as specified in specification
   */

  public static Optional<JavaMethodSymbol> phase3(JavaMethodSymbol methodSymbol, List<JavaTypeSymbolReference> formalParameters,
      List<JavaTypeSymbolReference> actualParameters) {
    if(methodSymbol.isEllipsisParameterMethod()) {
      JavaTypeSymbolReference componentType = getComponentType(
              formalParameters.get(formalParameters.size() - 1));
      for(int i = 0; i < actualParameters.size(); i++) {
        if(i < formalParameters.size() - 1) {
          if(!methodInvocationConversionAvailable(actualParameters.get(i),
                  formalParameters.get(i))) {
            return Optional.empty();
          }
        } else {
          if(!methodInvocationConversionAvailable(actualParameters.get(i), componentType)) {
            return Optional.empty();
          }
        }
      }
      return Optional.of(methodSymbol);
    }
    return Optional.empty();
  }

  /**
   *
   * @param isEllipsis indicates if the method is variable arity method
   * @param formalTypes list of formal type parameters
   * @param formalParameters list of formal parameters
   * @param actualParameters list of actual parameters
   * @return hashmap, which contains a name of the formal type parameter and the type it is being
   * referenced as.
   *
   * For a invoking a generic method, if the actual formal type parameters are not specified,
   * the type of formal type parameters have to be inferred. Depending on the formal parameters
   * and the actual parameters, formal type parameters are inferred.
   *
   * if the method
   * public <T> add (T t){};
   * is invoked as
   * add(new Integer(1));
   * then T is inferred with Integer type

   */
  public  static HashMap<String, JavaTypeSymbolReference> inferFormalParameterTypes(
      boolean isEllipsis, List<JavaTypeSymbol> formalTypes,
      List<JavaTypeSymbolReference> formalParameters,
      List<JavaTypeSymbolReference> actualParameters) {
    HashMap<String, JavaTypeSymbolReference> result = new HashMap<>();
    if (isEllipsis && !formalParameters.isEmpty()) {
      int size = formalParameters.size() - 1;
      if (actualParameters.size() > size) {
        for (int i = 0; i < size; i++) {
          for (JavaTypeSymbol formalType : formalTypes) {
            Set<JavaTypeSymbolReference> set = inferFormalParameterTypes(formalType,
                formalParameters,
                actualParameters);
            if(!set.isEmpty()) {
              result.put(formalType.getName(), lub(set));
            }
          }
        }
      }
    }
    else {
      if (formalParameters.size() == actualParameters.size() &&
          !formalParameters.isEmpty() && !actualParameters.isEmpty()) {
        for (JavaTypeSymbol formalType : formalTypes) {
          Set<JavaTypeSymbolReference> set = inferFormalParameterTypes(formalType, formalParameters,
              actualParameters);
            if(!set.isEmpty()) {
              result.put(formalType.getName(), lub(set));
            }
        }
      }
    }
    return result;
  }

  /**
   *
   * @param formalType a formal type parameter
   * @param formalParameters formal parameters
   * @param actualParameters actual parameters
   * @return a set of types that formal type is being referenced as. It is possible
   * that one formal type parameter is referenced by different actual types and in that case
   * mutual super type of these set of types is calculated
   */
  public  static Set<JavaTypeSymbolReference> inferFormalParameterTypes(JavaTypeSymbol formalType,
      List<JavaTypeSymbolReference> formalParameters,
      List<JavaTypeSymbolReference> actualParameters) {
    Set<JavaTypeSymbolReference> set = new HashSet<>();
    if(!actualParameters.isEmpty() && !formalParameters.isEmpty()) {
      for (int i = 0; i < formalParameters.size(); i++) {
        set.addAll(getInvokedType(formalType, formalParameters.get(i), actualParameters.get(i)));
      }
    }
    return set;
  }

  /**
   *
   * @param formalType a formal type parameter
   * @param formalParameter a formal parameter
   * @param actualParameter a actual parameter
   * @return a set of types that formal type is being referenced as. It is possible
   * that one formal type parameter is referenced by different actual types and in that case
   * mutual super type of these set of types is calculated
   */
  public  static Set<JavaTypeSymbolReference> getInvokedType(JavaTypeSymbol
      formalType, TypeReference<? extends TypeSymbol> formalParameter, TypeReference<? extends
      TypeSymbol> actualParameter) {
    Set<JavaTypeSymbolReference> set = new HashSet<>();
    if(!isPrimitiveType(box(getComponentType((JavaTypeSymbolReference) actualParameter)))){
      if (formalType.getName().equals(formalParameter.getName())) {
        JavaTypeSymbolReference newActual = new JavaTypeSymbolReference(actualParameter.getName(), actualParameter.getEnclosingScope(), 0);
        newActual = box(newActual);
        newActual.setDimension(actualParameter.getDimension());
        newActual.setActualTypeArguments(actualParameter.getActualTypeArguments());
        set.add(newActual);
        return set;
      }
      if(!formalParameter.getActualTypeArguments().isEmpty() &&
          !actualParameter.getActualTypeArguments().isEmpty() &&
          actualParameter.getActualTypeArguments().size() == formalParameter.getActualTypeArguments().size()) {
        for (int i = 0; i < formalParameter.getActualTypeArguments().size(); i++) {
          set.addAll(getInvokedType(formalType,
              formalParameter.getActualTypeArguments().get(i).getType(),
              actualParameter.getActualTypeArguments().get(i).getType()));
        }
      }
    }
    return set;
  }

  /**
   *
   * @param types contains a type parameter and the type that is being referenced as
   * @param formalTypes formal parameters
   * @return true if for given inferred type of formal type parameter, the formal parameters are
   * same
   */
  public  static boolean inferredTypeValid(HashMap<String, JavaTypeSymbolReference> types, List<JavaTypeSymbol> formalTypes) {
    for(JavaTypeSymbol formalType : formalTypes) {
      if(!formalType.getSuperTypes().isEmpty() && types.containsKey(formalType.getName())) {
        JavaTypeSymbolReference inferredType = types.get(formalType.getName());
        if(inferredType.getName().equals("&")) {
          for(JavaTypeSymbolReference supType : getReferencedSuperTypes(formalType)) {
            boolean isFound = false;
            for(ActualTypeArgument actualTypeArgument : inferredType.getActualTypeArguments()) {
              if(isSubType((JavaTypeSymbolReference) actualTypeArgument.getType(), supType)) {
                isFound = true;
              }
            }
            if(!isFound) {
              return isFound;
            }
          }
        }
        else {
          for(JavaTypeSymbolReference superType : getReferencedSuperTypes(formalType)) {
            if(!isSubType(inferredType, superType)) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }


  /**
   *
   * @param types
   * @return returns the least upper bound of the given set of types JLS3_15.12.2.7
   */
  public  static JavaTypeSymbolReference lub(Set<JavaTypeSymbolReference> types) {
    List<Set<JavaTypeSymbolReference>> set = new ArrayList<>();
    for (JavaTypeSymbolReference type : types) {
      set.add(EST(box(type)));
    }
    Set<JavaTypeSymbolReference> intersect = EC(set);
    Set<JavaTypeSymbolReference> candidates = MEC(intersect);
    if (candidates.size() == 1) {
      return candidates.iterator().next();
    }
    JavaTypeSymbolReference intersectType = new JavaTypeSymbolReference("&",
        candidates.iterator().next().getEnclosingScope(), 0);
    List<ActualTypeArgument> arguments = new ArrayList<>();
    for (JavaTypeSymbolReference bound : candidates) {
      ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, true, bound);
      arguments.add(actualTypeArgument);
    }
    intersectType.setActualTypeArguments(arguments);
    return intersectType;
  }

  /**
   *
   * @param set of types
   * @return JLS3_15.12.2.7 returns the set of erased candidate from given set.
   */
  public  static Set<JavaTypeSymbolReference> MEC(Set<JavaTypeSymbolReference> set) {
    Set<JavaTypeSymbolReference> ret = new HashSet<>();
    for (JavaTypeSymbolReference type : set) {
      boolean existsSubtype = false;
      for (JavaTypeSymbolReference type1 : set) {
        if (type != type1 && isSubType(type1, type)) {
          existsSubtype = true;
          break;
        }
      }
      if (!existsSubtype) {
        ret.add(type);
      }
    }
    return ret;
  }

  /**
   *
   * @param collection of set of types
   * @return JLS3_15.12.2.7 returns a set which contains intersected types in collection
   */
  public static  Set<JavaTypeSymbolReference> EC(List<Set<JavaTypeSymbolReference>> collection) {
    Set<JavaTypeSymbolReference> result = new HashSet<>();
    if(collection.size() == 1) {
      return new HashSet<>(collection.get(0));
    }
    for(int i = 0 ; i < collection.size() - 1; i++) {
      result = EC(collection.get(i), collection.get(i+1));
    }
    return result;
  }

  public static  Set<JavaTypeSymbolReference> EC(Set<JavaTypeSymbolReference> set1, Set<JavaTypeSymbolReference> set2) {
    Set<JavaTypeSymbolReference> ret = new HashSet<>();
    for (JavaTypeSymbolReference type1 : set1) {
      for(JavaTypeSymbolReference type2 : set2) {
        if(areEqual(type1, type2)){
          ret.add(type1);
        }

      }
    }
    return ret;
  }


  /**
   *
   * @param type
   * @return JLS3_15.12.2.7 returns a set of all super types of the given type ( recursive )
   */
  public  static Set<JavaTypeSymbolReference> EST(JavaTypeSymbolReference type) {
    Set<JavaTypeSymbolReference> set = new HashSet<>();
      List<JavaTypeSymbolReference> superTypes = getReferencedSuperTypes(type);
      for(JavaTypeSymbolReference superType : superTypes) {
        set.addAll(EST(superType));
      }
      set.add(getRawType(type));
    return removeDuplicateSuperType(set);
  }

  /**
   *
   * @param set of super types
   * @return set, which has the duplicate supertypes removed
   */
  public  static Set<JavaTypeSymbolReference> removeDuplicateSuperType (Set<JavaTypeSymbolReference> set){
    List<JavaTypeSymbolReference> list = new ArrayList<>(set);
    List<JavaTypeSymbolReference> res = new ArrayList<>(set);
    for(int i = 0 ; i < set.size(); i++) {
      for(int j = i + 1 ; j < set.size(); j++ ) {
        if(list.get(i) != list.get(j) && list.get(i).getName().equals(list.get(j).getName())) {
          if(res.contains(list.get(j))) {
            res.remove(list.get(j));
          }
        }
      }
    }
    return new HashSet<>(res);
  }

  /**
   *
   * @param type
   * @return a component type of the array
   */
  public static JavaTypeSymbolReference getComponentType(JavaTypeSymbolReference type) {
    if (type.isArray()) {
      return new JavaTypeSymbolReference(type.getName(), type.getEnclosingScope(), 0);
    }
    return type;
  }
  /**
   *
   * @param formalParameters a list of formal parameters
   * @return a list of formal parameters, if there exists a type in the given list
   * which still contains a type parameter even after type inferring, then that type
   * parameter is inferred as java.lang.Object
   */
  public static List<JavaTypeSymbolReference> substituteByObject(List<JavaTypeSymbolReference> formalParameters) {
    List<JavaTypeSymbolReference> result = new ArrayList<>();
    for(JavaTypeSymbolReference param : formalParameters) {
      result.add(substituteByObject(param));
    }
    return result;
  }

  public static JavaTypeSymbolReference substituteByObject(TypeReference<? extends TypeSymbol> type) {
    if(!isPrimitiveType((JavaTypeSymbolReference) type)) {
      if(type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
        JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope().resolve(type.getName(),
            JavaTypeSymbol.KIND).get();
        if(typeSymbol.isTypeVariable()) {
          return new JavaTypeSymbolReference("java.lang.Object", type.getEnclosingScope(), 0);
        }
      }
      JavaTypeSymbolReference newType = new JavaTypeSymbolReference(type.getName(),
          type.getEnclosingScope(), type.getDimension());
      List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();
      for(ActualTypeArgument actualTypeArgument : type.getActualTypeArguments()) {
        actualTypeArguments.add(new ActualTypeArgument(actualTypeArgument.isLowerBound(),
            actualTypeArgument.isUpperBound(), substituteByObject(actualTypeArgument.getType())));
      }
      newType.setActualTypeArguments(actualTypeArguments);
      return newType;
    }
    return (JavaTypeSymbolReference) type;
  }

  /**
   *
   * @param formalTypes list of types of formal type parameters
   * @param actualTypes list of types of actual formal type parameters
   * @return  a HashMap which contains the formal type parameter name with the type that is
   * used to instantiate it.
   * for example for the following method
   *    public <T> add(T t){};
   * if the actual formal type parameter used to invoke is as following
   *    this.<String>add("hello");
   * then the type parameter T is substituted by String.
   */
  public  static HashMap<String, JavaTypeSymbolReference> getSubstitutedFormalTypes(
      List<JavaTypeSymbol> formalTypes, List<JavaTypeSymbolReference> actualTypes) {
    HashMap<String, JavaTypeSymbolReference> result = new HashMap<>();
    for (int i = 0; i < formalTypes.size(); i++) {
      if (formalTypes.get(i).getSuperTypes().isEmpty()) {
        result.put(formalTypes.get(i).getName(), actualTypes.get(i));
      }
      else {
        for (JavaTypeSymbolReference superType : formalTypes.get(i).getSuperTypes()) {
          if (!anIdentityConversionAvailable(actualTypes.get(i), superType) || isSuperType(
              actualTypes.get(i), superType)) {
            result.put(formalTypes.get(i).getName(), actualTypes.get(i));
          }
          else {
            return null;
          }
        }
      }
    }
    return result;
  }


  /**
   *
   * @param typeSymbol a symbol of the type
   * @param type a reference of the type
   * @return Hashmap which contains the name of the type parameter and the type that is being
   * instantiated
   *
   * if the type is parametrized type then by comparing it with its symbol,
   * which type parameter is substituted by which type is resolved and returned.
   *
   * for example in the following class declaration
   * class A extends List<String> {
   * }
   * then superType in this case is List<String> and its symbol is List<E>. From this,
   * it can be resolved that type parameter E is substituted by String.
   */
  public  static HashMap<String, JavaTypeSymbolReference> getSubstitutedTypes(JavaTypeSymbol typeSymbol,
      JavaTypeSymbolReference type) {
    if (!typeSymbol.getFormalTypeParameters().isEmpty() && !type.getActualTypeArguments()
        .isEmpty()) {
      if (typeSymbol.getFormalTypeParameters().size() == type.getActualTypeArguments().size()) {
        HashMap<String, JavaTypeSymbolReference> map = new HashMap<>();
        for (int i = 0; i < type.getActualTypeArguments().size(); i++) {
          JavaTypeSymbol argSymbol = typeSymbol.getFormalTypeParameters().get(i);
          JavaTypeSymbolReference argType = (JavaTypeSymbolReference) type.getActualTypeArguments()
              .get(i).getType();
          if (argSymbol.isTypeVariable()) {
            if (argSymbol.getSuperTypes().isEmpty()) {
              map.put(typeSymbol.getFormalTypeParameters().get(i).getName(),
                  (JavaTypeSymbolReference) type.getActualTypeArguments().get(i).getType());
            }
            else {
              for (JavaTypeSymbolReference superType : argSymbol.getSuperTypes()) {
                if (isTypeParameter(superType)) {
                  if (map.containsKey(superType.getName())) {
                    if (isSuperType(map.get(superType.getName()), argType)) {
                      map.put(argSymbol.getName(), argType);
                    }
                  }
                  else {
                    return null;
                  }
                }
                else {
                  if (isSuperType(superType, argType)) {
                    map.put(argSymbol.getName(), argType);
                  }
                  else {
                    return null;
                  }
                }
              }
            }
          }
        }
        if (!map.isEmpty()) {
          return map;
        }
      }
    }
    return null;
  }

  /**
   *
   * @param type
   * @return true if the given type is a type parameter
   */
  public static  boolean isTypeParameter(JavaTypeSymbolReference type) {
    if (isPrimitiveType(type)) {
      return false;
    }
    if (type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
      return false;
    }
    return true;
  }

  /**
   *
   * @param type
   * @return a list of references of super types
   *
   * If the super type is a parametrized type then this method returns all supertypes references.
   * The supertypes are obtained through reflexive and transitive closure
   */
  public  static List<JavaTypeSymbolReference> getAllReferencedSuperTypes(JavaTypeSymbolReference type) {
    List<JavaTypeSymbolReference> list = new ArrayList<>(getReferencedSuperTypes(type));
    if (type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
          .resolve(type.getName(), JavaTypeSymbol.KIND).get();
      for (JavaTypeSymbolReference superType : typeSymbol.getSuperTypes()) {
        list.addAll(getAllReferencedSuperTypes(superType));
      }
    }
    return list;
  }

  /**
   *
   * @param type
   * @return a list of references of super types
   *
   * If the super type is a parametrized type then this method returns direct supertypes references.
   */
  public static  List<JavaTypeSymbolReference> getReferencedSuperTypes(JavaTypeSymbolReference type) {
    List<JavaTypeSymbolReference> result = new ArrayList<>();
    String completeName = getCompleteName(type);
    if (!isPrimitiveType(type) && !isObjectType(type) && type.getDimension() == 0) {
      if(type.getEnclosingScope().resolve(completeName, JavaTypeSymbol.KIND).isPresent()) {
        JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
            .resolve(completeName, JavaTypeSymbol.KIND).get();
        return getReferencedSuperTypes(typeSymbol);
      }
    }
    return result;
  }

  public  static List<JavaTypeSymbolReference> getReferencedSuperTypes(JavaTypeSymbol typeSymbol) {
    List<JavaTypeSymbolReference> result = new ArrayList<>();
    if (typeSymbol.getName().equals("Object") || typeSymbol.getName().equals("java.lang.Object")) {
      return result;
    }
    if (typeSymbol.getSuperClass().isPresent()) {
      result.add(typeSymbol.getSuperClass().get());
      result.addAll(getReferencedSuperTypes(typeSymbol.getSuperClass().get()));
    }
    else {
      result.add(getObjectType(typeSymbol.getEnclosingScope()));
    }
    for (JavaTypeSymbolReference superInterface: typeSymbol.getInterfaces()) {
      result.add(superInterface);
      result.addAll(getReferencedSuperTypes(superInterface));
    }
    return result;
  }

  /**
   *
   * @param type type of a class
   * @param superClasses super classes of the given type
   * @return true if there exists a circularity in the class hierarchy
   */
  public  static boolean classCircularityExist(JavaTypeSymbolReference type,
      List<JavaTypeSymbolReference> superClasses) {
    if (type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
          .resolve(type.getName(), JavaTypeSymbol.KIND).get();
      if (typeSymbol.getSuperClass().isPresent()) {
        for (JavaTypeSymbolReference superClass : superClasses) {
          if (superClass.getName().equals(typeSymbol.getSuperClass().get().getName())) {
            return true;
          }
        }
        superClasses.add(typeSymbol.getSuperClass().get());
        if (classCircularityExist(typeSymbol.getSuperClass().get(), superClasses)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   *
   * @param type
   * @return a list of type reference to all super interfaces.
   */
  public  static List<JavaTypeSymbolReference> getAllParametrizedSuperInterfaces(
      JavaTypeSymbolReference type) {
    List<JavaTypeSymbolReference> interfaces = new ArrayList<>(getAllReferencedSuperTypes(type));
    List<JavaTypeSymbolReference> result = new ArrayList<>();
    for (JavaTypeSymbolReference superType : interfaces) {
      if (superType.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
          .isPresent()) {
        JavaTypeSymbol typeSymbol = (JavaTypeSymbol) superType.getEnclosingScope()
            .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
        if (typeSymbol.isInterface() && !superType.getActualTypeArguments().isEmpty()) {
          result.add(superType);
        }
      }
    }
    return result;
  }

  /**
   *
   * @param type
   * @return true if the given type is a raw type
   */
  public  static boolean isRawType(JavaTypeSymbolReference type) {
    if (!isPrimitiveType(type) && type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
          .resolve(type.getName(), JavaTypeSymbol.KIND).get();
      if (!typeSymbol.getFormalTypeParameters().isEmpty() && type.getActualTypeArguments()
          .isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param type
   * @return true if the actual type arguments of the method contains
   * a wildcard type
   */
  public  static boolean containsWildCard(JavaTypeSymbolReference type) {
    for (ActualTypeArgument actualTypeArgument : type.getActualTypeArguments()) {
      if (actualTypeArgument.getType().getName().equals("ASTWildcardType")) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a widening reference conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.1.5
   */
  public  static  boolean wideningReferenceConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    //A widening reference conversion exists from any reference type S to any reference type T, provided S is a subtype of T.
    return isSubType(from, to);
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a widening primitive conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.1.2
   */
  public  static boolean wideningPrimitiveConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    if(from.getDimension() > 0 ||  to.getDimension() > 0) {
      return false;
    }
    if (from.getName().equals("byte") &&
        (to.getName().equals("short") ||
            to.getName().equals("int") ||
            to.getName().equals("long") ||
            to.getName().equals("float") ||
            to.getName().equals("double"))) {
      return true;
    }
    if (from.getName().equals("short") &&
        (to.getName().equals("int") ||
            to.getName().equals("long") ||
            to.getName().equals("float") ||
            to.getName().equals("double"))) {
      return true;
    }
    if (from.getName().equals("char") &&
        (to.getName().equals("int") ||
            to.getName().equals("long") ||
            to.getName().equals("float") ||
            to.getName().equals("double"))) {
      return true;
    }
    if (from.getName().equals("int") &&
        (to.getName().equals("long") ||
            to.getName().equals("double") ||
            to.getName().equals("float"))) {
      return true;
    }
    if (from.getName().equals("long") &&
        (to.getName().equals("float") ||
            to.getName().equals("double"))) {
      return true;
    }
    if (from.getName().equals("float") &&
        (to.getName().equals("double"))) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a narrowing primitive conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.1.3
   */
  public  static boolean narrowingPrimitiveConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    if(from.getDimension() > 0 || to.getDimension() > 0) {
      return false;
    }
    if (from.getName().equals("short") &&
        (to.getName().equals("byte") ||
            to.getName().equals("char"))) {
      return true;
    }
    if (from.getName().equals("char") &&
        (to.getName().equals("byte") ||
            to.getName().equals("short"))) {
      return true;
    }
    if (from.getName().equals("int") &&
        (to.getName().equals("byte") ||
            to.getName().equals("short") ||
            to.getName().equals("char"))) {
      return true;
    }
    if (from.getName().equals("long") &&
        (to.getName().equals("byte") ||
            to.getName().equals("short") ||
            to.getName().equals("char") ||
            to.getName().equals("int"))) {
      return true;
    }
    if (from.getName().equals("float") &&
        (to.getName().equals("byte") ||
            to.getName().equals("short") ||
            to.getName().equals("char") ||
            to.getName().equals("int") ||
            to.getName().equals("long"))) {
      return true;
    }
    if (from.getName().equals("double") &&
        (to.getName().equals("byte") ||
            to.getName().equals("short") ||
            to.getName().equals("char") ||
            to.getName().equals("int") ||
            to.getName().equals("long") ||
            to.getName().equals("float"))) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a narrowing reference conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.1.6
   */
  public  static boolean narrowingReferenceConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    //From any reference type S to any reference type T, provided that S is  proper supertype (§4.10) of T. (An important special case is that there is a narrowing conversion from the class type Object to any other reference type.)
    if (isProperSuperType(from, to) || isObjectType(to)) {
      return true;
    }
    if (from.getEnclosingScope().resolve(from.getName(), JavaTypeSymbol.KIND).isPresent()
        && to.getEnclosingScope().resolve(to.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol fromSymbol = (JavaTypeSymbol) from.getEnclosingScope()
          .resolve(from.getName(), JavaTypeSymbol.KIND).get();
      JavaTypeSymbol toSymbol = (JavaTypeSymbol) to.getEnclosingScope()
          .resolve(to.getName(), JavaTypeSymbol.KIND).get();
      // From any class type C to any non-parameterized interface type K, provided that C is not final and does not implement K.
      if ((fromSymbol.isClass() && !fromSymbol.isFinal())
          && (toSymbol.isInterface() && toSymbol.getFormalTypeParameters().isEmpty()
          && !isSuperType(to, from))) {
        return true;
      }
      if (fromSymbol.isInterface()) {
        // From any interface type J to any non-parameterized class type C that is not final.
        if (toSymbol.getFormalTypeParameters().isEmpty() && toSymbol.isClass() && !toSymbol
            .isFinal()) {
          return true;
        }
        // From the interface types Cloneable and java.io.Serializable to an array type T[].
        if ((isCloneable(from) || isSerializable(from)) && to.getDimension() > 0) {
          return true;
        }
        // From any interface type J to any non-parameterized interface type K, provided  that J is not a subinterface of K.
        if (toSymbol.isInterface() && toSymbol.getFormalTypeParameters().isEmpty() && !isSubType(
            from, to)) {
          return true;
        }
      }
      // From any array type SC[] to any array type TC[], provided that SC and TC are reference types and there is a narrowing conversion from SC to TC.
      if (from.getDimension() > 0 && to.getDimension() > 0) {
        return narrowingReferenceConversionAvailable(box(from), box(to));
      }
      if (fromSymbol.isFormalTypeParameter() && !fromSymbol.getSuperClass().isPresent() && fromSymbol.getSuperTypes().isEmpty()) {
        return true;
      }
    }

    return false;
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a unchecked conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.1.9
   */
  public  static boolean unCheckedConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    /*
    Let G name a generic type declaration with n formal type parameters. There is
    an unchecked conversion from the raw type (§4.8) G to any parameterized type of
    the form G<T1 ... Tn>. Use of an unchecked conversion generates a mandatory
    compile-time warning (which can only be suppressed using the SuppressWarnings
    annotation (§9.6.1.5))
     */
    JavaTypeSymbol fromSymbol = null;
    JavaTypeSymbol toSymbol = null;
    if (from.isReferencedSymbolLoaded() && to.isReferencedSymbolLoaded()) {
      fromSymbol = from.getReferencedSymbol();
      toSymbol = to.getReferencedSymbol();
    }
    else if (from.getEnclosingScope().resolve(from.getName(), JavaTypeSymbol.KIND).isPresent() && to
        .getEnclosingScope().resolve(to.getName(), JavaTypeSymbol.KIND).isPresent()) {
      fromSymbol = (JavaTypeSymbol) from.getEnclosingScope()
          .resolve(from.getName(), JavaTypeSymbol.KIND).get();
      toSymbol = (JavaTypeSymbol) to.getEnclosingScope().resolve(to.getName(), JavaTypeSymbol.KIND)
          .get();
    }
    if (fromSymbol == null || toSymbol == null) {
      return false;
    }
    if (!fromSymbol.getFormalTypeParameters().isEmpty() && !toSymbol.getFormalTypeParameters()
        .isEmpty()) {
      if (from.getActualTypeArguments().isEmpty() && !allArgsAreBoundlessWildCards(
          to.getActualTypeArguments())) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists an identity conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.1-1
   */
  public static boolean anIdentityConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    return areEqual(from, to);
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a method invocation conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.3
   */
  public static boolean methodInvocationConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    return (safeMethodInvocationConversionAvailable(from, to)
        || unsafeMethodInvocationConversionAvailable(from, to));

  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a safe method invocation conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.3-1
   */
  public  static boolean safeMethodInvocationConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    return (anIdentityConversionAvailable(from, to) ||
        wideningPrimitiveConversionAvailable(unbox(from), to) ||
        wideningReferenceConversionAvailable(box(from), to) ||
        boxingConversionAvailable(from, to) ||
        unboxingConversionAvailable(from, to));
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists an unsafe method invocation conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.3-1
   */
  public  static boolean unsafeMethodInvocationConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    if (isRawType(from) && !isRawType(to)) {
      if (isSuperType(getRawType(to), from)) {
        to = capture(to);
        return unCheckedConversionAvailable(from, to);
      }
    }
    return false;
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a cast type conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.5-0 - JLS3_5.5-11
   */
  public  static boolean castTypeConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    return safeCastTypeConversionAvailable(from, to) || unsafeCastTypeConversionAvailable(from, to);
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a safe cast type conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.5-0 - JLS3_5.5-11
   */
  public  static boolean safeCastTypeConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    if(!isPrimitiveType(from)) {
      if ((from.getActualTypeArguments().isEmpty() && to.getActualTypeArguments().isEmpty())
          || (!from.getActualTypeArguments().isEmpty()) && to.getActualTypeArguments().isEmpty()) {
        if (narrowingReferenceConversionAvailable(box(from), box(to))) {
          return true;
        }
      }
    }
    return (anIdentityConversionAvailable(from, to) ||
        wideningPrimitiveConversionAvailable(unbox(from), to) ||
        narrowingPrimitiveConversionAvailable(unbox(from), to) ||
        wideningReferenceConversionAvailable(box(from), to) ||
        boxingConversionAvailable(from, to) ||
        unboxingConversionAvailable(from, to));
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists an unsafe cast type conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.5-0 - JLS3_5.5-11
   */
  public  static  boolean unsafeCastTypeConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    //A cast to a type variable (§4.4) is always unchecked.
    if (to.getEnclosingScope().resolve(to.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) to.getEnclosingScope()
          .resolve(to.getName(), JavaTypeSymbol.KIND).get();
      if (typeSymbol.isTypeVariable()) {
        return true;
      }
    }
    if (from.getActualTypeArguments().isEmpty() && !to.getActualTypeArguments().isEmpty()) {
      if (narrowingReferenceConversionAvailable(box(from), to)) {
        return true;
      }
    }
    if (isRawType(from) && !isRawType(to)) {
      if (isSuperType(getRawType(to), from)) {
        to = capture(to);
        return unCheckedConversionAvailable(from, to);
      }
    }
    return false;
  }


  /**
   *
   * @param from
   * @param to
   * @return true if there exists an assignment conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.2
   */
  public  static  boolean assignmentConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    return safeAssignmentConversionAvailable(from, to) || unsafeAssignmentConversionAvailable(from,
        to);
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a safe assignment conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.2-1
   */
  public  static  boolean safeAssignmentConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    return (anIdentityConversionAvailable(from, to) ||
        wideningPrimitiveConversionAvailable(unbox(from), to) ||
        wideningReferenceConversionAvailable(box(from), to) ||
        boxingConversionAvailable(from, to) ||
        unboxingConversionAvailable(from, to));
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists an unsafe assignment conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.2-2
   */
  public  static boolean unsafeAssignmentConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    if (isRawType(from) && !isRawType(to)) {
      if (isSuperType(getRawType(to), from)) {
        to = capture(to);
        return unCheckedConversionAvailable(from, to);
      }
    }
    return false;
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a capture conversion available from type 'from'
   * to type 'to'.
   * JLS3_5.1-10
   */
  public  static boolean captureConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    if (!areEqualWithoutArgs(from, to)) {
      return false;
    }
    if (from.getActualTypeArguments().isEmpty() && to.getActualTypeArguments().isEmpty()) {
      return anIdentityConversionAvailable(from, to);
    }
    if (from.getActualTypeArguments().size() != to.getActualTypeArguments().size()) {
      return false;
    }
    if (!from.getActualTypeArguments().isEmpty() && !to.getActualTypeArguments().isEmpty()) {
      from = capture(from);
      for (int i = 0; i < from.getActualTypeArguments().size(); i++) {
        if (!contains(to.getActualTypeArguments(), from.getActualTypeArguments())) {
          return false;
        }
      }
    }
    return true;
  }


  /**
   *
   * @param type
   * @return a type, created by capturing according to capture conversion
   * JLS3_5.1-10
   */
  public  static JavaTypeSymbolReference capture(JavaTypeSymbolReference type) {
    if (type.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) type.getEnclosingScope()
          .resolve(type.getName(), JavaTypeSymbol.KIND).get();
      JavaTypeSymbolReference resulType = new JavaTypeSymbolReference(type.getName(), type.getEnclosingScope(), type.getDimension());
      if (type.getActualTypeArguments().size() == typeSymbol.getFormalTypeParameters().size()) {
        List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();
        for (int i = 0; i < type.getActualTypeArguments().size(); i++) {
          actualTypeArguments.add(capture(typeSymbol.getFormalTypeParameters().get(i),
              type.getActualTypeArguments().get(i)));
        }
        resulType.setActualTypeArguments(actualTypeArguments);
        return resulType;
      }
    }
    return type;
  }


  public static ActualTypeArgument capture(
      JavaTypeSymbol typeParam, ActualTypeArgument typeVar) {
    if (isWildCardType(typeVar.getType())) {
      if (isBoundlessWildCardType(typeVar.getType())) {
        return new ActualTypeArgument(false, false,
            glb(typeParam, typeParam.getSuperTypes(), typeVar.getType().getEnclosingScope()));
      }
      if (!getUpperBound(typeVar.getType()).equals(typeVar.getType()) && getLowerBound(
          typeVar.getType()).getName().equals("null") && !typeParam.getSuperTypes().isEmpty()) {
        List<JavaTypeSymbolReference> typeList = new ArrayList<>(typeParam.getSuperTypes());
        typeList.add(getUpperBound(typeVar.getType()));
        return new ActualTypeArgument(typeVar.isLowerBound(), typeVar.isUpperBound(),
            glb(typeParam, typeList, typeVar.getType().getEnclosingScope()));
      }
      if (!getLowerBound(typeVar.getType()).getName().equals("null")) {
        JavaTypeSymbolReference capturedType = glb(typeParam, typeParam.getSuperTypes(),
            typeVar.getType().getEnclosingScope());
        List<ActualTypeArgument> finalArgs = new ArrayList<>(capturedType.getActualTypeArguments());
        ActualTypeArgument lowerBound = new ActualTypeArgument(true, false,
            getLowerBound(typeVar.getType()));
        finalArgs.add(lowerBound);
        capturedType.setActualTypeArguments(finalArgs);
        return new ActualTypeArgument(typeVar.isLowerBound(), typeVar.isUpperBound(), capturedType);
      }
    }
    return typeVar;
  }

  /**
   *
   * @param typeParam symbol of the actual formal type parameter
   * @param typeArgs type arguments of the type of the actual formal type parameter
   * @param scope
   * @return a type, which is a greatest lower bound of the given typeArgs and type param
   */
  public static JavaTypeSymbolReference glb(JavaTypeSymbol typeParam,
      List<JavaTypeSymbolReference> typeArgs, Scope scope) {
    JavaTypeSymbolReference wildCard = new JavaTypeSymbolReference("ASTWildcardType", scope, 0);
    if (typeArgs.isEmpty()) {
      if (typeParam.isTypeVariable() && typeParam.getSuperTypes().isEmpty()) {
        ActualTypeArgument actualTypeArgument = new ActualTypeArgument(false, true,
            getObjectType(typeParam.getEnclosingScope()));
        List<ActualTypeArgument> argList = new ArrayList<>();
        argList.add(actualTypeArgument);
        wildCard.setActualTypeArguments(argList);
        return wildCard;
      }
      if (typeParam.isTypeVariable() && !typeParam.getSuperTypes().isEmpty()) {
        wildCard.setActualTypeArguments(getActualTypeArguments(typeParam.getSuperTypes()));
        return wildCard;
      }
    }
    List<JavaTypeSymbolReference> classes = new ArrayList<>();
    List<JavaTypeSymbolReference> interfaces = new ArrayList<>();
    for (JavaTypeSymbolReference superType : typeArgs) {
      JavaTypeSymbol superSymbol = (JavaTypeSymbol) superType.getEnclosingScope()
          .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
      if (superSymbol.isInterface()) {
        interfaces.add(superType);
      }
      if (superSymbol.isClass()) {
        classes.add(superType);
      }
    }
    if (classes.isEmpty()) {
      wildCard.setActualTypeArguments(getActualTypeArguments(interfaces));
      return wildCard;
    }
    if (classes.size() == 1) {
      classes.addAll(interfaces);
      wildCard.setActualTypeArguments(getActualTypeArguments(classes));
      return wildCard;
    }
    if (classes.size() == 2) {
      if (subtypingRelationExist(classes.get(0), classes.get(1))) {
        List<JavaTypeSymbolReference> finalArgs = new ArrayList<>();
        finalArgs.add(typeParam.getSuperClass().get());
        finalArgs.addAll(interfaces);
        wildCard.setActualTypeArguments(getActualTypeArguments(finalArgs));
        return wildCard;
      }
    }
    return null;
  }


  /**
   *
   * @param from
   * @param to
   * @return true if there exists a boxing conversion available from the type 'from'
   * to type 'to'
   *
   * JLS3_5.1-7
   */
  public  static boolean boxingConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    if (from.getName().equals("boolean") && (to.getName().equals("Boolean") || to.getName().equals("java.lang.Boolean"))) {
      return true;
    }
    if (from.getName().equals("byte") && (to.getName().equals("Byte")|| to.getName().equals("java.lang.Byte"))) {
      return true;
    }
    if (from.getName().equals("char") && (to.getName().equals("Character")|| to.getName().equals("java.lang.Character"))) {
      return true;
    }
    if (from.getName().equals("short") && (to.getName().equals("Short")|| to.getName().equals("java.lang.Short"))) {
      return true;
    }
    if (from.getName().equals("int") && (to.getName().equals("Integer")|| to.getName().equals("java.lang.Integer"))) {
      return true;
    }
    if (from.getName().equals("long") && (to.getName().equals("Long")|| to.getName().equals("java.lang.Long"))) {
      return true;
    }
    if (from.getName().equals("float") && (to.getName().equals("Float")|| to.getName().equals("java.lang.Float"))) {
      return true;
    }
    if (from.getName().equals("double") && (to.getName().equals("Double")|| to.getName().equals("java.lang.Double"))) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param from
   * @param to
   * @return true if there exists a boxing conversion available from the type 'from'
   * to type 'to'
   *
   * JLS3_5.1-8
   */
  public  static boolean unboxingConversionAvailable(JavaTypeSymbolReference from,
      JavaTypeSymbolReference to) {
    if ((from.getName().equals("Boolean")|| from.getName().equals("java.lang.Boolean")) && to.getName().equals("boolean")) {
      return true;
    }
    if ((from.getName().equals("Byte")|| from.getName().equals("java.lang.Byte")) && to.getName().equals("byte")) {
      return true;
    }
    if ((from.getName().equals("Character")|| from.getName().equals("java.lang.Character"))  && to.getName().equals("char")) {
      return true;
    }
    if ((from.getName().equals("Short")|| from.getName().equals("java.lang.Short")) && to.getName().equals("short")) {
      return true;
    }
    if ((from.getName().equals("Integer")|| from.getName().equals("java.lang.Integer"))  && to.getName().equals("int")) {
      return true;
    }
    if ((from.getName().equals("Long")|| from.getName().equals("java.lang.Long")) && to.getName().equals("long")) {
      return true;
    }
    if ((from.getName().equals("Float")|| from.getName().equals("java.lang.Float")) && to.getName().equals("float")) {
      return true;
    }
    if ((from.getName().equals("Double")|| from.getName().equals("java.lang.Double")) && to.getName().equals("double")) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param type
   * @return a type which is unboxed
   *
   * JLS3_5.1-8
   */
  public static JavaTypeSymbolReference unbox(JavaTypeSymbolReference type) {
    if(type.getDimension() == 0) {
      if (type.getName().equals("Boolean") || type.getName().equals("java.lang.Boolean")) {
        return new JavaTypeSymbolReference("boolean", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("Byte") || type.getName().equals("java.lang.Byte")) {
        return new JavaTypeSymbolReference("byte", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("Character") || type.getName().equals("java.lang.Character")) {
        return new JavaTypeSymbolReference("char", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("Short") || type.getName().equals("java.lang.Short")) {
        return new JavaTypeSymbolReference("short", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("Integer") || type.getName().equals("java.lang.Integer")) {
        return new JavaTypeSymbolReference("int", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("Long") || type.getName().equals("java.lang.Long")) {
        return new JavaTypeSymbolReference("long", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("Float") || type.getName().equals("java.lang.Float")) {
        return new JavaTypeSymbolReference("float", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("Double") || type.getName().equals("java.lang.Double")) {
        return new JavaTypeSymbolReference("double", type.getEnclosingScope(), type.getDimension());
      }
    }
    return type;
  }


  /**
   *
   * @param type
   * @return a type which is boxed
   *
   * JLS3_5.1-7
   */
  public static JavaTypeSymbolReference box(JavaTypeSymbolReference type) {
    if(type.getDimension() == 0) {
      if (type.getName().equals("boolean")) {
        return new JavaTypeSymbolReference("java.lang.Boolean", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("byte")) {
        return new JavaTypeSymbolReference("java.lang.Byte", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("char")) {
        return new JavaTypeSymbolReference("java.lang.Character", type.getEnclosingScope(),
            type.getDimension());
      }
      if (type.getName().equals("short")) {
        return new JavaTypeSymbolReference("java.lang.Short", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("int")) {
        return new JavaTypeSymbolReference("java.lang.Integer", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("long")) {
        return new JavaTypeSymbolReference("java.lang.Long", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("float")) {
        return new JavaTypeSymbolReference("java.lang.Float", type.getEnclosingScope(), type.getDimension());
      }
      if (type.getName().equals("double")) {
        return new JavaTypeSymbolReference("java.lang.Double", type.getEnclosingScope(), type.getDimension());
      }
    }
    return type;
  }

  /**
   *
   * @param typeArg
   * @return returns a lower bound of the given type argument
   */
  public static JavaTypeSymbolReference getLowerBound(TypeReference<? extends TypeSymbol> typeArg) {
    for (int i = 0; i < typeArg.getActualTypeArguments().size(); i++) {
      if (typeArg.getActualTypeArguments().get(i).isLowerBound()) {
        return (JavaTypeSymbolReference) typeArg.getActualTypeArguments().get(i).getType();
      }
    }
    return new JavaTypeSymbolReference("null", typeArg.getEnclosingScope(), 0);
  }

  /**
   *
   * @param typeArgs
   * @return a list of upper bounds, this can be the case for intersection type
   */
  public static List<JavaTypeSymbolReference> getUpperBounds(
      TypeReference<? extends TypeSymbol> typeArgs) {
    List<JavaTypeSymbolReference> result = new ArrayList<>();
    for (ActualTypeArgument actualTypeArgument : typeArgs.getActualTypeArguments()) {
      if (actualTypeArgument.isUpperBound()) {
        result.add((JavaTypeSymbolReference) actualTypeArgument.getType());
      }
    }
    if (typeArgs.getName().equals("ASTWildcardType") && result.isEmpty()) {
      result.add(new JavaTypeSymbolReference("Object", typeArgs.getEnclosingScope(), 0));
    }
    return result;
  }

  /**
   *
   * @param typeArg
   * @return returns an upper bound of the given typeArg
   */
  public static JavaTypeSymbolReference getUpperBound(TypeReference<? extends TypeSymbol> typeArg) {
    for (int i = 0; i < typeArg.getActualTypeArguments().size(); i++) {
      if (typeArg.getActualTypeArguments().get(i).isUpperBound()) {
        return (JavaTypeSymbolReference) typeArg.getActualTypeArguments().get(i).getType();
      }
    }
    if (typeArg.getName().equals("ASTWildcardType")) {
      return new JavaTypeSymbolReference("Object", typeArg.getEnclosingScope(), 0);
    }
    return (JavaTypeSymbolReference) typeArg;
  }

  /**
   *
   * @param type
   * @param substitutedTypes
   * @return a type, which is result of applying type substitution and then capture
   */
  public static JavaTypeSymbolReference applyTypeSubstitutionAndCapture(JavaTypeSymbolReference type,
      HashMap<String, JavaTypeSymbolReference> substitutedTypes) {
    List<ActualTypeArgument> argList = new ArrayList<>();
    if (substitutedTypes == null) {
      return type;
    }
    if (substitutedTypes.isEmpty()) {
      return new JavaTypeSymbolReference(type.getName(), type.getEnclosingScope(), type.getDimension());
    }
    if (isRawType(type)) {
      return type;
    }
    if (type.getActualTypeArguments().isEmpty()) {
      return type;
    }
    JavaTypeSymbolReference resultType = capture(type);
    for (ActualTypeArgument actualTypeArgument : type.getActualTypeArguments()) {
      TypeReference<? extends TypeSymbol> argType = actualTypeArgument.getType();
      if (argType.getEnclosingScope().resolve(argType.getName(), JavaTypeSymbol.KIND).isPresent()) {
        JavaTypeSymbol argSymbol = (JavaTypeSymbol) argType.getEnclosingScope()
            .resolve(argType.getName(), JavaTypeSymbol.KIND).get();
        if (argSymbol.isTypeVariable() && substitutedTypes.containsKey(argSymbol.getName())) {
          JavaTypeSymbolReference subType = new JavaTypeSymbolReference(
              substitutedTypes.get(argSymbol.getName()).getName(),
              substitutedTypes.get(argSymbol.getName()).getEnclosingScope(), substitutedTypes.get(argSymbol.getName()).getDimension());
          ActualTypeArgument subArg = new ActualTypeArgument(actualTypeArgument.isLowerBound(),
              actualTypeArgument.isUpperBound(), subType);
          argList.add(subArg);
        }
        else {
          argList.add(actualTypeArgument);
        }
      }
    }
    resultType.setActualTypeArguments(argList);
    return resultType;
  }

  /**
   *
   * @param types
   * @return  a list of actual type arguments for the given types
   */
  public  static List<ActualTypeArgument> getActualTypeArguments(List<JavaTypeSymbolReference> types) {
    List<ActualTypeArgument> list = new ArrayList<>();
    for (JavaTypeSymbolReference type : types) {
      list.add(new ActualTypeArgument(false, true, type));
    }
    return list;
  }

  /**
   *
   * @param a
   * @param b
   * @return true if there exist a subtyping relation between two types
   */
  public  static boolean subtypingRelationExist(JavaTypeSymbolReference a, JavaTypeSymbolReference b) {
    return isSuperType(a, b) || isSuperType(b, a);
  }

  /**
   *
   * @param a list of actual type arguments
   * @param b list of actual type arguments
   * @return true if a contains b
   *
   * JLS3_4.5.1.1
   */
  public  static  boolean contains(List<ActualTypeArgument> a, List<ActualTypeArgument> b) {
    if (!a.isEmpty() && !b.isEmpty() && a.size() == b.size()) {
      for (int i = 0; i < a.size(); i++) {
        if (!contains((JavaTypeSymbolReference) a.get(i).getType(),
            (JavaTypeSymbolReference) b.get(i).getType())) {
          return false;
        }
      }
      return true;
    }
    return false;
  }


  /**
   *
   * @param a actual type argument
   * @param b actual type argument
   * @return true if a contains b
   *
   * JLS3_4.5.1.1
   */
  public  static boolean contains(TypeReference<? extends JavaTypeSymbol> a,
      TypeReference<? extends JavaTypeSymbol> b) {
    if (a == b) {
      return true;
    }
    if (areEqual(a, b)) {
      return true;
    }
    if (a.getName().equals("ASTWildcardType") && b.getName().equals("ASTWildcardType")) {
      if (getUpperBounds(a).size() != getUpperBounds(b).size()) {
        if (contains(getUpperBounds(a), getUpperBounds(b), getLowerBound(a), getLowerBound(b))) {
          return true;
        }
      }
    }
    if (b.getName().equals("ASTWildcardType")) {
      if (!getLowerBound(b).getName().equals("null")) {
        if (isSuperType((JavaTypeSymbolReference) a, getLowerBound(b))) {
          return true;
        }
        else {
          return false;
        }
      }
      List<JavaTypeSymbolReference> bounds = getUpperBounds(b);
      for (int i = 0; i < bounds.size(); i++) {
        if (isSubType((JavaTypeSymbolReference) a, bounds.get(i))) {
          if (i == bounds.size() - 1) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   *
   * @param upperA an upper bound of a
   * @param upperB an upper bound of b
   * @param lowerA a lower bound of a
   * @param lowerB a lower bound of b
   * @return true if JLS3_4.5.1.1
   */
  public  static boolean contains(List<JavaTypeSymbolReference> upperA,
      List<JavaTypeSymbolReference> upperB, JavaTypeSymbolReference lowerA,
      JavaTypeSymbolReference lowerB) {
    for (int i = 0; i < upperA.size(); i++) {
      if (!isSubType(upperA.get(i), upperB.get(i))) {
        return false;
      }
    }
    if (!lowerA.getName().equals("null") && !lowerB.getName().equals("null")) {
      if (!isSuperType(lowerA, lowerB)) {
        return false;
      }
    }
    return true;
  }

  /**
   *
   * @param primaryExpression a Primary Expression
   * @return a String which is a name of the enclosing type symbol
   */
  public  static String getEnclosingTypeSymbolName(ASTExpression primaryExpression) {
    return getEnclosingTypeSymbolName(primaryExpression.getEnclosingScope());
  }

  /**
   *
   * @param scope scope
   * @return a String which is a name of the enclosing type symbol
   */
  public  static String getEnclosingTypeSymbolName(Scope scope) {
    if (scope.isSpannedBySymbol()) {
      if (scope.getSpanningSymbol().get().isKindOf(JavaTypeSymbol.KIND)) {
        return scope.getSpanningSymbol().get().getName();
      }
    }
    if (scope.getEnclosingScope().isPresent()) {
      return getEnclosingTypeSymbolName(scope.getEnclosingScope().get());
    }
    return null;
  }

  /**
   *
   * @param scope a scope, in which the search starts
   * @param typeName a name of the type to search
   * @return a type if a type is found.
   *
   * This is necessary to search for visible type trough super types,
   * if not found in the super types then search is continued to the outer type
   */
  public static Optional<JavaTypeSymbolReference> visibleTypeSymbolFound(Scope scope, String typeName) {
    if (scope.resolve(typeName, JavaTypeSymbol.KIND).isPresent()) {
      JavaTypeSymbolReference type = new JavaTypeSymbolReference(typeName, scope, 0);
      return Optional.of(type);
    }
    String enclosingType = getEnclosingTypeSymbolName(scope);
    if (enclosingType != null) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) scope.getEnclosingScope().get()
          .resolve(enclosingType, JavaTypeSymbol.KIND).get();
      //first search inside
      for (JavaTypeSymbol innerType : typeSymbol.getInnerTypes()) {
        if (innerType.getName().equals(typeName)) {
          JavaTypeSymbolReference type = new JavaTypeSymbolReference(typeName, scope, 0);
          return Optional.of(type);
        }
      }
      //search in super classes
      for (JavaTypeSymbolReference type : typeSymbol.getSuperTypes()) {
        JavaTypeSymbol foundSymbol = containsTypeSymbol(
            getAllVisibleTypeSymbols(type.getReferencedSymbol(), typeSymbol), typeName)
            .orElse(null);
        if (foundSymbol != null) {
          JavaTypeSymbolReference foundType = new JavaTypeSymbolReference(typeName, scope, 0);
          return Optional.of(foundType);
        }
      }
      return visibleTypeSymbolFound(scope.getEnclosingScope().get(), typeName);
    }
    return Optional.empty();
  }

  /**
   *
   * @param sup a super type symbol
   * @param sub a sub type symbol
   * @return a list of type symbols that are visible though subtyping relation.
   */
  public  static List<JavaTypeSymbol> getAllVisibleTypeSymbols(JavaTypeSymbol sup, JavaTypeSymbol sub) {
    List<JavaTypeSymbol> list = new ArrayList<>();
    if (sup.getPackageName().equals(sub.getPackageName())) {
      for (JavaTypeSymbol typeSymbol : sup.getInnerTypes()) {
        if (!typeSymbol.isPrivate())
          list.add(typeSymbol);
      }
    }
    else {
      for (JavaTypeSymbol typeSymbol : sup.getInnerTypes()) {
        if (typeSymbol.isPublic() || typeSymbol.isProtected()) {
          list.add(typeSymbol);

        }
      }
    }
    return list;
  }

  /**
   *
   * @param fields a list of types
   * @param fieldName a name of the type
   * @return a type symbol if there exists a type symbol with a same name in the fialds
   */
  public  static Optional<JavaTypeSymbol> containsTypeSymbol(List<JavaTypeSymbol> fields,
      String fieldName) {
    for (JavaTypeSymbol typeSymbol : fields) {
      if (typeSymbol.getName().equals(fieldName)) {
        return Optional.of(typeSymbol);
      }
    }
    return Optional.empty();
  }

  /**
   *
   * @param scope the scope, in which the search will start
   * @param fieldName fieldName name of the field to search
   * @return this method is necessary, because fields from super types are visible to
   * the subtype as well. If the field is not found in the supertype then search is
   * continued to the outer type
   */
  public  static Optional<JavaFieldSymbol> resolveFieldInSuperType(Scope scope, String fieldName) {
    if (scope.resolveMany(fieldName, JavaFieldSymbol.KIND).size() == 1) {
      return Optional.of((JavaFieldSymbol) scope.resolve(fieldName, JavaFieldSymbol.KIND).get());
    }
    String enclosingType = getEnclosingTypeSymbolName(scope);
    if (enclosingType != null) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) scope.getEnclosingScope().get()
          .resolve(enclosingType, JavaTypeSymbol.KIND).get();
      //search in superclasses
      for(JavaFieldSymbol fieldSymbol : typeSymbol.getFields()) {
        if(fieldSymbol.getName().equals(fieldName)){
          return Optional.of(fieldSymbol);
        }
      }
      for (JavaTypeSymbolReference typeSymbolReference : typeSymbol.getSuperTypes()) {
        List<JavaFieldSymbol> fields = getAllVisibleFields(
            typeSymbolReference.getReferencedSymbol(), typeSymbol);
        if (containsField(fields, fieldName).isPresent()) {
          return containsField(fields, fieldName);
        }
      }
      //search in outer type - main purpose is to search in the super type of outer type
      return resolveFieldInSuperType(typeSymbol.getEnclosingScope(), fieldName);
    }
    return Optional.empty();
  }

  /**
   *
   * @param sup a symbol of supertype
   * @param sub a symbol of subtype
   * @return list of fields that are visible to the subtype from the supertype
   */
  public  static List<JavaFieldSymbol> getAllVisibleFields(JavaTypeSymbol sup, JavaTypeSymbol sub) {
    List<JavaFieldSymbol> list = new ArrayList<>();
    if (sup.getPackageName().equals(sub.getPackageName())) {
      for (JavaFieldSymbol fieldSymbol : sup.getFields()) {
        if (!fieldSymbol.isPrivate())
          list.add(fieldSymbol);
      }
    }
    else {
      for (JavaFieldSymbol fieldSymbol : sup.getFields()) {
        if (fieldSymbol.isPublic() || fieldSymbol.isProtected()) {
          list.add(fieldSymbol);
        }
      }
    }
    return list;
  }

  /**
   *
   * @param fields list of fieldSymbol
   * @param fieldName name of the fieldSymbol to search for
   * @return a fieldSymbol if in the list of fieldSymbols there exists a fieldSymbol
   * with the fieldName
   */
  public  static Optional<JavaFieldSymbol> containsField(List<JavaFieldSymbol> fields, String fieldName) {
    for (JavaFieldSymbol fieldSymbol : fields) {
      if (fieldSymbol.getName().equals(fieldName)) {
        return Optional.of(fieldSymbol);
      }
    }
    return Optional.empty();
  }

  /**
   *
   * @param methodSymbol
   * @return true if formal patameters of the methodSymbol is contains a type
   * which is parameterized
   */
  public  static boolean containsParametrizedType(JavaMethodSymbol methodSymbol) {
    for (JavaFieldSymbol fieldSymbol : methodSymbol.getParameters()) {
      if (!fieldSymbol.getType().getActualTypeArguments().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param scope
   * @return java.lang.Object
   */
  public  static JavaTypeSymbolReference getObjectType(Scope scope) {
    return new JavaTypeSymbolReference("java.lang.Object", scope, 0);
  }

}
