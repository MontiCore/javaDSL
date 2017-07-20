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

package symbolTable.typeArgumentsAndParameters;

interface SomeInterface {
}

public class TypeParameterTestClass<T extends SomeClass & SomeInterface, S extends AnotherInterface<S>> {
  <U> U genericMethod(U u) {
    return u;
  }

  class InnerClass<E extends T> {
    E method() {
      return null;
    }
  }
}

class SomeClass {
}

class AnotherInterface<T> {
}
