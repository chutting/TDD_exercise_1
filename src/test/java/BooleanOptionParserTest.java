import exception.TooManyArgumentsException;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

public class BooleanOptionParserTest {
  static Option option(String value) {
    return new Option() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Option.class;
      }

      @Override
      public String value() {
        return value;
      }
    };
  }

  @Test
  public void shouldReturnTrueWhenLoggingIsPresent() {
    assertTrue(new BooleanOptionParser().parse(Arrays.asList("-l"), option("l")));
  }

  @Test
  public void shouldThrowTooManyExceptionWhenLoggingParamsExisted() {
    TooManyArgumentsException exception = assertThrows(TooManyArgumentsException.class, () -> {
      new BooleanOptionParser().parse(Arrays.asList("-l", "logging"), option("l"));
    });
    assertEquals(exception.getMessage(), "l");
  }

  @Test
  public void shouldSetDefaultValueToFalse() {
    assertFalse(new BooleanOptionParser().parse(Arrays.asList(), option("l")));
  }
}