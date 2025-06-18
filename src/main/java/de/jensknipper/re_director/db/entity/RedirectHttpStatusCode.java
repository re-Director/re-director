package de.jensknipper.re_director.db.entity;

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
}
