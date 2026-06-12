package de.jensknipper.re_director.analytics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBooleanProperty(
    name = "re-director.analytics.enabled",
    havingValue = false,
    matchIfMissing = true)
class NoOpAnalyticsService implements AnalyticsRecorder {

  @Override
  public void recordHit(int redirectId) {
    // nothing to do
  }
}
