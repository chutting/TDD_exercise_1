package exception;

public class IllegalOptionException extends RuntimeException {
  private String value;

  public IllegalOptionException(String message) {
    super(message);
  }

  public IllegalOptionException(String message, String value) {
    super(message);
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}