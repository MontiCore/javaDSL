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

package typeSystemTestModels.valid.fieldsAndLocalVars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.lang.Object;
import java.lang.String;
import java.lang.Integer;
import java.lang.Number;
import java.lang.Double;

public class CastConversion {

  Object o = new Object();
  String s = (String) o;

  double dd = 2.4;
  int n = (int) dd;

  Number num = dd;
  double d = (Double)num;

  Number nn = new Integer(5);
  Double n = (Double)nn;

  Serializable s = (Serializable)o;

  Serializable c = (Serializable) this;

}
