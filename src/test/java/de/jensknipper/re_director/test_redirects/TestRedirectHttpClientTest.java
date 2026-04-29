package de.jensknipper.re_director.test_redirects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.jensknipper.re_director.test_redirects.config.TestRedirectsClientProperties;
import de.jensknipper.re_director.util.HttpResponseMock;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestRedirectHttpClientTest {

  public static final URI URI = java.net.URI.create("https://re-director.github.io/");

  private final HttpClient httpClient = mock(HttpClient.class);
  private final TestRedirectsClientProperties clientProperties =
      new TestRedirectsClientProperties(300, 2, 1);
  private final TestRedirectHttpClient testRedirectHttpClient =
      new TestRedirectHttpClient(httpClient, clientProperties);

  @Test
  void call_should_work() throws Exception {
    Map<String, List<String>> headers =
        Map.of("key", List.of("value"), "another-key", List.of("value"));
    HttpResponse<Void> responseMock = new HttpResponseMock(200, headers);
    when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.discarding())))
        .thenReturn(responseMock);

    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI);

    assertThat(response.error()).isFalse();
    assertThat(response.headers().get("key")).contains("value");
    assertThat(response.headers().get("another-key")).contains("value");
  }

  @Test
  void max_values_reached_should_cut_values() throws Exception {
    Map<String, List<String>> headers =
        Map.of("key", List.of("value", "value1", "value2"), "another-key", List.of("value"));
    HttpResponse<Void> responseMock = new HttpResponseMock(200, headers);
    when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.discarding())))
        .thenReturn(responseMock);

    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI);

    assertThat(response.error()).isFalse();
    assertThat(response.headers().get("key")).containsExactly("value");
    assertThat(response.headers().get("another-key")).contains("value");
  }

  @Test
  void max_keys_reached_should_cut_keys() throws Exception {
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
    HttpResponse<Void> responseMock = new HttpResponseMock(200, headers);
    when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.discarding())))
        .thenReturn(responseMock);

    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI);

    assertThat(response.error()).isFalse();
    assertThat(response.headers().get("key")).containsExactly("value");
    assertThat(response.headers().get("another-key")).contains("value");
  }


  @Test
  void call_should_sanitize_values() throws Exception {
    Map<String, List<String>> headers =
      Map.of("key", List.of("v\ra\nl\n\ru\n\re"), "another-key", List.of("value"));
    HttpResponse<Void> responseMock = new HttpResponseMock(200, headers);
    when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.discarding())))
      .thenReturn(responseMock);

    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
      testRedirectHttpClient.call(URI);

    assertThat(response.error()).isFalse();
    assertThat(response.headers().get("key")).contains("value");
    assertThat(response.headers().get("another-key")).contains("value");
  }
}
