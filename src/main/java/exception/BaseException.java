package exception;

public class BaseException extends RuntimeException  {
  private String value;

  public BaseException(String message) {
    super(message);
  }

  public BaseException(String message, String value) {
    super(message);
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
