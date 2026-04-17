package de.jensknipper.re_director.web.controller.dto;

import de.jensknipper.re_director.db.entity.Status;

public record RedirectResponse(
    int id,
    String source,
    String target,
    boolean pathForwarding,
    boolean queryForwarding,
    int httpStatusCode,
    Status status) {}
