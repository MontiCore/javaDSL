package typeSystemTestModels.monticore.invalid;

import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuilder;

public class AmbiguityException extends RuntimeException {

  private static final long serialVersionUID = 2754767948180345585L;

  private String[] ambiguities = new String[] {};

  public AmbiguityException() {
  }

  public AmbiguityException(String message, String... ambiguities) {  // public AmbiguityException(String message, String[] ambiguities) {
    super(message);
    this.ambiguities = ambiguities;
  }

  @Override
  public String getMessage() {
    StringBuilder builder = new StringBuilder("Ambiguities:\n");
    for (String ambiguity : ambiguities) {
      builder.append(ambiguity + "\n");
    }
 //   builder.append(super.getMessage()); the method getMessage does not exist in java6
    builder.append("Hello");
    return builder.toString();
  }
}
