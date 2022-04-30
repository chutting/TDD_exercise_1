import exception.IllegalOptionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Args {
  public static <T> T parse(Class<T> optionsClass, String... args) {
    try {
      Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];
      List<String> arguments = Arrays.asList(args);

      Object[] values = Arrays.stream(constructor.getParameters()).map((param) -> parseOption(arguments, param)).toArray();

      return (T) constructor.newInstance(values);
    } catch (IllegalOptionException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Object parseOption(List<String> arguments, Parameter parameter) {
    if (!parameter.isAnnotationPresent(Option.class)) throw new IllegalOptionException(parameter.getName());
    return getObjectParser(parameter.getType()).parse(arguments, parameter.getAnnotation(Option.class));
  }

  private static Map<Class<?>, ObjectParser> PARSERS = Map.of(
      boolean.class, OptionParsers.bool(),
      int.class, OptionParsers.unary(Integer::parseInt, 0),
      String.class, OptionParsers.unary(String::valueOf, "")
  );

  private static ObjectParser getObjectParser(Class<?> type) {
    return PARSERS.get(type);
  }

}
