package de.jensknipper.re_director.db.entity;

import java.time.LocalDateTime;

public record Redirect(
    int id,
    String source,
    String target,
    Status status,
    LocalDateTime createdAt,
    RedirectHttpStatusCode httpStatusCode) {}
