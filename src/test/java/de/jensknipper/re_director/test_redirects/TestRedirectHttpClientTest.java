package de.jensknipper.re_director.test_redirects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.jensknipper.re_director.test_redirects.config.TestRedirectsClientProperties;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class TestRedirectHttpClientTest {

  public static final URI URI = java.net.URI.create("https://re-director.github.io/");

  private static final List<InetAddress> RESOLVED_ADDRESSES =
      List.of(InetAddress.getLoopbackAddress());

  private final TestRedirectsClientProperties clientProperties =
      new TestRedirectsClientProperties(300, 2, 1);

  @Test
  void call_should_work() {
    Map<String, List<String>> headers =
        Map.of("key", List.of("value"), "another-key", List.of("value"));
    TestRedirectHttpClient testRedirectHttpClient = clientRespondingWith(headers);

    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI, RESOLVED_ADDRESSES);

    assertThat(response.error()).isFalse();
    assertThat(response.headers().get("key")).contains("value");
    assertThat(response.headers().get("another-key")).contains("value");
  }

  @Test
  void max_values_reached_should_cut_values() {
    Map<String, List<String>> headers =
        Map.of("key", List.of("value", "value1", "value2"), "another-key", List.of("value"));
    TestRedirectHttpClient testRedirectHttpClient = clientRespondingWith(headers);

    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI, RESOLVED_ADDRESSES);

    assertThat(response.error()).isFalse();
    assertThat(response.headers().get("key")).containsExactly("value");
    assertThat(response.headers().get("another-key")).contains("value");
  }

  @Test
  void max_keys_reached_should_cut_keys() {
    Map<String, List<String>> headers =
        Map.of(
            "key",
            List.of("value"),
            "another-key",
            List.of("value"),
            "key3",
            List.of(""),
            "key4",
            List.of(""));
    TestRedirectHttpClient testRedirectHttpClient = clientRespondingWith(headers);

    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI, RESOLVED_ADDRESSES);

    assertThat(response.error()).isFalse();
    assertThat(response.headers().get("key")).containsExactly("value");
    assertThat(response.headers().get("another-key")).contains("value");
  }

  @Test
  void call_should_sanitize_values() {
    Map<String, List<String>> headers =
        Map.of("key", List.of("v\ra\nl\n\ru\n\re"), "another-key", List.of("value"));
    TestRedirectHttpClient testRedirectHttpClient = clientRespondingWith(headers);

    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI, RESOLVED_ADDRESSES);

    assertThat(response.error()).isFalse();
    assertThat(response.headers().get("key")).contains("value");
    assertThat(response.headers().get("another-key")).contains("value");
  }

  private TestRedirectHttpClient clientRespondingWith(Map<String, List<String>> headers) {
    Response mockResponse = mock();
    when(mockResponse.code()).thenReturn(200);
    when(mockResponse.headers()).thenReturn(headersOf(headers));

    OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(_ -> mockResponse).build();
    return new TestRedirectHttpClient(httpClient, clientProperties);
  }

  private Headers headersOf(Map<String, List<String>> headers) {
    Headers.Builder builder = new Headers.Builder();
    headers.forEach(
        (key, values) -> values.forEach(value -> builder.addUnsafeNonAscii(key, value)));
    return builder.build();
  }
}
