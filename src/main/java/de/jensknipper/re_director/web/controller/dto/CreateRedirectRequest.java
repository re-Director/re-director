package de.jensknipper.re_director.web.controller.dto;

import de.jensknipper.re_director.web.validation.IntIn;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public final class CreateRedirectRequest {
  @NotNull @NotEmpty private String source;
  @NotNull @NotEmpty private String target;

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
        && Objects.equals(target, that.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, target, httpStatusCode);
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
        + ", httpStatusCode="
        + httpStatusCode
        + '}';
  }
}
