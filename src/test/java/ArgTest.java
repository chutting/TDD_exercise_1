import exception.IllegalOptionException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgTest {

  static record MultiOptions(@Option(value = "l", fullName = "logging") boolean logging,
                             @Option(value = "p", fullName = "port") int port,
                             @Option(value = "d", fullName = "dict") String dict,
                             @Option(value = "e") String[] params,
                             @Option("g") String[] group,
                             @Option("dc") Integer[] decimals) {}
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

  @Test
  public void shouldParseMultiListOption() {
    MultiOptions options = Args.parse(MultiOptions.class, "-g", "this", "is", "a", "list", "-dc", "1", "2", "-3", "5");
    assertArrayEquals(new String[]{"this", "is", "a", "list"}, options.group());
    assertArrayEquals(new Integer[]{1, 2, -3, 5}, options.decimals());
  }

  @Test
  public void shouldParseDuplicatedFlagOptions() {
    MultiOptions options = Args.parse(
            MultiOptions.class,
            "-e", "MYSQL_ALLOW_EMPTY_PASSWORD=yes", "-e", "MYSQL_DATABASE=test", "-g", "this", "is", "a", "list");
    assertArrayEquals(new String[]{"MYSQL_ALLOW_EMPTY_PASSWORD=yes", "MYSQL_DATABASE=test"}, options.params());
    assertArrayEquals(new String[]{"this", "is", "a", "list"}, options.group());
  }
}
