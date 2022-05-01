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
    return (arguments, option, paramName) ->
        getCurrentOptionalValuesWithExpectedSize(arguments, option, 0, paramName)
            .map(it -> true)
            .orElse(false);
  }

  public static <T> ObjectParser<T> unary(Function<String, T> valueParser, T defaultValue) {
    return (arguments, option, paramName) -> getCurrentOptionalValuesWithExpectedSize(arguments, option, 1, paramName)
        .map(it -> parseValue(valueParser, it.get(0), option)).orElse(defaultValue);
  }

  public static <T> ObjectParser<T[]> list (IntFunction<T[]> generator, Function<String, T> valueParser) {
    //generator: 是参数数组 向 最后结果的转换
    //valueParser：参数个体  向  不同类型的转换
    return (arguments, option, paramName) -> getCurrentOptionalValues(arguments, option, paramName)
        .map(it -> it.stream().map(value -> parseValue(valueParser, value, option)).toArray(generator)).orElse(generator.apply(0));
  }

  private static <T> T parseValue(Function<String, T> valueParser, String value, Option option) {
    try {
      return valueParser.apply(value);
    } catch (Exception e) {
      throw new IllegalOptionException(option.value(), value);
    }
  }

  private static Optional<List<String>> getCurrentOptionalValues(List<String> arguments, Option option, String paramName) {
    int index = Math.max(arguments.indexOf("-" + option.value()), arguments.indexOf("--" + paramName));
    return Optional.ofNullable(index == -1 ? null : getCurrentOptionalValues(arguments, index));
  }

  private static Optional<List<String>> getCurrentOptionalValuesWithExpectedSize(List<String> arguments, Option option, int expectedSize, String paramName) {
    return getCurrentOptionalValues(arguments, option, paramName).map(it -> checkSize(option, expectedSize, it));
  }

  private static List<String> checkSize(Option option, int expectedSize, List<String> values) {
    if (values.size() > expectedSize) throw new TooManyArgumentsException(option.value());
    if (values.size() < expectedSize) throw new InsufficientArgumentException(option.value());
    return values;
  }

  private static List<String> getCurrentOptionalValues(List<String> arguments, int index) {
    int theFollowingOptionFlag = IntStream.range(index + 1, arguments.size())
        .filter((it) -> arguments.get(it).matches("^-[a-zA-Z-]+$"))
        .findFirst().orElse(arguments.size());
    return arguments.subList(index + 1, theFollowingOptionFlag);
  }
}
