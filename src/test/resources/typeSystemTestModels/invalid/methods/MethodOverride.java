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

package typeSystemTestModels.invalid.methods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.Integer;
import java.lang.String;
import java.util.Collection;

public class MethodOverride extends Methods<String> {

  Object id(Object o) {
    return new Object();
  }

  @Override
  public List<Integer> getList() {
    return super.getList();
  }

  @Override
  public double size() {
    return 100;
  }

  protected void setName(){

  }

  private void setName1(){

  }

  List toList(Collection c) {
    return new ArrayList();
  }
}
