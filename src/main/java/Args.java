import exception.IllegalOptionException;
import utils.Utils;

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

    return getObjectParser(parameter).parse(arguments, parameter.getAnnotation(Option.class), parameter.getName());
  }

  private static ObjectParser getObjectParser(Parameter parameter) {
    Boolean isCustomClass = !parameter.getType().isPrimitive() && !parameter.getType().getName().contains("java.lang");

    return isCustomClass ? OptionParsers.classParser(parameter.getType()) : PARSERS.get(parameter.getType());
  }

  private static Map<Class<?>, ObjectParser> PARSERS = Map.of(
      boolean.class, OptionParsers.bool(),
      int.class, OptionParsers.unary(Utils.classFunctionMap.get(Integer.class), 0),
      String.class, OptionParsers.unary(Utils.classFunctionMap.get(String.class), ""),
      String[].class, OptionParsers.list(String[]::new, Utils.classFunctionMap.get(String.class)),
      Integer[].class, OptionParsers.list(Integer[]::new, Utils.classFunctionMap.get(Integer.class))
  );
}
