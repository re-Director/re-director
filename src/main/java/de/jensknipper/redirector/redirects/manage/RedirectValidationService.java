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

  public void uniqueSource(
      BindingResult bindingResult, @Nullable String source, Integer excludeId) {
    if (source == null) {
      return;
    }
    boolean exists = manageRedirectsService.redirectAlreadyExists(source, excludeId);
    if (exists) {
      bindingResult.rejectValue("source", "unique.source", "already exists");
    }
  }

  public void uniqueSource(BindingResult bindingResult, @Nullable String source) {
    if (source == null) {
      return;
    }
    boolean exists = manageRedirectsService.redirectAlreadyExists(source);
    if (exists) {
      bindingResult.rejectValue("source", "unique.source", "already exists");
    }
  }

  public void sourceNotBaseUrl(BindingResult bindingResult, @Nullable String source) {
    URI baseUri = Optional.ofNullable(baseUrl.getFullUrl()).map(this::parseUri).orElse(null);

    if (source == null || baseUri == null) {
      return;
    }

    if (source.equalsIgnoreCase(baseUri.getHost())) {
      bindingResult.rejectValue("source", "base-url.source", "should not point to the base URL");
    }
  }

  public void targetNotBaseUrl(BindingResult bindingResult, @Nullable String target) {
    URI targetUri = Optional.ofNullable(target).map(this::parseUri).orElse(null);
    URI baseUri = Optional.ofNullable(baseUrl.getFullUrl()).map(this::parseUri).orElse(null);

    if (targetUri == null || baseUri == null) {
      return;
    }

    if (targetUri.getHost() != null
        && targetUri.getHost().equalsIgnoreCase(baseUri.getHost())
        && targetUri.getPort() == baseUri.getPort()) {
      bindingResult.rejectValue("target", "base-url.target", "should not point to the base URL");
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
