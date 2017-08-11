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

package typeSystemTestModels.valid.methods;

import typeSystemTestModels.valid.interfaces.C;
import typeSystemTestModels.valid.methods.A;

public class B extends C {

//  int n = getString();
 // int nn = getString(1);

  public class BB extends A {
   // int n  = getString();
    int nn = getName();
//    String s = getHaha();
//    int nnn = getName(1,2,3);
//    int nnnn = getString();
//    int nnnnn = getString(1);
   // int nnnnnn = getString();
  }

  public String getHaha(){
    return "haha";
  }

  public String getHaha(String haha) {
    return "haha";
  }

  public String getHaha(String... haha) {
    return "haha";
  }
}
