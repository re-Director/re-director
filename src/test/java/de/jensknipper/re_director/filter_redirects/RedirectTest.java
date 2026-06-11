package de.jensknipper.re_director.filter_redirects;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.assertj.core.api.Assertions.assertThat;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.manage_redirects.ManageRedirectsService;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedirectTest {

  public static final String REQUEST_URL = "request.com";
  public static final String TARGET_URL = "http://target.com";

  @Autowired private DSLContext dsl;
  @Autowired private ManageRedirectsService manageRedirectsService;

  @LocalServerPort private int port;

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    String uniqueDb = "jdbc:sqlite:file::memdb-" + UUID.randomUUID() + ":?mode=memory&cache=shared";
    registry.add("spring.datasource.url", () -> uniqueDb);
    registry.add("re-director.base-url", () -> "http://localhost");
  }

  @BeforeEach
  void cleanup() {
    dsl.deleteFrom(REDIRECTS).execute();
    manageRedirectsService.clearCache();
  }

  @Nested
  class BaseUrlNotActive {

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
      registry.add("re-director.base-url", () -> "");
    }

    @Test
    void testRedirectNotPresent() throws IOException {
      // given
      OkHttpClient client =
          createHttpClientWithCustomDns(REQUEST_URL).followRedirects(false).build();
      Request request = new Request.Builder().url("http://" + REQUEST_URL + ":" + port).build();

      // when
      Response response = client.newCall(request).execute();

      // then
      assertThat(response.code()).isEqualTo(200);
      assertThat(response.header("Location")).isNull();

      response.close();
    }
  }

  private static Stream<Arguments> provideRedirectHttpStatusCodes() {
    return Arrays.stream(RedirectHttpStatusCode.values()).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("provideRedirectHttpStatusCodes")
  void testRedirect(RedirectHttpStatusCode statusCode) throws IOException {
    // given
    insertRedirect(REQUEST_URL, TARGET_URL, false, false, statusCode);
    OkHttpClient client = createHttpClientWithCustomDns(REQUEST_URL).followRedirects(false).build();
    Request request = new Request.Builder().url("http://" + REQUEST_URL + ":" + port).build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.isRedirect()).isTrue();
    assertThat(response.code()).isEqualTo(statusCode.getCode());
    assertThat(response.header("Location")).isEqualTo(TARGET_URL);

    response.close();
  }

  @ParameterizedTest
  @MethodSource("provideRedirectHttpStatusCodes")
  void testRedirectWithFollow(RedirectHttpStatusCode statusCode) throws IOException {
    // given
    String target = "http://localhost:" + port + "/test";
    insertRedirect(REQUEST_URL, target, false, false, statusCode);
    OkHttpClient client = createHttpClientWithCustomDns(REQUEST_URL).followRedirects(true).build();
    Request request = new Request.Builder().url("http://" + REQUEST_URL + ":" + port).build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.code()).isEqualTo(200);
    assertThat(response.body()).isNotNull();
    assertThat(response.body().string()).isEqualTo("it works!");

    response.close();
  }

  @Test
  void testRedirectWithPathAndQuery() throws IOException {
    // given
    insertRedirect(
        REQUEST_URL, TARGET_URL, false, false, RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY);
    OkHttpClient client = createHttpClientWithCustomDns(REQUEST_URL).followRedirects(false).build();
    Request request =
        new Request.Builder()
            .url("http://" + REQUEST_URL + ":" + port + "/additional-path?a=1&b=2")
            .build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.isRedirect()).isTrue();
    assertThat(response.code())
        .isEqualTo(RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY.getCode());
    assertThat(response.header("Location")).isEqualTo(TARGET_URL);

    response.close();
  }

  @Test
  void testRedirectWithPathAndQueryAndPathForwardingActive() throws IOException {
    // given
    insertRedirect(
        REQUEST_URL, TARGET_URL, true, false, RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY);
    OkHttpClient client = createHttpClientWithCustomDns(REQUEST_URL).followRedirects(false).build();
    Request request =
        new Request.Builder()
            .url("http://" + REQUEST_URL + ":" + port + "/additional-path?a=1&b=2")
            .build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.isRedirect()).isTrue();
    assertThat(response.code())
        .isEqualTo(RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY.getCode());
    assertThat(response.header("Location")).isEqualTo(TARGET_URL + "/additional-path");

    response.close();
  }

  @Test
  void testRedirectWithPathAndQueryAndQueryForwardingActive() throws IOException {
    // given
    insertRedirect(
        REQUEST_URL, TARGET_URL, false, true, RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY);
    OkHttpClient client = createHttpClientWithCustomDns(REQUEST_URL).followRedirects(false).build();
    Request request =
        new Request.Builder()
            .url("http://" + REQUEST_URL + ":" + port + "/additional-path?a=1&b=2")
            .build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.isRedirect()).isTrue();
    assertThat(response.code())
        .isEqualTo(RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY.getCode());
    assertThat(response.header("Location")).isEqualTo(TARGET_URL + "?a=1&b=2");

    response.close();
  }

  @Test
  void testRedirectWithPathAndQueryAndQueryAndPathForwardingActive() throws IOException {
    // given
    insertRedirect(
        REQUEST_URL, TARGET_URL, true, true, RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY);
    OkHttpClient client = createHttpClientWithCustomDns(REQUEST_URL).followRedirects(false).build();
    Request request =
        new Request.Builder()
            .url("http://" + REQUEST_URL + ":" + port + "/additional-path?a=1&b=2")
            .build();

    // when
    Response response = client.newCall(request).execute();

    // then
    assertThat(response.isRedirect()).isTrue();
    assertThat(response.code())
        .isEqualTo(RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY.getCode());
    assertThat(response.header("Location")).isEqualTo(TARGET_URL + "/additional-path?a=1&b=2");

    response.close();
  }

  @Test
  void testForwardToNoRedirectFoundWhenBaseUrlSet() throws IOException {
    OkHttpClient client = createHttpClientWithCustomDns(REQUEST_URL).followRedirects(false).build();
    Request request = new Request.Builder().url("http://" + REQUEST_URL + ":" + port).build();

    Response response = client.newCall(request).execute();

    assertThat(response.code()).isEqualTo(302);
    assertThat(response.header("Location")).isEqualTo("http://localhost/no-redirect-found");

    response.close();
  }

  private void insertRedirect(
      String source,
      String target,
      boolean pathForwarding,
      boolean queryForwarding,
      RedirectHttpStatusCode statusCode) {
    dsl.insertInto(REDIRECTS)
        .columns(
            REDIRECTS.SOURCE,
            REDIRECTS.TARGET,
            REDIRECTS.PATH_FORWARDING,
            REDIRECTS.QUERY_FORWARDING,
            REDIRECTS.HTTP_STATUS_CODE)
        .values(source, target, pathForwarding, queryForwarding, statusCode)
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
