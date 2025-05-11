package de.jensknipper.re_director.db;

import java.time.LocalDateTime;

public record Redirect(long id, String source, String target, Status status, LocalDateTime createdAt) {
}
