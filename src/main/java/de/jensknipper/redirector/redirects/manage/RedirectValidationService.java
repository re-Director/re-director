package de.jensknipper.redirector.redirects.manage;

import de.jensknipper.redirector.redirects.filter.BaseUrl;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class RedirectValidationService {

  private final ManageRedirectsService manageRedirectsService;
  private final BaseUrl baseUrl;

  public RedirectValidationService(ManageRedirectsService manageRedirectsService, BaseUrl baseUrl) {
    this.manageRedirectsService = manageRedirectsService;
    this.baseUrl = baseUrl;
  }

  public void uniqueSource(BindingResult bindingResult, String source, Integer excludeId) {
    boolean exists = manageRedirectsService.redirectAlreadyExists(source, excludeId);
    if (exists) {
      bindingResult.rejectValue("source", "unique.source", "already exists");
    }
  }

  public void uniqueSource(BindingResult bindingResult, String source) {
    boolean exists = manageRedirectsService.redirectAlreadyExists(source);
    if (exists) {
      bindingResult.rejectValue("source", "unique.source", "already exists");
    }
  }

  public void targetNotBaseUrl(BindingResult bindingResult, String target) {
    Optional<String> targetHost = Optional.of(target).map(this::parseUri).map(URI::getHost);
    if (targetHost.isEmpty()) {
      return;
    }
    boolean targetIsBaseUrl =
        Optional.ofNullable(baseUrl.getUrl())
            .map(this::parseUri)
            .map(URI::getHost)
            .filter(it -> it.equalsIgnoreCase(targetHost.get()))
            .isPresent();
    if (targetIsBaseUrl) {
      bindingResult.rejectValue("target", "base-url.target", "should not be the same as base URL");
    }
  }

  @Nullable
  private URI parseUri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException _) {
      return null;
    }
  }
}
