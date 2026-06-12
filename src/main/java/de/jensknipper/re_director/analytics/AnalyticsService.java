package de.jensknipper.re_director.analytics;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

  private final AnalyticsRepository analyticsRepository;
  private final int dataRetentionDays;

  private final ConcurrentLinkedQueue<HitEvent> queue = new ConcurrentLinkedQueue<>();

  public AnalyticsService(
      AnalyticsRepository analyticsRepository,
      @Value("${re-director.analytics.data-retention-days}") int dataRetentionDays) {
    this.analyticsRepository = analyticsRepository;
    this.dataRetentionDays = dataRetentionDays;
  }

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
      analyticsRepository.insertHits(batch);
      log.debug("Flushed {} hit events", batch.size());
    } catch (Exception e) {
      queue.addAll(batch);
      log.warn("Failed to flush {} hit events, will retry", batch.size(), e);
    }
  }

  @Scheduled(fixedRateString = "${re-director.analytics.schedule.flush-hourly-aggregation-rate}")
  public void aggregateHourly() {
    LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
    analyticsRepository.aggregateHourly(cutoff);
    analyticsRepository.deleteAggregatedHits(cutoff);
    log.debug("Ran hourly aggregation at {}", Instant.now());
  }

  @Scheduled(fixedRateString = "${re-director.analytics.schedule.flush-daily-aggregation-rate}")
  public void aggregateDaily() {
    LocalDateTime cutoff = LocalDate.now(ZoneOffset.UTC).atStartOfDay();
    analyticsRepository.aggregateDaily(cutoff);
    analyticsRepository.deleteAggregatedHourlyHits(cutoff);
    log.debug("Ran daily aggregation at {}", Instant.now());
  }

  @Scheduled(cron = "${re-director.analytics.schedule.cleanup-old-data-cron}")
  public void cleanupOldData() {
    LocalDate cutoff = LocalDate.now(ZoneOffset.UTC).minusDays(dataRetentionDays);
    analyticsRepository.deleteOldDailyHits(cutoff);
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
