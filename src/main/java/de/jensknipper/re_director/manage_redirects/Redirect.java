package de.jensknipper.re_director.manage_redirects;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
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
