/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.types;

import java.util.Optional;

public class GenericTypeResolver<T> {
  Optional<T> result = Optional.empty();

  public Optional<T> getResult() {
    return result;
  }

  public void setResult(T t) {
    this.result = Optional.ofNullable(t);
  }
}
