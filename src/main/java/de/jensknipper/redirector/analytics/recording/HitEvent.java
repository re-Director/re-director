package de.jensknipper.redirector.analytics.recording;

import java.time.Instant;

public record HitEvent(int redirectId, Instant time) {}
