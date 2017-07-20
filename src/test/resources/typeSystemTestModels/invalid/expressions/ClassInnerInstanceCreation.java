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

package typeSystemTestModels.invalid.expressions;

public class ClassInnerInstanceCreation {

  InnerClass.Day d = InnerClass.new Day() {
  };

  InnerClass.C c = InnerClass.new C(){

  };

  InnerClass.A a = InnerClass.new A();
  InnerClass.B b = InnerClass.new B();
  InnerClass.DayDay dd = InnerClass.new DayDay();
  InnerClass.E e = InnerClass.new E();

}
