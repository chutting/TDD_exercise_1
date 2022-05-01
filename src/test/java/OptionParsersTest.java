import exception.IllegalOptionException;
import exception.InsufficientArgumentException;
import exception.TooManyArgumentsException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class OptionParsersTest {
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
  public void shouldParseValueWhenFlagNameWithDashExisted() {
    assertEquals(OptionParsers.unary(Integer::parseInt, 0).parse(Arrays.asList("--port", "8080"), option("p"), "port"), 8080);
  }

  @Nested
  class UnaryOptionParserTest {
    @Test
    public void shouldParseValueIfExisted() {
      assertEquals(OptionParsers.unary(Integer::parseInt, 0).parse(Arrays.asList("-p", "8080"), option("p"), ""), 8080);
    }

    @Test
    public void shouldThrowTooManyExceptionWhenValueMoreThenOne() {
      TooManyArgumentsException exception = assertThrows(TooManyArgumentsException.class, () -> {
        OptionParsers.unary(Integer::parseInt, 0).parse(Arrays.asList("-p", "8080", "8081"), option("p"), "");
      });
      assertEquals(exception.getMessage(), "p");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-p", "-p -l"})
    public void shouldThrowInsufficientArgumentExceptionWhenValueIsNotPresent(String arguments) {
      InsufficientArgumentException exception = assertThrows(InsufficientArgumentException.class, () -> {
        OptionParsers.unary(Integer::parseInt, 0).parse(Arrays.asList(arguments.split(" ")), option("p"), "");
      });
      assertEquals(exception.getMessage(), "p");
    }

    @Test
    public void shouldSetDefaultValueTo0ForIntOption() {
      assertEquals(OptionParsers.unary(Integer::parseInt, 0).parse(Arrays.asList(), option("p"), ""), 0);
    }
  }

  @Nested
  class BooleanOptionParserTest {
    @Test
    public void shouldReturnTrueWhenLoggingIsPresent() {
      assertTrue(OptionParsers.bool().parse(Arrays.asList("-l"), option("l"), ""));
    }

    @Test
    public void shouldThrowTooManyExceptionWhenLoggingParamsExisted() {
      TooManyArgumentsException exception = assertThrows(TooManyArgumentsException.class, () -> {
        OptionParsers.bool().parse(Arrays.asList("-l", "logging"), option("l"), "");
      });
      assertEquals(exception.getMessage(), "l");
    }

    @Test
    public void shouldSetDefaultValueToFalse() {
      assertFalse(OptionParsers.bool().parse(Arrays.asList(), option("l"), ""));
    }
  }

  @Nested
  class ListOptionParser {
    @Test
    public void shouldParseListValue() {
      String[] value = OptionParsers.list(String[]::new, String::valueOf).parse(Arrays.asList("-g", "this", "is"), option("g"), "");
      assertArrayEquals(new String[]{"this", "is"}, value);
    }

    @Test
    public void shouldSetDefaultValueToEmptyArray() {
      String[] value = OptionParsers.list(String[]::new, String::valueOf).parse(Arrays.asList("-g"), option("g"), "");
      assertEquals(0, value.length);
    }

    @Test
    public void shouldNotTreatNegativeIntAsFlag() {
      Integer[] value = OptionParsers.list(Integer[]::new, Integer::parseInt).parse(Arrays.asList("-g", "-1", "2"), option("g"), "");
      assertArrayEquals(new Integer[]{-1, 2}, value);
    }

    @Test
    public void shouldThrowIllegalExceptionWhenParserThrowException() {
      Function<String, String> parser = (it) -> {
        throw new RuntimeException();
      };
      IllegalOptionException exception = assertThrows(IllegalOptionException.class, () -> {
        OptionParsers.list(String[]::new, parser).parse(Arrays.asList("-g", "this", "is"), option("g"), "");
      });
      assertEquals(exception.getMessage(), "g");
      assertEquals(exception.getValue(), "this");
    }
  }
}