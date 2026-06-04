package de.jensknipper.re_director.test_redirects;

import de.jensknipper.re_director.test_redirects.config.TestRedirectsProperties;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TestRedirectService {
  private static final Logger LOG = LoggerFactory.getLogger(TestRedirectService.class);

  private static final List<Integer> REDIRECT_CODES = List.of(301, 302, 303, 307, 308);
  public static final List<String> ALLOWED_URI_SCHEMES = List.of("http", "https");
  public static final List<String> CLOUD_META_ADDRESSES =
      List.of("169.254.169.254", "metadata.google.internal", "100.100.100.200", "192.0.0.192");
  public static final String LOCATION_HEADER = "Location";

  private final TestRedirectHttpClient testRedirectHttpClient;
  private final TestRedirectsProperties testRedirectsProperties;

  public TestRedirectService(
      TestRedirectHttpClient testRedirectHttpClient,
      TestRedirectsProperties testRedirectsProperties) {
    this.testRedirectHttpClient = testRedirectHttpClient;
    this.testRedirectsProperties = testRedirectsProperties;
  }

  public TestRedirectResult getRedirectionSteps(String urlToTest) {
    List<TestRedirectResult.Step> result = new ArrayList<>();
    Set<URI> visited = new HashSet<>();

    for (int i = 0; i < testRedirectsProperties.maxRedirects(); i++) {

      URI uri = resolveUrl(urlToTest, result);
      if (uri == null) {
        return new TestRedirectResult(result, TestRedirectResult.ExitCode.URI_INVALID);
      }
      if (!visited.add(uri)) {
        LOG.debug("Loop detected for URL: {}", uri);
        return new TestRedirectResult(result, TestRedirectResult.ExitCode.LOOP_DETECTED);
      }
      if (!isSchemeValid(uri)) {
        LOG.debug("URL has invalid scheme: {}", uri);
        return new TestRedirectResult(result, TestRedirectResult.ExitCode.WRONG_PROTOCOL);
      }
      if (isSSRF(uri)) {
        LOG.debug("URL is not allowed: {}", uri);
        return new TestRedirectResult(result, TestRedirectResult.ExitCode.SSRF_DETECTED);
      }

      TestRedirectHttpClient.TestRedirectHttpClientResponse response =
          testRedirectHttpClient.call(uri);

      if (response.error()) {
        LOG.debug("HTTP client caught an error for URL: {}", uri);
        return new TestRedirectResult(result, TestRedirectResult.ExitCode.HTTP_ERROR);
      }

      String location =
          Stream.of(
                  response.headers().get(LOCATION_HEADER),
                  response.headers().get(LOCATION_HEADER.toLowerCase()))
              .filter(Objects::nonNull)
              .flatMap(Collection::stream)
              .findFirst()
              .orElse(null);
      result.add(
          new TestRedirectResult.Step(
              uri.toString(),
              location,
              response.statusCode(),
              response.duration(),
              response.headers()));

      if (!isRedirect(response.statusCode()) || location == null || location.isBlank()) {
        return new TestRedirectResult(result, TestRedirectResult.ExitCode.SUCCESS);
      }

      urlToTest = location;
    }
    LOG.debug(
        "Maximum redirects ({}) reached for URL: {}",
        testRedirectsProperties.maxRedirects(),
        urlToTest);
    return new TestRedirectResult(result, TestRedirectResult.ExitCode.MAX_REDIRECTS);
  }

  private boolean isRedirect(int code) {
    return REDIRECT_CODES.contains(code);
  }

  @Nullable
  private URI resolveUrl(String url, List<TestRedirectResult.Step> location) {
    try {
      if (location.isEmpty()) {
        return URI.create(url);
      }
      return URI.create(location.getLast().from()).resolve(url);
    } catch (IllegalArgumentException e) {
      LOG.debug("Could not parse URL: {}, error: {}", url, e.getMessage());
      return null;
    }
  }

  private boolean isSchemeValid(URI uri) {
    String scheme = uri.getScheme();
    return ALLOWED_URI_SCHEMES.contains(scheme);
  }

  private boolean isSSRF(URI url) {
    try {
      String host = url.getHost();
      InetAddress[] addresses = InetAddress.getAllByName(host);

      for (InetAddress addr : addresses) {
        if (addr.isAnyLocalAddress()
            || addr.isLoopbackAddress()
            || addr.isSiteLocalAddress()
            || addr.isLinkLocalAddress()
            || addr.isMulticastAddress()
            || isIpv6Ula(addr)
            || isCarrierGradeNat(addr)
            || isCloudMetadata(addr)) {
          return true;
        }
      }
    } catch (UnknownHostException e) {
      LOG.debug("Exception during SSRF check for URL: {}, error: {}", url, e.getMessage());
      return true;
    }
    return false;
  }

  private static boolean isIpv6Ula(InetAddress addr) {
    byte[] bytes = addr.getAddress();

    return bytes.length == 16 && (bytes[0] & (byte) 0xFE) == (byte) 0xFC;
  }

  private static boolean isCarrierGradeNat(InetAddress addr) {
    byte[] bytes = addr.getAddress();

    // IPv4 only
    if (bytes.length != 4) {
      return false;
    }

    int first = bytes[0] & 0xFF;
    int second = bytes[1] & 0xFF;

    // 100.64.0.0/10
    return first == 100 && second >= 64 && second <= 127;
  }

  private boolean isCloudMetadata(InetAddress addr) {
    return CLOUD_META_ADDRESSES.contains(addr.getHostAddress());
  }
}
