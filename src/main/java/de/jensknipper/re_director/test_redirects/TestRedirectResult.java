package de.jensknipper.re_director.test_redirects;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public record TestRedirectResult(List<Step> steps, ExitCode code) {
  public static final TestRedirectResult EMPTY =
      new TestRedirectResult(List.of(), ExitCode.UNDEFINED);

  public record Step(
      String from,
      @Nullable String to,
      int httpStatusCode,
      long durationInMs,
      Map<String, List<String>> attributes) {}

  public enum ExitCode {
    UNDEFINED,
    SUCCESS,
    MAX_REDIRECTS,
    LOOP_DETECTED,
    SSRF_DETECTED,
    URI_INVALID,
    HTTP_ERROR,
    WRONG_PROTOCOL
  }
}
