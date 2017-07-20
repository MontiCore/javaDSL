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

package typeSystemTestModels.invalid.fieldsAndLocalVars;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Integer;

public class FieldInitializerAssignmentCompatible {

  List<String> list = {new ArrayList<String>(), new ArrayList<String>()};
  List<String>[] list1 = {new ArrayList<String>(), new ArrayList<String>()};
  //for error
  Capture<?> capture = new Capture();//warning

  First<Integer> s = new Third<String>();

  Capture<?> capture1 = new Capture<InputStream>();

  Capture<?> capture2 = new Capture<String>(); // error

  Capture<? extends Serializable> capture2 = new Capture<Serr>();

  Capture<? super FilterInputStream> capture3 = new Capture<String>(); //error

  List<?> list = new ArrayList<String>();

  List<? extends Integer> list = new ArrayList<String>(); //error

  CaptureExample<Object, String> cap = new CaptureExample(); //warning

  CaptureExample<Object, String> cap = new CaptureExample<Object, String>();

  Capture<String> s = new Capture<String>();

  Box<String> param = new Box();
  Box bb = param; //warning

  List<Integer> l = new ArrayList(); //warning

  Box<String> c = new CaptureExample<Object, String>();

  Map<String, List<String>> map = new HashMap(); //warning

  Integer i= 1;
  int ii = i;
  byte b = 1;
  //  Map<String, List<String>> map1 = new HashMap();

  //casting conversion
  // ChildBox childBox = new ChildBox(5);
  // Box b = childBox;

  // Arrays
  int c1 [] = 1;
  int[] c2  = 1;
  int d = {1};
  int [] e [] = {1};
}
