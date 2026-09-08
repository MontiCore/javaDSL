package de.monticore.codeAdaption;

/**
 * Runtime exception for adaptation setup problems that would lead to invalid generated Java.
 */
public class CodeAdaptationException extends RuntimeException {
  public CodeAdaptationException(String message) {
    super(message);
  }

  public CodeAdaptationException(String message, Throwable cause) {
    super(message, cause);
  }
}
