import exception.IllegalOptionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgTest {
  private PrintStream originalSystemOut;
  private ByteArrayOutputStream systemOutContent;

  @BeforeEach
  void redirectSystemOutStream() {

    originalSystemOut = System.out;

    systemOutContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(systemOutContent));
  }

  @AfterEach
  void restoreSystemOutStream() {
    System.setOut(originalSystemOut);
  }

  static record MultiOptions(@Option(value = "l", fullName = "logging") boolean logging,
                             @Option(value = "p", fullName = "port") int port,
                             @Option(value = "d", fullName = "dict") String dict,
                             @Option(value = "e") String[] params,
                             @Option(value = "g", fullName = "group") String[] group,
                             @Option(value = "dc", fullName = "decimals") Integer[] decimals,
                             @Option(value = "h", fullName = "helpText") Map<String, String> help ) {}
  @Test
  public void shouldParseMultiOptions() {
    MultiOptions options = Args.parse(MultiOptions.class, "-l", "-p", "8080", "-d", "/usr/doc");
    assertTrue(options.logging());
    assertEquals(options.port(), 8080);
    assertEquals(options.dict(), "/usr/doc");
  }

  @Test
  public void shouldParseHelpTextOptions() {
    MultiOptions options = Args.parse(MultiOptions.class, "-h -l");
    assertEquals(Map.of("-l", "logging",
                    "-p", "port",
                    "-d", "dict",
                    "-e", "",
                    "-g", "group",
                    "-dc", "decimals",
                    "-h", "helpText"),
            options.help());
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
