package de.jensknipper.redirector.manage_redirects.dto;

import de.jensknipper.redirector.common.db.Status;

public record RedirectResponse(
    int id,
    String source,
    String target,
    boolean pathForwarding,
    boolean queryForwarding,
    int httpStatusCode,
    Status status) {}
