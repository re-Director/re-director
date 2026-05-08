package de.jensknipper.re_director.test_redirects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.jensknipper.re_director.test_redirects.config.TestRedirectConfiguration;
import de.jensknipper.re_director.test_redirects.config.TestRedirectsClientProperties;
import java.net.URI;
import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@WireMockTest
class TestRedirectHttpClientIT {

  public static final String URL = "/";

  private final HttpClient httpClient =
      new TestRedirectConfiguration().noFollowRedirectHttpClient();
  private final TestRedirectsClientProperties clientProperties =
      new TestRedirectsClientProperties(500, 5, 1);
  private final TestRedirectHttpClient testRedirectHttpClient =
      new TestRedirectHttpClient(httpClient, clientProperties);

  @ParameterizedTest
  @ValueSource(ints = {200, 301, 400, 401, 402, 403, 404, 500, 501, 502, 503, 504})
  void statusCodeTest(int statusCode, WireMockRuntimeInfo runtimeInfo) {
    // given
    stubFor(head(urlEqualTo(URL)).willReturn(aResponse().withStatus(statusCode)));

    // when
    String baseUrl = runtimeInfo.getHttpBaseUrl();
    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI.create(baseUrl + URL));

    // then
    assertThat(response.error()).isFalse();
    assertThat(response.statusCode()).isEqualTo(statusCode);
    assertThat(response.duration()).isGreaterThan(0);
    assertThat(response.headers()).isNotEmpty();
  }

  @Test
  void timeoutTest(WireMockRuntimeInfo runtimeInfo) {
    // given
    stubFor(
        head(urlEqualTo(URL))
            .willReturn(
                aResponse().withStatus(200).withFixedDelay(clientProperties.timeoutInMs() + 100)));

    // when
    String baseUrl = runtimeInfo.getHttpBaseUrl();
    TestRedirectHttpClient.TestRedirectHttpClientResponse response =
        testRedirectHttpClient.call(URI.create(baseUrl + URL));

    // then
    assertThat(response.error()).isTrue();
  }
}
