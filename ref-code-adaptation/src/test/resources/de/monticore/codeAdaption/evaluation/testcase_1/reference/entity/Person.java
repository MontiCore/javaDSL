package de.monticore.codeAdaption.evaluation.reference.entity;

import java.util.Date;

public class Person {
  private String lastname;
  private String firstname;
  private Date birthday;

  public String getLastname() {
    return lastname;
  }

  public Date getBirthday() {
    return birthday;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }
}
