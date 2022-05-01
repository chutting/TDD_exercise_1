import exception.BaseException;
import exception.IllegalOptionException;
import exception.InsufficientArgumentException;
import exception.TooManyArgumentsException;
import utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class OptionParsers {
  public static ObjectParser<Boolean> bool() {
    return (arguments, option, paramName) ->
        getCurrentOptionalValuesWithExpectedSize(arguments, option, 0, paramName)
            .map(it -> true)
            .orElse(false);
  }

  public static <T> ObjectParser<T> unary(Function<String, T> valueParser, T defaultValue) {
    return (arguments, option, paramName) ->
        getCurrentOptionalValuesWithExpectedSize(arguments, option, 1, paramName)
        .map(it -> parseValue(valueParser, it.get(0), option)).orElse(defaultValue);
  }

  public static <T> ObjectParser<T[]> list (IntFunction<T[]> generator, Function<String, T> valueParser) {
    return (arguments, option, paramName) -> getCurrentOptionalValues(arguments, option, paramName)
        .map(it -> it.stream().map(value -> parseValue(valueParser, value, option)).toArray(generator))
        .orElse(generator.apply(0));
  }

  public static <T> ObjectParser<T> classParser(Class<T> generatorClass) {
    return (arguments, option, paramName) -> {
      try {

        Constructor<?> constructor = generatorClass.getDeclaredConstructors()[0];
        Parameter[] parameters = constructor.getParameters();

        Map<String, Object> keyValueMap = getKeyValueMap(arguments, option, paramName, parameters);
        Object[] values = Arrays.stream(parameters).map((param) -> keyValueMap.get(param.getName())).toArray();
        return (T) constructor.newInstance(values);
      }catch (BaseException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  private static Map<String, Object> getKeyValueMap(
      List<String> arguments,
      Option option,
      String paramName,
      Parameter[] parameters
  ) {
    Map<String, Object> keyValueMap = Arrays.stream(getFlagIndexList(arguments, option, paramName))
        .mapToObj(index -> getKeyValueList(arguments, option, index))
        .filter(keyValueList -> getParameterByName(parameters, keyValueList.get(0)).isPresent())
        .collect(Collectors.toMap(
            keyValueList -> keyValueList.get(0),
            keyValueList -> parseValueOfClass(parameters, keyValueList)));
    return keyValueMap;
  }

  private static List<String> getKeyValueList(List<String> arguments, Option option, int index) {
    List<String> currentOptionalValues = getCurrentOptionalValues(arguments, index);
    checkSize(option, 1, currentOptionalValues);
    List<String> keyValueList = Arrays.asList(currentOptionalValues.get(0).split("="));
    if (keyValueList.size() == 1) throw new IllegalOptionException(keyValueList.get(0));
    return keyValueList;
  }

  private static int[] getFlagIndexList(List<String> arguments, Option option, String paramName) {
    return IntStream.range(0, arguments.size())
        .filter((it) -> arguments.get(it).equals("-" + option.value()) || arguments.get(it).equals("--" + paramName)).toArray();
  }

  private static Object parseValueOfClass(Parameter[] parameters, List<String> keyValueList) {
    String value = keyValueList.subList(1, keyValueList.size()).stream().collect(Collectors.joining("="));
    Parameter parameter = getParameterByName(parameters, keyValueList.get(0)).get();
    return Utils.parseStringToObject(value, parameter.getType());
  };

  private static Optional<Parameter> getParameterByName(Parameter[] parameters, String name) {
    return Arrays.stream(parameters).filter(parameter -> parameter.getName().equals(name)).findFirst();
  }

  private static <T> T parseValue(Function<String, T> valueParser, String value, Option option) {
    try {
      return valueParser.apply(value);
    } catch (Exception e) {
      throw new IllegalOptionException(option.value(), value);
    }
  }

  private static Optional<List<String>> getCurrentOptionalValues(List<String> arguments, Option option, String paramName) {
    int[] flagIndexList = getFlagIndexList(arguments, option, paramName);
    return Optional.ofNullable(
        flagIndexList.length == 0 ? null :
            getCurrentOptionalValues(arguments, flagIndexList[flagIndexList.length - 1]));
  }

  private static Optional<List<String>> getCurrentOptionalValuesWithExpectedSize(
      List<String> arguments,
      Option option,
      int expectedSize,
      String paramName
  ) {
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
