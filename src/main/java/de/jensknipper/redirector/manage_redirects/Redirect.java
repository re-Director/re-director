package de.jensknipper.redirector.manage_redirects;

import de.jensknipper.redirector.common.db.RedirectHttpStatusCode;
import de.jensknipper.redirector.common.db.Status;
import java.time.LocalDateTime;

public record Redirect(
    int id,
    String source,
    String target,
    Status status,
    LocalDateTime createdAt,
    RedirectHttpStatusCode httpStatusCode,
    boolean pathForwarding,
    boolean queryForwarding) {}
