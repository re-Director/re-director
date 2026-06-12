package de.jensknipper.re_director.analytics;

import java.time.Instant;

public record HitEvent(int redirectId, Instant time) {}
