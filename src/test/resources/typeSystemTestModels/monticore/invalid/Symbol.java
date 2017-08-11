/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
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

package typeSystemTestModels.monticore.invalid;

import de.monticore.ast.ASTNode;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public interface Symbol {


  static String getName();

  native String getPackageName();

  final String getFullName();

  SymbolKind getKind();

//  default boolean isKindOf(SymbolKind kind) {
//    return this.getKind().isKindOf((SymbolKind) Log.errorIfNull(kind));
//  }

//  default AccessModifier getAccessModifier() {
//    return AccessModifier.ALL_INCLUSION;
//  }

  void setAccessModifier(AccessModifier var1);

  void setAstNode(ASTNode var1);

  Optional<ASTNode> getAstNode();

//  default SourcePosition getSourcePosition() {
//    return this.getAstNode().isPresent()?((ASTNode)this.getAstNode().get()).get_SourcePositionStart():SourcePosition.getDefaultSourcePosition();
//  }

  Scope getEnclosingScope();

  void setEnclosingScope(MutableScope var1);
}
