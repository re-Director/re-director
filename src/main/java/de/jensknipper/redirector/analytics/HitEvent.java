package de.jensknipper.redirector.analytics;

import java.time.Instant;

public record HitEvent(int redirectId, Instant time) {}
