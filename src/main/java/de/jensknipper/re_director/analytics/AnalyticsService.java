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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
  private static final int DAILY_RETENTION_DAYS = 90;

  private final AnalyticsRepository analyticsRepository;
  private final ConcurrentLinkedQueue<HitEvent> queue = new ConcurrentLinkedQueue<>();

  public AnalyticsService(AnalyticsRepository analyticsRepository) {
    this.analyticsRepository = analyticsRepository;
  }

  public void recordHit(int redirectId) {
    queue.offer(new HitEvent(redirectId, Instant.now()));
  }

  @Scheduled(fixedDelay = 3000) // 3 seconds
  public void flushHits() {
    log.debug("Queue size before flush: {}", queue.size());

    List<HitEvent> batch = drainQueue();
    if (batch.isEmpty()) {
      return;
    }

    analyticsRepository.insertHits(batch);
    log.debug("Flushed {} hit events", batch.size());
  }

  @Scheduled(fixedDelay = 15 * 60 * 1000) // 15 minutes
  public void aggregateHourly() {
    LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
    analyticsRepository.aggregateHourly(cutoff);
    analyticsRepository.deleteAggregatedHits(cutoff);
    log.debug("Ran hourly aggregation at {}", Instant.now());
  }

  @Scheduled(fixedDelay = 60 * 60 * 1000) // 60 minutes
  public void aggregateDaily() {
    LocalDateTime cutoff = LocalDate.now(ZoneOffset.UTC).atStartOfDay();
    analyticsRepository.aggregateDaily(cutoff);
    analyticsRepository.deleteAggregatedHourlyHits(cutoff);
    log.debug("Ran daily aggregation at {}", Instant.now());
  }

  @Scheduled(cron = "0 0 3 * * *") // 3 AM
  public void cleanupOldData() {
    LocalDate cutoff = LocalDate.now(ZoneOffset.UTC).minusDays(DAILY_RETENTION_DAYS);
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
