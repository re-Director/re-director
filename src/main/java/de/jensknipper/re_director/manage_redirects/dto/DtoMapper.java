package de.jensknipper.re_director.manage_redirects.dto;

import de.jensknipper.re_director.manage_redirects.Redirect;
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
