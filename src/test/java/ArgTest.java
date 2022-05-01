import exception.IllegalOptionException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgTest {

  static record MultiOptions(@Option("l") boolean logging, @Option("p") int port, @Option("d") String dict) {}
  @Test
  public void shouldParseMultiOptions() {
    MultiOptions options = Args.parse(MultiOptions.class, "-l", "-p", "8080", "-d", "/usr/doc");
    assertTrue(options.logging());
    assertEquals(options.port(), 8080);
    assertEquals(options.dict(), "/usr/doc");
  }

  @Test
  public void shouldParseMultiOptionsWithDashedParameterName() {
    MultiOptions options = Args.parse(MultiOptions.class, "--logging", "--port", "8080", "--dict", "/usr/doc");
    assertTrue(options.logging());
    assertEquals(options.port(), 8080);
    assertEquals(options.dict(), "/usr/doc");
  }

  static record MultiOptionsWithoutAnnotation(@Option("l") boolean logging, int port, @Option("d") String dict) {}
  @Test
  public void shouldThrowIllegalOptionExceptionWhenAnnotationNotExisted() {
    IllegalOptionException exception = assertThrows(IllegalOptionException.class, () -> {
      Args.parse(MultiOptionsWithoutAnnotation.class, "-l", "-p", "8080", "-d", "/usr/doc");
    });
    assertEquals(exception.getMessage(), "port");
  }

  static record ListOptions(@Option("g") String[] group, @Option("d") Integer[] decimals) {}
  @Test
  public void shouldParseMultiListOption() {
    ListOptions options = Args.parse(ListOptions.class, "-g", "this", "is", "a", "list", "-d", "1", "2", "-3", "5");
    assertArrayEquals(new String[]{"this", "is", "a", "list"}, options.group());
    assertArrayEquals(new Integer[]{1, 2, -3, 5}, options.decimals());
  }

  static record MysqlConfiguration(Boolean MYSQL_ALLOW_EMPTY_PASSWORD, String MYSQL_DATABASE) {}
  static record Student(String name, int age) {}
  static record MultiClassOptions(@Option("e") MysqlConfiguration mysql, @Option("s") Student student) {}
  @Test
  public void shouldParseMultiClassOptions() {
    MultiClassOptions options = Args.parse(
        MultiClassOptions.class,
        "-e", "MYSQL_ALLOW_EMPTY_PASSWORD=yes", "-e", "MYSQL_DATABASE=test",
        "-s", "name=chutt", "--student", "age=20");
    MysqlConfiguration mysql = options.mysql();
    Student student = options.student();
    assertEquals(true, mysql.MYSQL_ALLOW_EMPTY_PASSWORD());
    assertEquals("test", mysql.MYSQL_DATABASE());
    assertEquals("chutt", student.name());
    assertEquals(20, student.age());
  }
}
