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

package simpleTestClasses.types;

enum EmptyEnum {
}

enum OneSemicolon {
  ;
}

enum OneElement {
  ONE
}

enum OneElementAndSemicolon {
  ONE;
}

enum OneElementWithArgs {
  ONE(1);

  OneElementWithArgs(int i) {
  }
}

enum TwoElements {
  ONE, TWO
}

enum TwoElementsAndSemicolon {
  ONE, TWO;
}

enum TwoElementsWithArgs {
  ONE(1),
  TWO(2);

  TwoElementsWithArgs(int i) {
  }
}
