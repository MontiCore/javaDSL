package de.monticore.codeAdaption.evaluation.reference.builders;

import de.monticore.codeAdaption.evaluation.reference.entity.User;
import de.monticore.codeAdaption.utils.Adapt;

public class UserBuilder extends PersonBuilder {
  private String loginName;

  private String password;

  private int intData;

  public UserBuilder setIntData(int intData) {
    this.intData = intData;
    return this;
  }

  public UserBuilder setLoginName(String loginName) {
    this.loginName = loginName;
    return this;
  }

  public UserBuilder setPassword(String password) {
    this.password = password;
    return this;
  }

  @Override
  @Adapt(ignore = true)
  public User build() {
    User user = new User();
    user.setIntData(intData);
    user.setLoginName(loginName);
    user.setPassword(password);

    user.setLastname(getLastname());
    user.setFirstname(getFirstname());
    user.setBirthday(getBirthday());

    return user;
  }
}
