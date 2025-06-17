package de.jensknipper.re_director.web.controller.dto;

import de.jensknipper.re_director.db.entity.Redirect;
import org.springframework.stereotype.Component;

@Component
public final class DtoMapper {

  public RedirectResponse toRedirectResponse(Redirect redirect) {
    return new RedirectResponse(
        redirect.id(), redirect.source(), redirect.target(), redirect.status());
  }
}
