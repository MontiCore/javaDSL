package de.monticore.java.javadsl.types3;

import de.monticore.expressions.lambdaexpressions._ast.ASTLambdaExpression;
import de.monticore.expressions.lambdaexpressions.types3.LambdaExpressionsTypeVisitor;
import de.monticore.java.javadsl._ast.ASTLambdaBlockBody;
import de.monticore.java.utils.ReturnStatementCollector;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.SymTypeRelations;

import java.util.List;
import java.util.Optional;

/**
 * Extends the lambda type visitor by lambdas with a block body ({@code () -> { ... }}), whose
 * return type is derived from the return statements of the lambda itself (not from those of
 * nested lambdas or classes, see {@link ReturnStatementCollector}).
 */
public class JavaDSLLambdaExpressionsTypeVisitor extends LambdaExpressionsTypeVisitor {

  @Override
  public void endVisit(ASTLambdaExpression exp) {
    if (exp.getLambdaBody() instanceof ASTLambdaBlockBody blockBody) {
      blockBody.setType(calculateReturnType(blockBody));
    }
    super.endVisit(exp);
  }

  protected SymTypeExpression calculateReturnType(ASTLambdaBlockBody blockBody) {
    List<SymTypeExpression> returnTypes =
        ReturnStatementCollector.getReturnExpressions(blockBody.getMCJavaBlock()).values()
            .stream()
            .flatMap(Optional::stream)
            .map(expr -> getType4Ast().getPartialTypeOfExpr(expr))
            .toList();
    if (returnTypes.isEmpty()) {
      return SymTypeExpressionFactory.createTypeVoid();
    }
    if (returnTypes.stream().anyMatch(SymTypeExpression::isObscureType)) {
      return SymTypeExpressionFactory.createObscureType();
    }
    return SymTypeRelations.leastUpperBound(returnTypes)
        .orElseGet(SymTypeExpressionFactory::createObscureType);
  }
}
