/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTEnhancedForControl;
import de.monticore.java.javadsl._ast.ASTForStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTForStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class ForEachIsValid implements JavaDSLASTForStatementCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public ForEachIsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.14.2-1
  @Override public void check(ASTForStatement node) {
    if (node.getForControl() instanceof ASTEnhancedForControl) {
      ASTEnhancedForControl forControl = (ASTEnhancedForControl) node.getForControl();
      typeResolver.handle(forControl.getFormalParameter());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference formalType = typeResolver.getResult()
            .get();
        typeResolver.handle(forControl.getExpression());
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference expressionType = typeResolver
              .getResult().get();
          boolean isIterable = JavaDSLHelper.isIterableType(expressionType);
          if (expressionType.getDimension() == 0 && !isIterable) {
            Log.error(
                "0xA0907 expression of the for-each statement must be an array or Iterable.",
                node.get_SourcePositionStart());
          }
          else if (expressionType.getDimension() > 0) {
            if (!JavaDSLHelper.assignmentConversionAvailable(
                new JavaTypeSymbolReference(expressionType.getName(),
                    expressionType.getEnclosingScope(), 0), formalType)) {
              Log.error(
                  "0xA0908 variable in the for-each statement is not compatible with the expression, got : "
                      + formalType.getName() + ", expected : " + expressionType.getName() + ".",
                  node.get_SourcePositionStart());
            }
          }
          else if (isIterable) {
            if (expressionType.getActualTypeArguments().isEmpty()) {
              if (!JavaDSLHelper.assignmentConversionAvailable(
                  new JavaTypeSymbolReference(expressionType.getName(),
                      expressionType.getEnclosingScope(), 0), formalType)) {
                Log.error(
                    "0xA0908 variable in the for-each statement is not compatible with the expression, got : "
                        + formalType.getName() + ", expected : " + expressionType.getName() + ".",
                    node.get_SourcePositionStart());
              }
            }
            else if (!JavaDSLHelper.assignmentConversionAvailable(
                (JavaTypeSymbolReference) expressionType.getActualTypeArguments().get(0).getType(),
                formalType)) {
              Log.error(
                  "0xA0908 variable in the for-each statement is not compatible with the expression, got : "
                      + formalType.getName() + ", expected : " + expressionType
                      .getActualTypeArguments().get(0).getType().getName() + ".",
                  node.get_SourcePositionStart());
            }
          }

        }
      }
    }
  }
}
