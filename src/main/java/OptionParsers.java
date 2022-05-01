import exception.IllegalOptionException;
import exception.InsufficientArgumentException;
import exception.TooManyArgumentsException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

class OptionParsers {
  public static ObjectParser<Boolean> bool() {
    return (arguments, option) ->
        getCurrentOptionalValuesWithExpectedSize(arguments, option, 0)
            .map(it -> true)
            .orElse(false);
  }

  public static <T> ObjectParser<T> unary(Function<String, T> valueParser, T defaultValue) {
    return (arguments, option) -> getCurrentOptionalValuesWithExpectedSize(arguments, option, 1)
        .map(it -> parseValue(valueParser, it.get(0), option)).orElse(defaultValue);
  }

  private static <T> T parseValue(Function<String, T> valueParser, String value, Option option) {
    try {
      return valueParser.apply(value);
    } catch (Exception e) {
      throw new IllegalOptionException(option.value());
    }
  }

  public static <T> ObjectParser<T[]> list (IntFunction<T[]> generator, Function<String, T> valueParser) {
    return (arguments, option) -> getCurrentOptionalValuesWithoutExpectedSize(arguments, option)
        .map(it -> it.stream().map(value -> parseValue(valueParser, value, option)).toArray(generator)).orElse(generator.apply(0));
  }

  private static Optional<List<String>> getCurrentOptionalValuesWithoutExpectedSize(List<String> arguments, Option option) {
    int index = arguments.indexOf("-" + option.value());
    return Optional.ofNullable(index == -1 ? null : getCurrentOptionalValues(arguments, index));
  }

  private static Optional<List<String>> getCurrentOptionalValuesWithExpectedSize(List<String> arguments, Option option, int expectedSize) {
    int index = arguments.indexOf("-" + option.value());
    if (index == -1) return Optional.empty();

    List<String> values = getCurrentOptionalValues(arguments, index);
    if (values.size() > expectedSize) throw new TooManyArgumentsException(option.value());
    if (values.size() < expectedSize) throw new InsufficientArgumentException(option.value());
    return Optional.of(values);
  }

  private static List<String> getCurrentOptionalValues(List<String> arguments, int index) {
    int theFollowingOptionFlag = IntStream.range(index + 1, arguments.size())
        .filter((it) -> arguments.get(it).startsWith("-"))
        .findFirst().orElse(arguments.size());
    List<String> values = arguments.subList(index + 1, theFollowingOptionFlag);
    return values;
  }
}
