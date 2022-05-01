import java.util.List;

interface ObjectParser<T> {
  T parse(List<String> arguments, Option option, String paramName);
}
