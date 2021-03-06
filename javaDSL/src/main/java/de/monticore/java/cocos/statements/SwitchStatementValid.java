/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._cocos.JavaDSLASTSwitchStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class SwitchStatementValid implements JavaDSLASTSwitchStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public SwitchStatementValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override public void check(ASTSwitchStatement node) {
      typeResolver.handle(node.getExpression());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference typeSwitch = typeResolver.getResult()
            .get();
        //JLS3 14.11-1
        if (!JavaDSLHelper.isIntType(typeSwitch) &&
            !JavaDSLHelper.isCharType(typeSwitch) &&
            !JavaDSLHelper.isShortType(typeSwitch) &&
            !JavaDSLHelper.isByteType(typeSwitch) &&
            !JavaDSLHelper.isEnum(typeSwitch)) {
          Log.error(
              "0xA0917 switch expression in the switch-statement must be char, byte, short, int, Character,\n"
                  + "Byte, Short, Integer, or an enum type.", node.get_SourcePositionStart());
        }
        int defaultNum = 0;
        for (ASTSwitchBlockStatementGroup switchBlockStatementGroup : node
            .getSwitchBlockStatementGroupList()) {
          for (ASTSwitchLabel switchLabel : switchBlockStatementGroup.getSwitchLabelList()) {
            //JLS3 14.11-2
            if (switchLabel == null) {
              Log.error("0xA0914 switch-label must be not null", node.get_SourcePositionStart());
            }
            if (switchLabel instanceof ASTDefaultSwitchLabel) {
              defaultNum = defaultNum + 1;
              //JLS3 14.11-2
              if (defaultNum > 1) {
                Log.error("0xA0915 switch-statement must have at most one default label.",
                    node.get_SourcePositionStart());
              }
            }
            else {
              ASTConstantExpressionSwitchLabel constant = (ASTConstantExpressionSwitchLabel) switchLabel;
              if (JavaDSLHelper.isEnum(typeSwitch) && constant.getConstantExpression() instanceof ASTNameExpression) {
                // TODO MB 
                String enumMember = ((ASTNameExpression)constant.getConstantExpression())
                    .getName();
                JavaTypeSymbolReference memberType = new JavaTypeSymbolReference(enumMember,
                    typeSwitch.getEnclosingScope(), 0);
                //JLS3 14.11-2
                if (!JavaDSLHelper.isEnumMember(typeSwitch, memberType)) {
                  Log.error(
                      "0xA0916 type of the case-label is incompatible with the type of the switch-statement.",
                      node.get_SourcePositionStart());
                }
              }
              else {
                typeResolver.handle(constant);
                if (typeResolver.getResult().isPresent()) {
                  JavaTypeSymbolReference typeLabel = typeResolver
                      .getResult().get();
                  //JLS3 14.11-2
                  if (!JavaDSLHelper.assignmentConversionAvailable(typeLabel, typeSwitch)) {
                    Log.error(
                        "0xA0916 type of the case-label is incompatible with the type of the switch-statement.",
                        node.get_SourcePositionStart());
                  }
                }
              }
            }
          }
        }
      }
    }
  
}
