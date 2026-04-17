package de.jensknipper.re_director.db.entity;

public record RedirectInformation(
    String target,
    RedirectHttpStatusCode httpStatusCode,
    boolean pathForwarding,
    boolean queryForwarding) {}
