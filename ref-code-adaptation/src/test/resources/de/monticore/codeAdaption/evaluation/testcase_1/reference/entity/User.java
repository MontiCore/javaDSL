package de.monticore.codeAdaption.evaluation.reference.entity;

public class User extends Person {
  private String loginName;
  private String password;

  private int intData;

  public String getPassword() {
    return password;
  }

  public String getLoginName() {
    return loginName;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public int getIntData() {
    return intData;
  }

  public void setIntData(int intData) {
    this.intData = intData;
  }
}
