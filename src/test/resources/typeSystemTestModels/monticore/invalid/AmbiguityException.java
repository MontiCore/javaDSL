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

package typeSystemTestModels.monticore.invalid;

import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuilder;

public class AmbiguityException extends RuntimeException {

  private static final long serialVersionUID = 2754767948180345585L;

  private String[] ambiguities = new String[] {};

  public AmbiguityException() {
  }

  public AmbiguityException(String message, String... ambiguities) {  // public AmbiguityException(String message, String[] ambiguities) {
    super(message);
    this.ambiguities = ambiguities;
  }

  @Override
  public String getMessage() {
    StringBuilder builder = new StringBuilder("Ambiguities:\n");
    for (String ambiguity : ambiguities) {
      builder.append(ambiguity + "\n");
    }
 //   builder.append(super.getMessage()); the method getMessage does not exist in java6
    builder.append("Hello");
    return builder.toString();
  }
}
