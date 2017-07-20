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
import java.lang.Integer;
import java.util.List;

public class InstanceOf {

  public void testInstanceOf(){
    Integer storage;
    char c = 'a';
    if(c instanceof int) {

    }
    if(storage instanceof List<Integer>) {

    }
    if(storage instanceof String) {

    }

  }
}
