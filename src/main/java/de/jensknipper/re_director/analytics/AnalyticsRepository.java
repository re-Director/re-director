package de.jensknipper.re_director.analytics;

import static de.jensknipper.re_director.database.tables.RedirectHit.REDIRECT_HIT;
import static de.jensknipper.re_director.database.tables.RedirectHitDaily.REDIRECT_HIT_DAILY;
import static de.jensknipper.re_director.database.tables.RedirectHitHourly.REDIRECT_HIT_HOURLY;

import de.jensknipper.re_director.database.tables.records.RedirectHitRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class AnalyticsRepository {

  private final DSLContext dsl;

  public AnalyticsRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public void insertHits(List<HitEvent> hits) {
    List<RedirectHitRecord> records =
        hits.stream()
            .map(
                hit -> {
                  RedirectHitRecord record = dsl.newRecord(REDIRECT_HIT);
                  record.setRedirectId(hit.redirectId());
                  record.setHitTime(LocalDateTime.ofInstant(hit.time(), ZoneOffset.UTC));
                  return record;
                })
            .toList();
    dsl.batchInsert(records).execute();
  }

  /** Aggregates raw hits older than {@code cutoff} into {@code redirect_hit_hourly}. */
  public void aggregateHourly(LocalDateTime cutoff) {
    Field<LocalDateTime> hour =
        DSL.field("strftime('%Y-%m-%d %H:00:00', {0})", LocalDateTime.class, REDIRECT_HIT.HIT_TIME);

    dsl.insertInto(
            REDIRECT_HIT_HOURLY,
            REDIRECT_HIT_HOURLY.REDIRECT_ID,
            REDIRECT_HIT_HOURLY.HOUR,
            REDIRECT_HIT_HOURLY.HITS)
        .select(
            dsl.select(REDIRECT_HIT.REDIRECT_ID, hour, DSL.count())
                .from(REDIRECT_HIT)
                .where(REDIRECT_HIT.HIT_TIME.lt(cutoff))
                .groupBy(REDIRECT_HIT.REDIRECT_ID, hour))
        .onConflict(REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR)
        .doUpdate()
        .set(
            REDIRECT_HIT_HOURLY.HITS,
            REDIRECT_HIT_HOURLY.HITS.plus(DSL.excluded(REDIRECT_HIT_HOURLY.HITS)))
        .execute();
  }

  /**
   * Removes raw hits older than {@code cutoff} that have been folded into {@code
   * redirect_hit_hourly}.
   */
  public void deleteAggregatedHits(LocalDateTime cutoff) {
    dsl.deleteFrom(REDIRECT_HIT).where(REDIRECT_HIT.HIT_TIME.lt(cutoff)).execute();
  }

  /** Aggregates hourly hits older than {@code cutoff} into {@code redirect_hit_daily}. */
  public void aggregateDaily(LocalDateTime cutoff) {
    Field<LocalDate> day = DSL.field("DATE({0})", LocalDate.class, REDIRECT_HIT_HOURLY.HOUR);

    dsl.insertInto(
            REDIRECT_HIT_DAILY,
            REDIRECT_HIT_DAILY.REDIRECT_ID,
            REDIRECT_HIT_DAILY.DAY,
            REDIRECT_HIT_DAILY.HITS)
        .select(
            dsl.select(
                    REDIRECT_HIT_HOURLY.REDIRECT_ID,
                    day,
                    DSL.sum(REDIRECT_HIT_HOURLY.HITS).cast(Integer.class))
                .from(REDIRECT_HIT_HOURLY)
                .where(REDIRECT_HIT_HOURLY.HOUR.lt(cutoff))
                .groupBy(REDIRECT_HIT_HOURLY.REDIRECT_ID, day))
        .onConflict(REDIRECT_HIT_DAILY.REDIRECT_ID, REDIRECT_HIT_DAILY.DAY)
        .doUpdate()
        .set(
            REDIRECT_HIT_DAILY.HITS,
            REDIRECT_HIT_DAILY.HITS.plus(DSL.excluded(REDIRECT_HIT_DAILY.HITS)))
        .execute();
  }

  /**
   * Removes hourly hits older than {@code cutoff} that have been folded into {@code
   * redirect_hit_daily}.
   */
  public void deleteAggregatedHourlyHits(LocalDateTime cutoff) {
    dsl.deleteFrom(REDIRECT_HIT_HOURLY).where(REDIRECT_HIT_HOURLY.HOUR.lt(cutoff)).execute();
  }

  /** Removes daily hits older than {@code cutoff}. */
  public void deleteOldDailyHits(LocalDate cutoff) {
    dsl.deleteFrom(REDIRECT_HIT_DAILY).where(REDIRECT_HIT_DAILY.DAY.lt(cutoff)).execute();
  }
}
