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

public class FieldAccess extends String {

  int j = 1;
  int m  = 1;
  int m  = 2;
  int n = this.m;
  int o = m;
  int g = 1;
  int ff = g;

  D d = D.MONDAY;
  D d = D.h;
  String[] intArr = new String[ff];

  public enum D{
    MONDAY,
    TUESDAY
  }

  public void checkArray(){
    int arrLength = intArr.length;
    int arrField = intArr.aa;
  }




}
