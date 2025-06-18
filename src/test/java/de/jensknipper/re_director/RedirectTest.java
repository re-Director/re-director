package de.jensknipper.re_director;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.assertj.core.api.Assertions.assertThat;

import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedirectTest {

  public static final String requestUrl = "request.com";
  public static final String targetUrl = "http://target.com";

  @Autowired private DSLContext dsl;

  @LocalServerPort private int port;

  @BeforeEach
  void cleanup() {
    dsl.deleteFrom(REDIRECTS).execute();
  }

  @Test
  void testRedirectNotPresent() throws IOException {
    // given
    OkHttpClient client = createHttpClientWithCustomDns(requestUrl).followRedirects(false).build();
    Request request = new Request.Builder().url("http://" + requestUrl + ":" + port).build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.code()).isIn(200);
    assertThat(response.header("Location")).isEqualTo(null);

    response.close();
  }

  private static Stream<Arguments> provideRedirectHttpStatusCodes() {
    return Arrays.stream(RedirectHttpStatusCode.values()).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("provideRedirectHttpStatusCodes")
  void testRedirect(RedirectHttpStatusCode statusCode) throws IOException {
    // given
    insertRedirect(requestUrl, targetUrl, statusCode);
    OkHttpClient client = createHttpClientWithCustomDns(requestUrl).followRedirects(false).build();
    Request request = new Request.Builder().url("http://" + requestUrl + ":" + port).build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.isRedirect()).isTrue();
    assertThat(response.code()).isEqualTo(statusCode.getCode());
    assertThat(response.header("Location")).isEqualTo(targetUrl);

    response.close();
  }

  @ParameterizedTest
  @MethodSource("provideRedirectHttpStatusCodes")
  void testRedirectWithFollow(RedirectHttpStatusCode statusCode) throws IOException {
    // given
    String target = "http://localhost:" + port + "/test";
    insertRedirect(requestUrl, target, statusCode);
    OkHttpClient client = createHttpClientWithCustomDns(requestUrl).followRedirects(true).build();
    Request request = new Request.Builder().url("http://" + requestUrl + ":" + port).build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.code()).isEqualTo(200);
    assertThat(response.body()).isNotNull();
    assertThat(response.body().string()).isEqualTo("it works!");

    response.close();
  }

  private void insertRedirect(String source, String target, RedirectHttpStatusCode statusCode) {
    dsl.insertInto(REDIRECTS)
        .columns(REDIRECTS.SOURCE, REDIRECTS.TARGET, REDIRECTS.HTTP_STATUS_CODE)
        .values(source, target, statusCode)
        .execute();
  }

  private OkHttpClient.Builder createHttpClientWithCustomDns(String... requestUrls) {
    Map<String, String> customDnsMap =
        Arrays.stream(requestUrls).collect(Collectors.toMap(s -> s, s -> "localhost"));

    Dns customDns =
        hostname -> {
          if (customDnsMap.containsKey(hostname)) {
            try {
              return List.of(InetAddress.getByName(customDnsMap.get(hostname)));
            } catch (UnknownHostException e) {
              throw new RuntimeException("Failed to resolve " + hostname, e);
            }
          }
          return Dns.SYSTEM.lookup(hostname);
        };

    return new OkHttpClient.Builder().dns(customDns);
  }
}
