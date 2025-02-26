package de.monticore.codeAdaption.evaluation.testcase_2_cd4code.Concrete;

import de.monticore.codeAdaption.evaluation.reference.entity.Person;
import de.monticore.codeAdaption.utils.Adapt;

import java.util.Date;

public class UserBuilder {
  private String lastname;
  private String firstname;

  private Date birthday;

  public UserBuilder setFirstname(String firstname) {
    this.firstname = firstname;
    return this;
  }

  public UserBuilder setLastname(String lastname) {
    this.lastname = lastname;
    return this;
  }

  public UserBuilder setBirthday(Date birthday) {
    this.birthday = birthday;
    return this;
  }

  public Date getBirthday() {
    return birthday;
  }

  public String getLastname() {
    return lastname;
  }

  public String getFirstname() {
    return firstname;
  }
@Adapt(ignore = true)
  public User build() {
    User user = new User();
    user.setLastname(lastname);
    user.setFirstname(firstname);
    user.setBirthday(birthday);

    return user;
  }
}
