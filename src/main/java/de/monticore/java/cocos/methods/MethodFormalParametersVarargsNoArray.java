/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTLastFormalParameter;
import de.monticore.java.javadsl._cocos.JavaDSLASTLastFormalParameterCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author: sinkovec $
 * @since TODO: add version number
 */
public class MethodFormalParametersVarargsNoArray implements JavaDSLASTLastFormalParameterCoCo {
  
  public static final String ERROR_MESSAGE = "Variable argument %s must not be an array";
  
  @Override
  public void check(ASTLastFormalParameter node) {
    if(!node.getDeclaratorId().getDim().isEmpty()) {
      Log.error(String.format(ERROR_MESSAGE, node.getDeclaratorId().getName()), 
          node.get_SourcePositionStart());
    }
  }
}
