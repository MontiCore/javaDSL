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

import java.lang.String;

public class AssignmentCompatible {
  int n = 1;
  char c = 1;


  public void TestInvalidAssignment(){
    int[] a = {1,2,4};
    a[1] = 3;
    n = 1.2;
    c = 1 + 2 + 1.2;
    g = 2;//throw error
    getName() = 1;
  }

  public String getName(){
    int n = 1;
    double d = 1.2;
    float f = 1.2f;
    long l = 1L;

  }

}
