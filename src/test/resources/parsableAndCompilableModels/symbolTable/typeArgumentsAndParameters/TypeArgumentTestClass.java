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

package symbolTable.typeArgumentsAndParameters;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class TypeArgumentTestClass<T> {
  Consumer<?> wildcardOnly = new MyConsumer<Integer>();

  Consumer<T> typeVariableOnly = new MyConsumer<T>();

  Consumer<? super T> superTypeVariable = new MyConsumer<T>();

  Consumer<? extends T> extendsTypeVariable = new MyConsumer<T>();

  Consumer<? super String> superReferenceType = new MyConsumer<String>();

  Consumer<? extends String> extendsReferenceType = new MyConsumer<String>();

  Consumer<? super Integer[]> superIntegerArrayType = new MyConsumer<Integer[]>();

  Consumer<? extends Integer[]> extendsIntegerArrayType = new MyConsumer<Integer[]>();

  Consumer<? super int[]> superIntArrayType = new MyConsumer<int[]>();

  Consumer<? extends int[]> extendsIntArrayType = new MyConsumer<int[]>();

  Function<Collection<?>, Set<?>> multipleAndRecursiveTypeArguments = new MyFunction<Collection<?>, Set<?>>();
}

class MyConsumer<T> implements Consumer<T> {
  public void accept(T t) {
  }
}

class MyFunction<T, R> implements Function<T, R> {
  public R apply(T t) {
    return null;
  }
}
