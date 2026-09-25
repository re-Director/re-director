package de.jensknipper.redirector.redirects.manage.dto;

import de.jensknipper.redirector.common.db.RedirectHttpStatusCode;
import de.jensknipper.redirector.common.validation.IsUrl;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateRedirectRequest(
    @NotEmpty String source,
    @NotEmpty @IsUrl String target,
    boolean pathForwarding,
    boolean queryForwarding,
    @NotNull RedirectHttpStatusCode httpStatusCode) {

  public static CreateRedirectRequest empty() {
    return new CreateRedirectRequest(
        "", "", false, false, RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY);
  }
}
