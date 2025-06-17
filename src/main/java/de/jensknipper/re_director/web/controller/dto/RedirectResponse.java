package de.jensknipper.re_director.web.controller.dto;

import de.jensknipper.re_director.db.entity.Status;

public record RedirectResponse(long id, String source, String target, Status status) {}
