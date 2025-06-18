package de.jensknipper.re_director.db.entity;

import java.util.Arrays;
import java.util.Optional;

public enum RedirectHttpStatusCode {
  MOVED_PERMANENTLY(301),
  FOUND(302),
  TEMPORARY_REDIRECT(307),
  PERMANENT_REDIRECT(308);

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
