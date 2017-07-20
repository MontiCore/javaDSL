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

package typeSystemTestModels.invalid.interfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;
import java.lang.Integer;

public interface InterfaceMethodSignatureNoOverrideEquivalent {

  public String getName();

  public String getName();

  public String getName();

  public void setName(String s);

  public void setName(String a);

  public void setList(List<Integer> list, ArrayList<Integer> array);

  public void setList(List<String> list, ArrayList<String> array);

  public <T extends List<?>> T get(T t);

  public <T extends List<String>> T get(T t);

  public void setNaming(List<Integer> n);

  public <T extends List<Integer> & Serializable> void setNaming(T t);

}
