import exception.IllegalOptionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Args {
  public static <T> T parse(Class<T> optionsClass, String... args) {
    try {
      Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];
      List<String> arguments = Arrays.asList(args);
      Parameter[] parameters = constructor.getParameters();
      Object[] values = parseOptions(parameters, arguments);

      return (T) constructor.newInstance(values);
    } catch (IllegalOptionException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Object[] parseOptions(Parameter[] parameters, List<String> arguments) {
    return Arrays.stream(parameters).map((param) -> {
      if (!param.isAnnotationPresent(Option.class)) throw new IllegalOptionException(param.getName());

      Option option = param.getAnnotation(Option.class);
      if (option.value().equals("h")) return parse(parameters);

      return PARSERS.get(param.getType()).parse(arguments, option);
    }).toArray();
  }

  public static Map<String, String> parse(Parameter[] parameters) {
    Map<String, String> helpTextMap = new HashMap<>();

    Arrays.stream(parameters).forEach((it) -> {
      if (!it.isAnnotationPresent(Option.class)) throw new IllegalOptionException(it.getName());
      Option option = it.getAnnotation(Option.class);
      helpTextMap.put("-" + option.value(), option.fullName());
    });

    return helpTextMap;
  }

  private static Map<Class<?>, ObjectParser> PARSERS = Map.of(
      boolean.class, OptionParsers.bool(),
      int.class, OptionParsers.unary(Integer::parseInt, 0),
      String.class, OptionParsers.unary(String::valueOf, ""),
      String[].class, OptionParsers.list(String[]::new, String::valueOf),
      Integer[].class, OptionParsers.list(Integer[]::new, Integer::parseInt)
  );

}
