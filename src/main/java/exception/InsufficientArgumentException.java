package exception;

public class InsufficientArgumentException extends RuntimeException {
  public InsufficientArgumentException(String message) {
    super(message);
  }
}