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

package typeSystemTestModels.monticore.valid;

import java.lang.String;

public class TypeSymbolKind implements SymbolKind {
  private static final String NAME = "de.monticore.symboltable.types.TypeSymbolKind";

  protected TypeSymbolKind() {
  }

  public String getName() {
    return "de.monticore.symboltable.types.TypeSymbolKind";
  }

  public boolean isKindOf(SymbolKind kind) {
   // return "de.monticore.symboltable.types.TypeSymbolKind".equals(kind.getName()) || super.isKindOf(kind);
    return true;
  }
}
