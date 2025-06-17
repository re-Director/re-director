package de.jensknipper.re_director.web.controller.dto;

import java.util.Objects;

public final class CreateRedirectRequest {
  private String source;
  private String target;

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

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (CreateRedirectRequest) obj;
    return Objects.equals(this.source, that.source) && Objects.equals(this.target, that.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, target);
  }

  @Override
  public String toString() {
    return "CreateRedirectRequest[" + "source=" + source + ", " + "target=" + target + ']';
  }
}
