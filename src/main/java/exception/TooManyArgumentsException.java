package exception;

public class TooManyArgumentsException extends RuntimeException {
  public TooManyArgumentsException(String message) {
    super(message);
  }
}
