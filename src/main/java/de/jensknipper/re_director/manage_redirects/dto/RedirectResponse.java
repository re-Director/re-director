package de.jensknipper.re_director.manage_redirects.dto;

import de.jensknipper.re_director.common.db.Status;

public record RedirectResponse(
    int id,
    String source,
    String target,
    boolean pathForwarding,
    boolean queryForwarding,
    int httpStatusCode,
    Status status) {}
