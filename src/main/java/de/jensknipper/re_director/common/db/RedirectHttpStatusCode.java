package de.jensknipper.re_director.common.db;

import java.util.Arrays;
import java.util.Optional;

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

  public static Optional<RedirectHttpStatusCode> findByCode(int code) {
    return Arrays.stream(values()).filter(it -> it.code == code).findAny();
  }
}
