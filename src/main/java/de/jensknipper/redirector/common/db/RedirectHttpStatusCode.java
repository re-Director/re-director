package de.jensknipper.redirector.common.db;

import org.jspecify.annotations.Nullable;

public enum RedirectHttpStatusCode {
  HTTP_301_MOVED_PERMANENTLY(301),
  HTTP_302_FOUND(302),
  HTTP_307_TEMPORARY_REDIRECT(307),
  HTTP_308_PERMANENT_REDIRECT(308);

  private final int code;

  RedirectHttpStatusCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  @Nullable
  public static RedirectHttpStatusCode fromCode(String code) {
    for (RedirectHttpStatusCode status : values()) {
      if (String.valueOf(status.code).equals(code.trim())) {
        return status;
      }
    }
    return null;
  }
}
