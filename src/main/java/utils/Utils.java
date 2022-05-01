package utils;

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
      Boolean.class, value -> stringBoolMap.get(value)
  );

  public static <T>T parseStringToObject(String value, Class<T> type) {
    return (T) classFunctionMap.get(type).apply(value);
  }
}
