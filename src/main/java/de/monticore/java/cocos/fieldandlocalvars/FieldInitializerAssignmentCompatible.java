/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.fieldandlocalvars;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTArrayInitializer;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTVariableInititializerOrExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.literals.literals._ast.ASTLiteral;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLArrayInitializerCollector;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class FieldInitializerAssignmentCompatible implements JavaDSLASTFieldDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public FieldInitializerAssignmentCompatible(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTFieldDeclaration node) {
    node.getMCType().accept(typeResolver);
    if (!typeResolver.getResult().isPresent()) {
      Log.debug("No result in TypeResolver", FieldInitializerAssignmentCompatible.class.getSimpleName());
      return;
    }
    JavaTypeSymbolReference fieldType = typeResolver.getResult()
            .get();
    JavaDSLArrayInitializerCollector arrayInitializerCollector = new JavaDSLArrayInitializerCollector();
    for (ASTVariableDeclarator variableDeclarator : node.getVariableDeclaratorList()) {
      if (JavaDSLHelper.isByteType(fieldType) || JavaDSLHelper.isCharType(fieldType)
              || JavaDSLHelper.isShortType(fieldType)) {
        if (variableDeclarator.isPresentVariableInititializerOrExpression()
                && variableDeclarator.getVariableInititializerOrExpression() instanceof ASTExpression) {
          ASTExpression astExpression = (ASTExpression) variableDeclarator
                  .getVariableInititializerOrExpression();
          if (astExpression instanceof ASTLiteral) {
            ASTLiteral primaryExpression =(ASTLiteral) astExpression;
            ASTLiteral literal = primaryExpression;
            if (literal instanceof ASTIntLiteral) {
              return;
            }
          }
        }
      }
      int dim = fieldType.getDimension()+variableDeclarator.getDeclaratorId().getDimList().size();
      fieldType.setDimension(dim);
      String expectedArray = "";
      for (int i = 0; i < dim; i++) {
        expectedArray = expectedArray.concat("[]");
      }
      if (variableDeclarator.isPresentVariableInititializerOrExpression() && variableDeclarator.getVariableInititializerOrExpression().isPresentVariableInitializer()
              && variableDeclarator.getVariableInititializerOrExpression().getVariableInitializer() instanceof ASTArrayInitializer) {
        variableDeclarator.getVariableInititializerOrExpression().accept(arrayInitializerCollector);
        List<ASTArrayInitializer> arrList = arrayInitializerCollector
                .getArrayInitializerList();
        if (dim > 0 && (arrList.isEmpty() || (dim != arrList.size())) ||
                (dim==0 && !arrList.isEmpty())) {
          String initArray = "";
          for (int i = 0; i < arrList.size(); i++) {
            initArray = initArray.concat("[]");
          }
          Log.error("0xA0601 type mismatch, cannot convert from '"
                          + fieldType.getName() + initArray + "' to '" + fieldType.getName() + expectedArray + "'.",
                  node.get_SourcePositionStart());
          return;
        }
        if (dim > 0) {
          for (ASTArrayInitializer arrayInitializer : arrList) {
            for (ASTVariableInititializerOrExpression variableInitializer : arrayInitializer
                    .getVariableInititializerOrExpressionList()) {
              typeResolver.handle(variableInitializer);
              if (typeResolver.getResult().isPresent()) {
                JavaTypeSymbolReference arrType = JavaDSLHelper.getComponentType(typeResolver.getResult().get());
                if (!JavaDSLHelper.isReifiableType(arrType)) {
                  Log.error("0xA0613 Array component must be a reifiable type.",
                          node.get_SourcePositionStart());
                  return;
                }
                else {
                  JavaTypeSymbolReference componentType = JavaDSLHelper
                          .getComponentType(fieldType);
                  if (JavaDSLHelper.safeAssignmentConversionAvailable(arrType,
                          componentType)) {
                    return;
                  }
                  else if (JavaDSLHelper
                          .unsafeAssignmentConversionAvailable(arrType, componentType)) {
                    Log.warn(
                            "0xA0602 Possible unchecked conversion from type '" + componentType
                                    .getName()
                                    + "' to '"
                                    + fieldType.getName() + "'.",
                            node.get_SourcePositionStart());
                  }
                  else {
                    Log.error(
                            "0xA0603 cannot assign a value of array type '" + arrType.getName()
                                    + "' to '"
                                    + componentType
                                    .getName()
                                    + "'.",
                            node.get_SourcePositionStart());
                  }
                }
              }
            }
          }
        }
      } else {
        typeResolver.handle(variableDeclarator);
//        variableDeclarator.accept(typeResolver);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference expType = typeResolver.getResult()
                  .get();
          if (dim != expType.getDimension()) {
            Log.error(
                    "0xA0612  type mismatch, cannot convert from '"
                            + expType.getName()  + "' to '" + fieldType.getName() + expectedArray + "'.",
                    node.get_SourcePositionStart());
            return;
          }
          // JLS3 5.2-1
          if (JavaDSLHelper.safeAssignmentConversionAvailable(expType, fieldType)) {
            return;
          }
          // JLS3 4.4-2
          else if (JavaDSLHelper.unsafeAssignmentConversionAvailable(expType, fieldType)) {
            Log.warn(
                    "0xA0602 Possible unchecked conversion from type '" + expType.getName()
                            + "' to '"
                            + fieldType.getName() + "'.",
                    node.get_SourcePositionStart());
          }
          // JLS3 5.2.-3 (§15.28)
          else if (JavaDSLHelper.narrowingPrimitiveConversionAvailable(expType, fieldType)) {
            return;
          }
          else {
            Log.error(
                    "0xA0603 cannot assign a value of type '" + expType.getName() + "' to '"
                            + fieldType
                            .getName()
                            + "'.",
                    node.get_SourcePositionStart());
          }
        } else {
          Log.error(
                  "0xA0614 cannot assign a value to '"
                          + variableDeclarator.getDeclaratorId().getName()
                          + "'.",
                  node.get_SourcePositionStart());

        }
      }
    }
  }
}
