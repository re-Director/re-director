package de.jensknipper.re_director.filter_redirects;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;

public record RedirectInformation(
    int id,
    String target,
    RedirectHttpStatusCode httpStatusCode,
    boolean pathForwarding,
    boolean queryForwarding) {}
