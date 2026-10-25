package Concrete;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Teacher"}, template = "${}")
public class Teacher extends TeacherTOP {

  @Adapt(ref = {"Department"}, template = "first${}")
  public Department firstDepartment(Department fallback) {
    for (Department department : this.departments) {
      return department;
    }
    return fallback;
  }
}
