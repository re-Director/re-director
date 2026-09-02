package de.jensknipper.redirector.redirects.manage.dto;

import de.jensknipper.redirector.common.db.Status;

public record RedirectResponse(
    int id,
    String source,
    String target,
    boolean pathForwarding,
    boolean queryForwarding,
    int httpStatusCode,
    Status status) {}
