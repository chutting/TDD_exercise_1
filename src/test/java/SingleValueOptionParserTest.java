import exception.InsufficientArgumentException;
import exception.TooManyArgumentsException;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SingleValueOptionParserTest {
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
  public void shouldParseValueIfExisted() {
    assertEquals(new SingleValueOptionParser<>(Integer::parseInt, 0).parse(Arrays.asList("-p", "8080"), option("p")), 8080);
  }

  @Test
  public void shouldThrowTooManyExceptionWhenValueMoreThenOne() {
    TooManyArgumentsException exception = assertThrows(TooManyArgumentsException.class, () -> {
      new SingleValueOptionParser<>(Integer::parseInt, 0).parse(Arrays.asList("-p", "8080", "8081"), option("p"));
    });
    assertEquals(exception.getMessage(), "p");
  }

  @ParameterizedTest
  @ValueSource(strings = {"-p", "-p -l"})
  public void shouldThrowInsufficientArgumentExceptionWhenValueIsNotPresent(String arguments) {
    InsufficientArgumentException exception = assertThrows(InsufficientArgumentException.class, () -> {
      new SingleValueOptionParser<>(Integer::parseInt, 0).parse(Arrays.asList(arguments.split(" ")), option("p"));
    });
    assertEquals(exception.getMessage(), "p");
  }

  @Test
  public void shouldSetDefaultValueTo0ForIntOption() {
    assertEquals(new SingleValueOptionParser<>(Integer::parseInt, 0).parse(Arrays.asList(), option("p")), 0);
  }
}