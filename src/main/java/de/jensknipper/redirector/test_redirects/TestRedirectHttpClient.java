package de.jensknipper.redirector.test_redirects;

import de.jensknipper.redirector.test_redirects.config.TestRedirectsClientProperties;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TestRedirectHttpClient {
  private static final Logger LOG = LoggerFactory.getLogger(TestRedirectHttpClient.class);

  private final OkHttpClient noFollowRedirectHttpClient;
  private final TestRedirectsClientProperties clientProperties;

  public TestRedirectHttpClient(
      OkHttpClient noFollowRedirectHttpClient, TestRedirectsClientProperties clientProperties) {
    this.noFollowRedirectHttpClient = noFollowRedirectHttpClient;
    this.clientProperties = clientProperties;
  }

  public TestRedirectHttpClientResponse call(URI uri, List<InetAddress> resolvedAddresses) {
    Request request = new Request.Builder().url(HttpUrl.get(uri)).head().build();
    OkHttpClient client =
        noFollowRedirectHttpClient
            .newBuilder()
            .callTimeout(Duration.ofMillis(clientProperties.timeoutInMs()))
            .dns(_ -> resolvedAddresses)
            .build();

    long start = System.currentTimeMillis();
    try (Response response = client.newCall(request).execute()) {
      long duration = System.currentTimeMillis() - start;
      Map<String, List<String>> safeHeaders = safeHeaders(response.headers().toMultimap());
      return new TestRedirectHttpClientResponse(safeHeaders, response.code(), duration, false);
    } catch (IOException e) {
      LOG.debug("Could not perform HEAD request to '{}', error: '{}'", uri, e.getMessage());
    }
    return TestRedirectHttpClientResponse.FAULTY;
  }

  private Map<String, List<String>> safeHeaders(Map<String, List<String>> headers) {
    return headers.entrySet().stream()
        .limit(clientProperties.maxHeaderKeys())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
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
    private static final TestRedirectHttpClientResponse FAULTY =
        new TestRedirectHttpClientResponse(Map.of(), 0, 0, true);
  }
}
