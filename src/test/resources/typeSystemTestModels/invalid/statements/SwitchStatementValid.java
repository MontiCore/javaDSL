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

import java.lang.String;
import java.lang.Double;

public class SwitchStatementValid {
  public void test(){
    int month = 8;
    Double d;
    String monthString;
    switch (d) {
      case 1:  monthString = "January";
        break;
      default: monthString = "Invalid month";
        break;
      default: monthString = "Another month";
        break;
    }

    WeekDay day;
    switch (day) {
      case WED:  monthString = "January";
        break;
      case MONDAY:  monthString = "January";
        break;
      case TUESDAY:  monthString = "January";
        break;
    }
  }

  public enum WeekDay {
    MONDAY,
    TUESDAY
  }
}
