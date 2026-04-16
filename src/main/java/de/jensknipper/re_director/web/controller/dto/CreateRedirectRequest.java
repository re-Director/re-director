package de.jensknipper.re_director.web.controller.dto;

import de.jensknipper.re_director.web.validation.IntIn;
import de.jensknipper.re_director.web.validation.IsUrl;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public final class CreateRedirectRequest {
  @NotNull @NotEmpty private String source;
  @NotNull @NotEmpty @IsUrl private String target;

  private boolean pathForwarding = false;

  @IntIn({301, 302, 307, 308})
  private int httpStatusCode = 301;

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public boolean isPathForwarding() {
    return pathForwarding;
  }

  public void setPathForwarding(boolean pathForwarding) {
    this.pathForwarding = pathForwarding;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }

  public void setHttpStatusCode(int httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    CreateRedirectRequest that = (CreateRedirectRequest) o;
    return httpStatusCode == that.httpStatusCode
        && Objects.equals(source, that.source)
        && Objects.equals(target, that.target)
        && Objects.equals(pathForwarding, that.pathForwarding);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, target, pathForwarding, httpStatusCode);
  }

  @Override
  public String toString() {
    return "CreateRedirectRequest{"
        + "source='"
        + source
        + '\''
        + ", target='"
        + target
        + '\''
        + ", pathForwarding='"
        + pathForwarding
        + '\''
        + ", httpStatusCode="
        + httpStatusCode
        + '}';
  }
}
