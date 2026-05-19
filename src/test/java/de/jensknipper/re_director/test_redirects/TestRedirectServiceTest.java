package de.jensknipper.re_director.test_redirects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.jensknipper.re_director.test_redirects.config.TestRedirectsProperties;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TestRedirectServiceTest {
  public static final String LOCATION_HEADER = "location";

  private final TestRedirectHttpClient testRedirectHttpClient = mock(TestRedirectHttpClient.class);
  private final TestRedirectsProperties testRedirectsProperties = new TestRedirectsProperties(2);

  private final TestRedirectService testRedirectService =
      new TestRedirectService(testRedirectHttpClient, testRedirectsProperties);

  @Test
  void call_should_handle_non_redirects() {
    // given
    String url = "https://re-director.github.io";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of("key", List.of("value")), 201, 10, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.SUCCESS);
    assertThat(result.steps()).hasSize(1);
    assertThat(result.steps().getFirst().from()).isEqualTo(url);
    assertThat(result.steps().getFirst().to()).isNull();
    assertThat(result.steps().getFirst().httpStatusCode()).isEqualTo(201);
    assertThat(result.steps().getFirst().durationInMs()).isEqualTo(10);
    assertThat(result.steps().getFirst().attributes()).hasSize(1);
    assertThat(result.steps().getFirst().attributes().get("key")).containsExactly("value");
  }

  @Test
  void call_should_handle_empty_location() {
    // given
    String url = "https://re-director.github.io";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of("")), 301, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.LOOP_DETECTED);
    assertThat(result.steps()).hasSize(1);
    assertThat(result.steps().getFirst().from()).isEqualTo(url);
    assertThat(result.steps().getFirst().to()).isEmpty();
    assertThat(result.steps().getFirst().httpStatusCode()).isEqualTo(301);
    assertThat(result.steps().getFirst().durationInMs()).isZero();
    assertThat(result.steps().getFirst().attributes()).hasSize(1);
  }

  @Test
  void call_should_handle_empty_location_list() {
    // given
    String url = "https://re-director.github.io";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of()), 301, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.SUCCESS);
    assertThat(result.steps()).hasSize(1);
    assertThat(result.steps().getFirst().from()).isEqualTo(url);
    assertThat(result.steps().getFirst().to()).isNull();
    assertThat(result.steps().getFirst().httpStatusCode()).isEqualTo(301);
    assertThat(result.steps().getFirst().durationInMs()).isZero();
    assertThat(result.steps().getFirst().attributes()).hasSize(1);
  }

  @ParameterizedTest
  @ValueSource(ints = {301, 302, 303, 307, 308})
  void call_should_handle_redirects(int statusCode) {
    // given
    String url = "https://re-director.github.io";
    String secondUrl = "https://example.com";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(secondUrl)), statusCode, 0, false));
    when(testRedirectHttpClient.call(URI.create(secondUrl)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(Map.of(), 200, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.SUCCESS);
    assertThat(result.steps()).hasSize(2);
    assertThat(result.steps().getFirst().from()).isEqualTo(url);
    assertThat(result.steps().getFirst().to()).isEqualTo(secondUrl);
    assertThat(result.steps().getFirst().httpStatusCode()).isEqualTo(statusCode);
    assertThat(result.steps().getFirst().durationInMs()).isZero();
    assertThat(result.steps().getFirst().attributes()).hasSize(1);
    assertThat(result.steps().getFirst().attributes().get(LOCATION_HEADER))
        .containsExactly(secondUrl);
  }

  @Test
  void call_should_handle_local_redirect() {
    // given
    String url = "https://re-director.github.io";
    String secondUrl = "/path";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(secondUrl)), 301, 0, false));
    when(testRedirectHttpClient.call(URI.create(url + secondUrl)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(Map.of(), 200, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.SUCCESS);
  }

  @Test
  void call_should_handle_invalid_url() {
    // given
    String url = "https://re-director.github.io";
    String secondUrl = "http://exa mple.com";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(secondUrl)), 301, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.URI_INVALID);
  }

  @Test
  void call_should_handle_invalid_local_redirect() {
    // given
    String url = "https://re-director.github.io";
    String secondUrl = "//path";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(secondUrl)), 301, 0, false));
    when(testRedirectHttpClient.call(URI.create(url + secondUrl)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(Map.of(), 200, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.SSRF_DETECTED);
  }

  @Test
  void call_should_handle_loop() {
    // given
    String url = "https://re-director.github.io";
    String secondUrl = "https://re-director.github.io/2";
    String thirdUrl = "https://re-director.github.io";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(secondUrl)), 301, 0, false));
    when(testRedirectHttpClient.call(URI.create(secondUrl)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(thirdUrl)), 301, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.MAX_REDIRECTS);
    assertThat(result.steps()).hasSize(2);
  }

  private static Stream<Arguments> provideSchemes() {
    return Stream.of(
        Arguments.of("http", TestRedirectResult.ExitCode.SUCCESS),
        Arguments.of("https", TestRedirectResult.ExitCode.SUCCESS),
        Arguments.of("ftp", TestRedirectResult.ExitCode.WRONG_PROTOCOL));
  }

  @ParameterizedTest
  @MethodSource("provideSchemes")
  void call_should_handle_protocol_validation(String scheme, TestRedirectResult.ExitCode exitCode) {
    // given
    String url = scheme + "://re-director.github.io";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(Map.of(), 200, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(exitCode);
  }

  private static Stream<Arguments> provideInvalidUrls() {
    return Stream.of(
        // loopback
        Arguments.of("https://localhost"),
        Arguments.of("https://LOCALHOST"),
        Arguments.of("https://LocalHost"),
        Arguments.of("https://localhost."),
        Arguments.of("https://127.1"),
        Arguments.of("https://[::1]"),
        Arguments.of("https://127.0.0.1"),
        Arguments.of("https://2130706433"), // decimal for 127.0.0.1
        Arguments.of("https://0x7f000001"), // hexal
        Arguments.of("https://017700000001"), // octal
        // link local
        Arguments.of("https://169.254.1.1"),
        Arguments.of("https://169.254.100.100"),
        Arguments.of("https://[fe80::1]"),
        // site local
        Arguments.of("https://[::]"),
        Arguments.of("https://0.0.0.0"),
        Arguments.of("https://10.0.0.1"),
        Arguments.of("https://172.16.0.1"),
        Arguments.of("https://172.31.255.255"),
        Arguments.of("https://172.16.0.0"),
        Arguments.of("https://172.31.255.255"),
        Arguments.of("https://192.168.0.10"),
        Arguments.of("https://192.168.1.1"),
        Arguments.of("https://10.10.10.10"),
        // Ipv6Ula
        Arguments.of("https://[fc00::1]"),
        Arguments.of("https://[fd00::1]"),
        Arguments.of("https://[fd12:3456:789a::1]"),
        Arguments.of("https://[fcff::1]"),
        // cloud meta
        Arguments.of("https://169.254.169.254"),
        Arguments.of("https://metadata.google.internal"),
        Arguments.of("https://192.0.0.192"),
        Arguments.of("https://100.100.100.200"),
        // other
        Arguments.of("https://[::ffff:127.0.0.1]"),
        Arguments.of("https://[::ffff:192.168.1.1]"),
        Arguments.of("https://0.0.0.0"),
        Arguments.of("https://[::]"),
        Arguments.of("https://[::ffff:7f00:1]"),
        // dyn dns
        Arguments.of("https://127.0.0.1.nip.io"),
        Arguments.of("https://192.168.1.1.nip.io"),
        Arguments.of("https://127.0.0.1.sslip.io"),
        Arguments.of("https://192.168.1.1.sslip.io"),
        Arguments.of("https://127.0.0.1.nip.io."),
        // cgnat
        Arguments.of("https://100.64.0.1"),
        Arguments.of("https://100.127.255.255"),
        // multicast
        Arguments.of("https://224.0.0.1"),
        Arguments.of("https://239.255.255.255"),
        Arguments.of("https://[ff02::1]"));
  }

  @ParameterizedTest
  @MethodSource("provideInvalidUrls")
  void call_should_handle_ssrf(String secondUrl) {
    // given
    String url = "https://re-director.github.io";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(secondUrl)), 301, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.SSRF_DETECTED);
  }

  private static Stream<Arguments> provideValidUrls() {
    return Stream.of(
        // loopback
        Arguments.of("https://example.com"),
        Arguments.of("https://1.1.1.1"),
        Arguments.of("https://8.8.8.8"),
        Arguments.of("https://[2606:4700:4700::1111]"),
        Arguments.of("https://172.15.255.255"),
        Arguments.of("https://172.32.0.0"),
        Arguments.of("https://100.63.255.255"),
        Arguments.of("https://100.128.0.0"));
  }

  @ParameterizedTest
  @MethodSource("provideValidUrls")
  void call_should_handle_valid_urls(String secondUrl) {
    // given
    String url = "https://re-director.github.io";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(secondUrl)), 301, 0, false));
    when(testRedirectHttpClient.call(URI.create(secondUrl)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(Map.of(), 200, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.SUCCESS);
  }

  @Test
  void call_should_handle_http_client_error() {
    // given
    String url = "https://re-director.github.io";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(Map.of(), 301, 0, true));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.HTTP_ERROR);
    assertThat(result.steps()).isEmpty();
  }

  @Test
  void call_should_handle_max_redirects() {
    // given
    String url = "https://re-director.github.io";
    String secondUrl = "https://re-director.github.io/2";
    String thirdUrl = "https://re-director.github.io/3";
    when(testRedirectHttpClient.call(URI.create(url)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(secondUrl)), 301, 0, false));
    when(testRedirectHttpClient.call(URI.create(secondUrl)))
        .thenReturn(
            new TestRedirectHttpClient.TestRedirectHttpClientResponse(
                Map.of(LOCATION_HEADER, List.of(thirdUrl)), 301, 0, false));

    // when
    TestRedirectResult result = testRedirectService.getRedirectionSteps(url);

    // then
    assertThat(result.code()).isEqualTo(TestRedirectResult.ExitCode.MAX_REDIRECTS);
    assertThat(result.steps()).hasSize(2);
  }
}
