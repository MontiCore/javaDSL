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

import java.io.Serializable;
import java.lang.Object;
import java.lang.String;
import java.lang.Number;
import java.lang.Double;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.Cloneable;

public class CastConversion {

  Object o = new Object();
  String s = (String) o;

  double dd = 2.4;
  int n = (int) dd;

  Number num = dd;
  double d = (Double)num;

  String oo = "str";
  Integer str = (Integer)oo; //error

  Number nn = new Integer(5);
  Double dd = (Double)nn;

  ArrayInitializer a = (ArrayInitializer) AdditiveOps; //error

  Serializable s = (Serializable)oo;

  Serializable c = (Serializable) this;

  Map<String, Integer> map = (Map<String, Integer>) o; //warning

  ArrayList ar = new ArrayList();
  ArrayList<String> arrrr = (ArrayList<String>)ar; //warning

  public void test(){
    int[] n = new int[10];
    Object obj = (Object) n;
    s = (Serializable)n;
    Cloneable cl = (Cloneable)n;

    List l = (List) n; //error
  }
}
