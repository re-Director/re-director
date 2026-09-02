package de.jensknipper.redirector.redirects.manage.dto;

import de.jensknipper.redirector.redirects.manage.Redirect;
import org.springframework.stereotype.Component;

@Component
public final class DtoMapper {

  public RedirectResponse toRedirectResponse(Redirect redirect) {
    return new RedirectResponse(
        redirect.id(),
        redirect.source(),
        redirect.target(),
        redirect.pathForwarding(),
        redirect.queryForwarding(),
        redirect.httpStatusCode().getCode(),
        redirect.status());
  }
}
