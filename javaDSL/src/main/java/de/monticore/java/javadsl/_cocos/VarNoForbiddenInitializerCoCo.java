/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.javadsl._cocos;

import de.monticore.expressions.commonexpressions._ast.ASTBracketExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.lambdaexpressions._ast.ASTLambdaExpression;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTMethodReferenceExpression;
import de.monticore.statements.mcarraystatements._ast.ASTArrayInit;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTSimpleInit;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.types.check.SymTypeExpression;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

/**
 * The initializer of a 'var' variable must not be (JLS 14.4.1)
 * <ul>
 *   <li>an array initializer, e.g., {@code var x = {1, 2}},</li>
 *   <li>a lambda expression or a method reference, e.g., {@code var x = () -> 1}, as they
 *   require a target type,</li>
 *   <li>an expression of the null type or void, e.g., {@code var x = null}; the type has been
 *   inferred in phase 3 of the symbol table construction, hence, the type of the variable symbol
 *   is checked.</li>
 * </ul>
 */
public class VarNoForbiddenInitializerCoCo implements JavaDSLASTLocalVariableDeclarationCoCo {

  public static final String ERROR_CODE = "0x7A024";

  public static final String ERROR_MSG_FORMAT = "  'var' is not allowed with %s";

  @Override
  public void check(ASTLocalVariableDeclaration node) {
    if (node.isVar()) {
      node.getVariableDeclaratorList().forEach(this::check);
    }
  }

  protected void check(ASTVariableDeclarator declarator) {
    if (!declarator.isPresentVariableInit()) {
      return;
    }
    if (declarator.getVariableInit() instanceof ASTArrayInit arrayInit) {
      Log.error(ERROR_CODE + String.format(ERROR_MSG_FORMAT, "an array initializer"),
          arrayInit.get_SourcePositionStart(), arrayInit.get_SourcePositionEnd());
    }
    else if (declarator.getVariableInit() instanceof ASTSimpleInit simpleInit) {
      ASTExpression initializer = simpleInit.getExpression();
      getForbiddenInitializer(declarator, initializer).ifPresent(forbidden ->
          Log.error(ERROR_CODE + String.format(ERROR_MSG_FORMAT, forbidden),
              initializer.get_SourcePositionStart(), initializer.get_SourcePositionEnd()));
    }
  }

  /** A description of the initializer if it is forbidden, e.g., "a lambda expression". */
  protected Optional<String> getForbiddenInitializer(ASTVariableDeclarator declarator,
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
    if (declarator.getDeclarator().isPresentSymbol()) {
      SymTypeExpression type = declarator.getDeclarator().getSymbol().getType();
      if (type != null && (type.isNullType() || type.isVoidType())) {
        return Optional.of("an initializer of type " + type.printFullName());
      }
    }
    return Optional.empty();
  }
}
