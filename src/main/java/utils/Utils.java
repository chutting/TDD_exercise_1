package utils;

import exception.IllegalOptionException;

import java.util.Map;
import java.util.function.Function;

public class Utils {
  private static Map<String, Boolean> stringBoolMap = Map.of(
      "yes", true,
      "true", true,
      "no", false,
      "false", false
  );

  public static Map<Class<?>, Function<String, Object>> classFunctionMap = Map.of(
      int.class, Integer::parseInt,
      Integer.class, Integer::parseInt,
      String.class, String::valueOf,
      Boolean.class, value -> {
        if (!stringBoolMap.containsKey(value)) throw new IllegalOptionException(value);
        return stringBoolMap.get(value);
      }
  );

  public static <T>T parseStringToObject(String value, Class<T> type) {
    try {
      return (T) classFunctionMap.get(type).apply(value);
    } catch (IllegalOptionException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalOptionException(e.getMessage());
    }
  }
}
