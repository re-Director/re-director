package de.jensknipper.re_director.test_redirects;

import de.jensknipper.re_director.test_redirects.config.TestRedirectsClientProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TestRedirectHttpClient {
  private static final Logger LOG = LoggerFactory.getLogger(TestRedirectHttpClient.class);

  private final HttpClient noFollowRedirectHttpClient;
  private final TestRedirectsClientProperties clientProperties;

  public TestRedirectHttpClient(
      HttpClient noFollowRedirectHttpClient, TestRedirectsClientProperties clientProperties) {
    this.noFollowRedirectHttpClient = noFollowRedirectHttpClient;
    this.clientProperties = clientProperties;
  }

  public TestRedirectHttpClientResponse call(URI uri) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofMillis(clientProperties.timeoutInMs()))
            .HEAD()
            .build();

    try {
      long start = System.currentTimeMillis();
      HttpResponse<Void> response =
          noFollowRedirectHttpClient.send(request, HttpResponse.BodyHandlers.discarding());
      long duration = System.currentTimeMillis() - start;
      if (response == null) {
        LOG.debug("No response from HEAD request to '{}'", uri);
        return TestRedirectHttpClientResponse.ERROR;
      }
      Map<String, List<String>> safeHeaders = safeHeaders(response.headers());
      return new TestRedirectHttpClientResponse(
          safeHeaders, response.statusCode(), duration, false);
    } catch (IOException e) {
      LOG.debug("Could not perform HEAD request to '{}', error: '{}'", uri, e.getMessage());
    } catch (InterruptedException e) {
      LOG.debug("Thread got interrupted during request to '{}', error: '{}'", uri, e.getMessage());
      Thread.currentThread().interrupt();
    }
    return TestRedirectHttpClientResponse.ERROR;
  }

  private Map<String, List<String>> safeHeaders(@Nullable HttpHeaders httpHeaders) {
    if (httpHeaders == null) {
      return Map.of();
    }
    return httpHeaders.map().entrySet().stream()
        .limit(clientProperties.maxHeaderKeys())
        .collect(
            Collectors.toMap(
                it->it.getKey().toLowerCase(),
                e ->
                    e.getValue().stream()
                        .limit(clientProperties.maxHeaderValues())
                        .map(this::sanitize)
                        .toList()));
  }

  private String sanitize(String value) {
    return value.replace("\r", "").replace("\n", "").strip();
  }

  public record TestRedirectHttpClientResponse(
      Map<String, List<String>> headers, int statusCode, long duration, boolean error) {
    private static final TestRedirectHttpClientResponse ERROR =
        new TestRedirectHttpClientResponse(Map.of(), 0, 0, true);
  }
}
