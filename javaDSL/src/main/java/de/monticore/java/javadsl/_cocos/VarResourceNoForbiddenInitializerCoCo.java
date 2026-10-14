/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.javadsl._cocos;

import de.monticore.expressions.commonexpressions._ast.ASTBracketExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.lambdaexpressions._ast.ASTLambdaExpression;
import de.monticore.java.javadsl._ast.ASTMethodReferenceExpression;
import de.monticore.java.javadsl._ast.ASTTryLocalVariableDeclaration;
import de.monticore.types.check.SymTypeExpression;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

/**
 * The initializer of a 'var' resource of a try-with-resources statement must not be
 * (JLS 14.20.3, 14.4.1), as for local variables (see {@link VarNoForbiddenInitializerCoCo})
 * <ul>
 *   <li>a lambda expression or a method reference, e.g., {@code try (var r = () -> { })}, as
 *   they require a target type,</li>
 *   <li>an expression of the null type or void, e.g., {@code try (var r = null)}; the type has
 *   been inferred in phase 3 of the symbol table construction, hence, the type of the variable
 *   symbol is checked.</li>
 * </ul>
 */
public class VarResourceNoForbiddenInitializerCoCo
    implements JavaDSLASTTryLocalVariableDeclarationCoCo {

  public static final String ERROR_CODE = "0x7A024";

  public static final String ERROR_MSG_FORMAT = "  'var' is not allowed with %s";

  @Override
  public void check(ASTTryLocalVariableDeclaration node) {
    if (node.isVar()) {
      ASTExpression initializer = node.getExpression();
      getForbiddenInitializer(node, initializer).ifPresent(forbidden ->
          Log.error(ERROR_CODE + String.format(ERROR_MSG_FORMAT, forbidden),
              initializer.get_SourcePositionStart(), initializer.get_SourcePositionEnd()));
    }
  }

  /** A description of the initializer if it is forbidden, e.g., "a lambda expression". */
  protected Optional<String> getForbiddenInitializer(ASTTryLocalVariableDeclaration node,
      ASTExpression initializer) {
    ASTExpression unwrapped = initializer;
    while (unwrapped instanceof ASTBracketExpression bracketExpression) {
      unwrapped = bracketExpression.getExpression();
    }
    if (unwrapped instanceof ASTLambdaExpression) {
      return Optional.of("a lambda expression");
    }
    if (unwrapped instanceof ASTMethodReferenceExpression) {
      return Optional.of("a method reference");
    }
    if (node.getDeclaratorId().isPresentSymbol()) {
      SymTypeExpression type = node.getDeclaratorId().getSymbol().getType();
      if (type != null && (type.isNullType() || type.isVoidType())) {
        return Optional.of("an initializer of type " + type.printFullName());
      }
    }
    return Optional.empty();
  }
}
