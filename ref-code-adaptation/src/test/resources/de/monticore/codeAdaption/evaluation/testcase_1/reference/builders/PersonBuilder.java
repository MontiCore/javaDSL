package de.monticore.codeAdaption.evaluation.reference.builders;

import de.monticore.codeAdaption.evaluation.reference.entity.Person;
import de.monticore.codeAdaption.utils.Adapt;

import java.util.Date;

public class PersonBuilder {
  private String lastname;
  private String firstname;

  private Date birthday;

  public PersonBuilder setFirstname(String firstname) {
    this.firstname = firstname;
    return this;
  }

  public PersonBuilder setLastname(String lastname) {
    this.lastname = lastname;
    return this;
  }

  public PersonBuilder setBirthday(Date birthday) {
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
  public Person build() {
    Person person = new Person();
    person.setLastname(lastname);
    person.setFirstname(firstname);
    person.setBirthday(birthday);

    return person;
  }
}
