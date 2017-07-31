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

package symbolTable.enums;

//This is needed for testing since java 1.7 sources are not available 
@interface Override {

}

public interface EnumViaJavaInterface {
  EnumViaJavaInterface CONSTANT1 = new EnumViaJavaInterface() {
    @Override
    public int method() {
      return 1;
    }
  };

  EnumViaJavaInterface CONSTANT2 = new EnumViaJavaInterface() {
    @Override
    public int method() {
      return 2;
    }
  };

  abstract int method();

  // possibly more methods like values(), valueOf(), ...
  // omitted due to parser bug not parsing static methods
}
