package exception;

public class IllegalOptionException extends RuntimeException {
  public IllegalOptionException(String message) {
    super(message);
  }
}