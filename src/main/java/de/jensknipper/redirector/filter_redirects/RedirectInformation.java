package de.jensknipper.redirector.filter_redirects;

import de.jensknipper.redirector.common.db.RedirectHttpStatusCode;

public record RedirectInformation(
    int id,
    String target,
    RedirectHttpStatusCode httpStatusCode,
    boolean pathForwarding,
    boolean queryForwarding) {}
