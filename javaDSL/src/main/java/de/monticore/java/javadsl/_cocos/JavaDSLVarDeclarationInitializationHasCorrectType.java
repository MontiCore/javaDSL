/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.javadsl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTSimpleInit;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.statements.mcvardeclarationstatements._cocos.VarDeclarationInitializationHasCorrectType;
import de.monticore.types.check.SymTypeExpression;
import de.se_rwth.commons.logging.Log;

/**
 * {@link VarDeclarationInitializationHasCorrectType} that skips 'var' declarations: the type of a
 * 'var' variable is the type of its initializer, hence, there is nothing to check. Moreover, the
 * initializer has already been type checked without target type in phase 3 of the symbol table
 * construction (JLS 14.4.1), i.e., type checking it again with the type of the variable as target
 * type causes the internal error 0xFD111 of the type check.
 */
public class JavaDSLVarDeclarationInitializationHasCorrectType
    extends VarDeclarationInitializationHasCorrectType {

  @Override
  public void check(ASTVariableDeclarator node) {
    if (!node.isPresentVariableInit() || !(node.getVariableInit() instanceof ASTSimpleInit)) {
      return; // We can only check initializations of the form of expressions (as defined in SimpleInit).
    }
    if (!node.getDeclarator().isPresentSymbol()) {
      Log.error(String.format("Could not find a symbol for variable '%s', thus can not check coco '%s'. Check " +
              "whether you have run the symbol table creation before running this coco.",
          node.getDeclarator().getName(), this.getClass().getSimpleName()), node.get_SourcePositionStart(), node.get_SourcePositionEnd());

    } else { // Proceed with checking the coco
      SymTypeExpression varType = node.getDeclarator().getSymbol().getType();

      // The variable is declared with 'var' iff its type has been derived from an expression (its
      // initializer, see JavaDSLScopesGenitorP3), as the type check stores the expression as
      // source of the derived type, whereas explicitly declared types have their MCType as source
      if (varType != null && varType.getSourceInfo().getSourceNode().isPresent()
          && varType.getSourceInfo().getSourceNode().get() instanceof ASTExpression) {
        return;
      }
      super.check(node);
    }
  }
}
