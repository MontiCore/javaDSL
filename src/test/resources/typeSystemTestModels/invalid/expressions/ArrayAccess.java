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

public class ArrayAccess
{
  int[] n = new int[1];
  int[][] nn = new int[1][2];
  int[][][] nnn = new int[1][2][3];
  long l = 1;

  public void testArray(){
    n[1.2] = 1;
    nn[0]["String"] = 1;
    nnn[0][0][true] = 2;
    l[1] = 1;
  }
}
