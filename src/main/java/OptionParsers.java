import exception.InsufficientArgumentException;
import exception.TooManyArgumentsException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
        .map(it -> valueParser.apply(it.get(0))).orElse(defaultValue);
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
