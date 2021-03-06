/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.fieldandlocalvars;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTArrayInitializer;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTVariableInititializerOrExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTLocalVariableDeclarationCoCo;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLArrayInitializerCollector;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.monticore.literals.literals._ast.ASTLiteral;
import de.se_rwth.commons.logging.Log;

/**

 *
 */
public class LocalVariableInitializerAssignmentCompatible implements
        JavaDSLASTLocalVariableDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public LocalVariableInitializerAssignmentCompatible(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTLocalVariableDeclaration node) {
//    node.getType().accept(typeResolver);
//    if (typeResolver.getResult().isPresent()) {
//      JavaTypeSymbolReference localVarType = typeResolver.getResult()
//          .get();
    if(node.isPresentSymbol()) {
      JavaTypeSymbolReference type = ((JavaFieldSymbol) node.getSymbol()).getType();
      JavaTypeSymbolReference localVarType = new JavaTypeSymbolReference(JavaDSLHelper.getCompleteName(type), type.getEnclosingScope(), type.getDimension());
      localVarType.setActualTypeArguments(type.getActualTypeArguments());
      JavaDSLArrayInitializerCollector arrayInitializerCollector = new JavaDSLArrayInitializerCollector();
      if (node.getVariableDeclaratorList().isEmpty()) {
        return;
      }
      else
        for (ASTVariableDeclarator variableDeclarator : node.getVariableDeclaratorList()) {
          if (JavaDSLHelper.isByteType(localVarType) || JavaDSLHelper.isCharType(localVarType)
                  || JavaDSLHelper.isShortType(localVarType)) {
            if (variableDeclarator.isPresentVariableInititializerOrExpression()) {
              if (variableDeclarator.getVariableInititializerOrExpression().isPresentExpression()) {
                ASTExpression astExpression =  variableDeclarator
                        .getVariableInititializerOrExpression().getExpression();
                if (astExpression instanceof ASTLiteral) {
                  ASTLiteral primaryExpression = (ASTLiteral) astExpression;
                  ASTLiteral literal = primaryExpression;
                  if (literal instanceof ASTIntLiteral) {
                    return;
                  }
                }
              }
            }
          }
          if (variableDeclarator.isPresentVariableInititializerOrExpression()) {
            ASTVariableInititializerOrExpression varOrExpr = variableDeclarator.getVariableInititializerOrExpression();
            if(varOrExpr.isPresentVariableInitializer() && varOrExpr.getVariableInitializer() instanceof  ASTArrayInitializer) {
              variableDeclarator.getVariableInititializerOrExpression().getVariableInitializer().accept(arrayInitializerCollector);
              List<ASTArrayInitializer> arrList = arrayInitializerCollector.getArrayInitializerList();
              if (localVarType.getDimension() == 0 && !arrList.isEmpty()) {
                Log.error("0xA0609 type mismatch, cannot convert from array to type '" + localVarType
                        .getName() + "'.", node.get_SourcePositionStart());
                return;
              }
              if (localVarType.getDimension() > 0 && arrList.size() == 0) {
                Log.error("0xA0609 type mismatch, array is expected.",
                        node.get_SourcePositionStart());
                return;
              }
              if (localVarType.getDimension() > 0 && localVarType.getDimension() == arrList.size()) {
                for (ASTArrayInitializer arrayInitializer : arrList) {
                  for(ASTVariableInititializerOrExpression variableInitializer : arrayInitializer.getVariableInititializerOrExpressionList()) {
                    typeResolver.handle(variableInitializer);
                    if (typeResolver.getResult().isPresent()) {
                      JavaTypeSymbolReference arrType = typeResolver.getResult().get();
                      if (!JavaDSLHelper.isReifiableType(arrType)) {
                        Log.error("Array component must be a reifiable type.",
                                node.get_SourcePositionStart());
                        return;
                      }
                      else {
                        JavaTypeSymbolReference componentType = JavaDSLHelper
                                .getComponentType(localVarType);
                        if (JavaDSLHelper.safeAssignmentConversionAvailable(arrType, componentType)) {
                          return;
                        }
                        else if (JavaDSLHelper
                                .unsafeAssignmentConversionAvailable(arrType, componentType)) {
                          Log.warn(
                                  "0xA0610 Possible unchecked conversion from type '" + componentType
                                          .getName()
                                          + "' to '"
                                          + localVarType.getName() + "'.", node.get_SourcePositionStart());
                          break;
                        }
                        else {
                          Log.error(
                                  "0xA0611 cannot assign a value of type '" + arrType.getName() + "' to '"
                                          + componentType
                                          .getName() + "'.", node.get_SourcePositionStart());
                          break;
                        }
                      }
                    }
                  }
                }
              }
            }
            else {
              variableDeclarator.accept(typeResolver);
              //  typeResolver.handle();
              if (typeResolver.getResult().isPresent()) {
                JavaTypeSymbolReference expType = typeResolver.getResult()
                        .get();
                //JLS3 5.2-1
                if (JavaDSLHelper.isByteType(localVarType) || JavaDSLHelper.isCharType(localVarType)
                        || JavaDSLHelper.isShortType(localVarType)) {
                  if (JavaDSLHelper.isIntType(expType)) {
                    return;
                  }
                }
                if (JavaDSLHelper.safeAssignmentConversionAvailable(expType, localVarType)) {
                  return;
                }
                //JLS3 4.4-2
                else if (JavaDSLHelper.unsafeAssignmentConversionAvailable(expType, localVarType)) {
                  Log.warn(
                          "0xA0610 Possible unchecked conversion from type '" + expType.getName()
                                  + "' to '"
                                  + localVarType.getName() + "'.", node.get_SourcePositionStart());
                }
                else {
                  Log.error(
                          "0xA0611 cannot assign a value of type '" + expType.getName() + "' to '"
                                  + localVarType
                                  .getName() + "'.", node.get_SourcePositionStart());
                }
              }
            }
          }
        }
    }
  }
}
