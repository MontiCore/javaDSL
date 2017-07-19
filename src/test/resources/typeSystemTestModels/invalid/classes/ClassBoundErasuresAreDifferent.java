package typeSystemTestModels.invalid.classes;

import java.util.List;
import java.lang.String;
import java.lang.Integer;

public class ClassBoundErasuresAreDifferent<T extends List<String> & List<Integer>> {

}
