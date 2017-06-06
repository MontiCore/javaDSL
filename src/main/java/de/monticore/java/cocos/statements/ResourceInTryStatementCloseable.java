/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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
package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTResource;
import de.monticore.java.javadsl._ast.ASTTryStatementWithResources;
import de.monticore.java.javadsl._cocos.JavaDSLASTTryStatementWithResourcesCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 */
public class ResourceInTryStatementCloseable implements JavaDSLASTTryStatementWithResourcesCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ResourceInTryStatementCloseable(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTTryStatementWithResources node) {
    if (!node.getResources().isEmpty())
      for (ASTResource astResource : node.getResources()) {
        astResource.getType().accept(typeResolver);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference typeOfResource = typeResolver.getResult().get();
          boolean isCloseable = false;
          if (!typeOfResource.getReferencedSymbol().getInterfaces().isEmpty()) {
            for (JavaTypeSymbolReference Interface : typeOfResource.getReferencedSymbol()
                .getInterfaces()) {
              if ("Closeable".equals(Interface.getName())
                  || "java.io.Closeable".equals(Interface.getName())) {
                isCloseable = true;
              }
            }
          }
          else {
            if (typeOfResource.getReferencedSymbol().getSuperClass().isPresent()) {
              JavaTypeSymbolReference superClass = typeOfResource.getReferencedSymbol()
                  .getSuperClass().get();
              superClasses: while (!("Object".equals(superClass.getName()))) {
                if (!superClass.getReferencedSymbol().getInterfaces().isEmpty()) {
                  for (JavaTypeSymbolReference Interface : superClass.getReferencedSymbol()
                      .getInterfaces()) {
                    if ("Closeable".equals(Interface.getName())
                        || "java.io.Closeable".equals(Interface.getName())) {
                      isCloseable = true;
                      break superClasses;
                    }
                  }
                }
                if (superClass.getReferencedSymbol().getSuperClass().isPresent()) {
                  superClass = superClass.getReferencedSymbol().getSuperClass().get();
                }
              }
            }
          }
          if (!isCloseable) {
            Log.error("0xA0920 resource in try-statement must be closeable.",
                node.get_SourcePositionStart());
          }
        }
      }
    
  }
  
}
