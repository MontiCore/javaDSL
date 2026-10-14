/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.types3;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.javadsl._cocos.JavaDSLVarDeclarationInitializationHasCorrectType;
import de.monticore.java.javadsl._cocos.VarNoForbiddenInitializerCoCo;
import de.monticore.java.javadsl._cocos.VarResourceNoForbiddenInitializerCoCo;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.javalight.cocos.ReturnTypeAssignmentIsValid;
import de.monticore.statements.mccommonstatements.cocos.*;

/**
 * The context conditions of the MontiCore grammar library that type check statements and
 * initializers using TypeCheck3. The JavaDSL does not yet provide such a set of CoCos itself, so
 * the tests combine them to type check complete models.
 */
final class TypeCheckCoCos {

  private TypeCheckCoCos() {
  }

  static JavaDSLCoCoChecker checker() {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    // expressions
    checker.addCoCo(new ExpressionStatementIsValid());
    checker.addCoCo(new JavaDSLVarDeclarationInitializationHasCorrectType());
    checker.addCoCo(new ReturnTypeAssignmentIsValid());
    // var
    checker.addCoCo(new VarNoForbiddenInitializerCoCo());
    checker.addCoCo(new VarResourceNoForbiddenInitializerCoCo());
    // conditions
    checker.addCoCo(new IfConditionHasBooleanType());
    checker.addCoCo(new WhileConditionHasBooleanType());
    checker.addCoCo(new DoWhileConditionHasBooleanType());
    checker.addCoCo(new ForConditionHasBooleanType());
    checker.addCoCo(new ForEachIsValid());
    checker.addCoCo(new AssertIsValid());
    // switch
    checker.addCoCo(new SwitchStatementValid());
    checker.addCoCo(new SwitchCaseTypesValid());
    // exceptions and synchronization
    checker.addCoCo(new ThrowIsValid());
    checker.addCoCo(new CatchIsValid());
    checker.addCoCo(new ResourceInTryStatementCloseable());
    checker.addCoCo(new SynchronizedArgIsReftype());
    return checker;
  }

  /** Type checks the model whose symbol table has already been built. */
  static void check(IJavaDSLArtifactScope artifactScope) {
    checker().checkAll((ASTCompilationUnit) artifactScope.getAstNode());
  }
}
