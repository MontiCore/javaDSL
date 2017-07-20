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

package typeSystemTestModels.invalid.statements;

import java.lang.Integer;
import java.lang.Double;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

public class ForEachIsValid {
  int[] n = {1,2,3};
  List<String> l = new ArrayList<>();
  List ll = new ArrayList();
  Integer integer ;
  public void  testForEach(){
    for(int nn : integer) {

    }
    for(Double d : n){

    }
    for(Integer s : l) {

    }
    for(Object o : ll){

    }
  }
}
