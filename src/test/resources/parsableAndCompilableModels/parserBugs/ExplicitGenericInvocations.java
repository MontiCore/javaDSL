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

package parserBugs;

import java.util.HashSet;
import java.util.Set;

class SomeSuperClass {
  <T extends Set<?>> SomeSuperClass(int x) {
  }

  protected <T extends Number> void explicitGenericInvocation(int i) {
  }
}

public class ExplicitGenericInvocations extends SomeSuperClass {
  public ExplicitGenericInvocations() {
    <HashSet<?>>super(123);
    this.<Integer>explicitGenericInvocation(123);
  }

  class InnerClass {
    InnerClass() {
      // The following line is a corner case that can not be parsed
      ExplicitGenericInvocations.super.<Integer>explicitGenericInvocation(123);
      // The parser fails here         ^
    }
  }
}
