package exception;

public class IllegalOptionException extends BaseException {
  public IllegalOptionException(String message) {
    super(message);
  }

  public IllegalOptionException(String message, String value) {
    super(message, value);
  }
}