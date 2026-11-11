package de.jensknipper.redirector.analytics.recording;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBooleanProperty("re-director.analytics.enabled")
public class AnalyticsRecordingService implements AnalyticsRecorder {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsRecordingService.class);

  private final AnalyticsRecordingRepository analyticsRecordingRepository;
  private final int dataRetentionDays;

  private final ConcurrentLinkedQueue<HitEvent> queue = new ConcurrentLinkedQueue<>();

  public AnalyticsRecordingService(
      AnalyticsRecordingRepository analyticsRecordingRepository,
      @Value("${re-director.analytics.data-retention-days}") int dataRetentionDays) {
    this.analyticsRecordingRepository = analyticsRecordingRepository;
    this.dataRetentionDays = dataRetentionDays;
  }

  @Override
  public void recordHit(int redirectId) {
    queue.offer(new HitEvent(redirectId, Instant.now()));
  }

  @Scheduled(fixedRateString = "${re-director.analytics.schedule.flush-hits-rate}")
  public void flushHits() {
    log.debug("Queue size before flush: {}", queue.size());

    List<HitEvent> batch = drainQueue();
    if (batch.isEmpty()) {
      return;
    }

    try {
      analyticsRecordingRepository.insertHits(batch);
      log.debug("Flushed {} hit events", batch.size());
    } catch (Exception e) {
      queue.addAll(batch);
      log.warn("Failed to flush {} hit events, will retry", batch.size(), e);
    }
  }

  @Transactional
  @Scheduled(fixedRateString = "${re-director.analytics.schedule.flush-hourly-aggregation-rate}")
  public void aggregateHourly() {
    Instant cutoff = Instant.now().minus(1, ChronoUnit.HOURS);
    analyticsRecordingRepository.aggregateHourly(cutoff);
    analyticsRecordingRepository.deleteAggregatedHits(cutoff);
    log.debug("Ran hourly aggregation at {}", Instant.now());
  }

  @Transactional
  @Scheduled(fixedRateString = "${re-director.analytics.schedule.flush-daily-aggregation-rate}")
  public void aggregateDaily() {
    Instant cutoff = Instant.now().truncatedTo(ChronoUnit.DAYS);
    analyticsRecordingRepository.aggregateDaily(cutoff);
    analyticsRecordingRepository.deleteAggregatedHourlyHits(cutoff);
    log.debug("Ran daily aggregation at {}", Instant.now());
  }

  @Scheduled(cron = "${re-director.analytics.schedule.cleanup-old-data-cron}")
  public void cleanupOldData() {
    Instant cutoff =
        Instant.now().truncatedTo(ChronoUnit.DAYS).minus(dataRetentionDays, ChronoUnit.DAYS);
    analyticsRecordingRepository.deleteOldDailyHits(cutoff);
    log.debug("Ran retention cleanup at {}", Instant.now());
  }

  @PreDestroy
  public void flushOnShutdown() {
    flushHits();
  }

  private List<HitEvent> drainQueue() {
    List<HitEvent> batch = new ArrayList<>();
    HitEvent event;
    while ((event = queue.poll()) != null) {
      batch.add(event);
    }
    return batch;
  }
}
