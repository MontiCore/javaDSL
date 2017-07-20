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

package typeSystemTestModels.valid.statements;

import java.lang.String;

public class SwitchStatementValid {
  public void test(){
    int month = 8;
    String monthString;
    switch (month) {
      case 1:  monthString = "January";
        break;
      case 2:  monthString = "February";
        break;
      case 3:  monthString = "March";
        break;
      case 4:  monthString = "April";
        break;
      case 5:  monthString = "May";
        break;
      case 6:  monthString = "June";
        break;
      case 7:  monthString = "July";
        break;
      case 8:  monthString = "August";
        break;
      case 9:  monthString = "September";
        break;
      case 10: monthString = "October";
        break;
      case 11: monthString = "November";
        break;
      case 12: monthString = "December";
        break;
      default: monthString = "Invalid month";
        break;
    }
  }

}
