/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.fieldsAndLocalVars;

import typeSystemTestModels.invalid.fieldsAndLocalVars.*;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;
import java.lang.Integer;
import java.lang.Object;

public class FieldInitializerAssignmentCompatible {

  First<Integer> s = new Third<String>();

  Capture<?> capture1 = new Capture<InputStream>();

  Capture<? extends Serializable> capture2 = new Capture<Serr>();

  List<?> list = new ArrayList<String>();

  Box<String> c = new CaptureExample<Object, String>();

  CaptureExample<Object, String> cap = new CaptureExample<Object, String>();

  Capture<String> s = new Capture<String>();
  
  // Arrays
  int c [] = {1};
  int[] d  = {1};
  int [] e [] = {{1}};


}
