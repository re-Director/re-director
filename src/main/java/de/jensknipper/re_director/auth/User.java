package de.jensknipper.re_director.auth;

import java.time.LocalDateTime;

public record User(
    int id, String username, String passwordHash, boolean enabled, LocalDateTime createdAt) {}
