package de.jensknipper.re_director.auth;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "re-director.auth")
public record AuthProperties(@Nullable List<String> additionalPermitAllPaths) {

  public List<String> additionalPermitAllPaths() {
    if (additionalPermitAllPaths != null) {
      return additionalPermitAllPaths;
    }
    return List.of();
  }
}
