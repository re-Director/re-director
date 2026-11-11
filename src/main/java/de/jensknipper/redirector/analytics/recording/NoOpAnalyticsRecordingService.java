package de.jensknipper.redirector.analytics.recording;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBooleanProperty(
    name = "re-director.analytics.enabled",
    havingValue = false,
    matchIfMissing = true)
class NoOpAnalyticsRecordingService implements AnalyticsRecorder {

  @Override
  public void recordHit(int redirectId) {
    // nothing to do
  }
}
