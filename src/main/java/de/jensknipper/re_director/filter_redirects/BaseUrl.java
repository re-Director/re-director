package de.jensknipper.re_director.filter_redirects;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BaseUrl {
  private static final Logger LOG = LoggerFactory.getLogger(BaseUrl.class);

  @Nullable private final String baseUrl;
  @Nullable private final String fullUrl;

  public BaseUrl(@Nullable @Value("${re-director.base-url:}") String baseUrl) {
    Optional<URI> uri =
        Optional.ofNullable(baseUrl)
            .map(String::strip)
            .map(String::toLowerCase)
            .filter(it -> !it.isEmpty())
            .map(this::getHost);
    this.baseUrl = uri.map(URI::getHost).orElse(null);
    this.fullUrl = uri.map(URI::toString).orElse(null);
    LOG.debug("Specified base url is: '{}'", baseUrl);
  }

  @Nullable
  private URI getHost(String url) {
    try {
      if (!url.contains("://")) {
        url = "https://" + url;
      }
      return new URI(url);
    } catch (URISyntaxException _) {
      LOG.warn("Could not parse specified base url to URI: '{}'", url);
      return null;
    }
  }

  public boolean isNotHost(String normalizedHost) {
    return baseUrl != null && !baseUrl.equals(normalizedHost);
  }

  @Nullable
  public String getBaseUrl() {
    return baseUrl;
  }

  @Nullable
  public String getFullUrl() {
    return fullUrl;
  }
}
