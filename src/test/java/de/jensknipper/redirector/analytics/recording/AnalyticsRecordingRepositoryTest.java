package de.jensknipper.redirector.analytics.recording;

import static de.jensknipper.redirector.database.tables.RedirectHit.REDIRECT_HIT;
import static de.jensknipper.redirector.database.tables.RedirectHitDaily.REDIRECT_HIT_DAILY;
import static de.jensknipper.redirector.database.tables.RedirectHitHourly.REDIRECT_HIT_HOURLY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.*;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AnalyticsRecordingRepositoryTest {

  private static final LocalDate date = LocalDate.of(2026, 1, 15);
  private static final LocalDateTime time = date.atTime(12, 0, 0);
  private static final Instant timeInstant = time.toInstant(ZoneOffset.UTC);
  private static final Instant dateStartInstant = date.atStartOfDay().toInstant(ZoneOffset.UTC);

  @Autowired private DSLContext dsl;
  @Autowired private AnalyticsRecordingRepository analyticsRecordingRepository;

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    String uniqueDb = "jdbc:sqlite:file::memdb-" + UUID.randomUUID() + ":?mode=memory&cache=shared";
    registry.add("spring.datasource.url", () -> uniqueDb);
  }

  @BeforeEach
  void cleanup() {
    dsl.deleteFrom(REDIRECT_HIT).execute();
    dsl.deleteFrom(REDIRECT_HIT_HOURLY).execute();
    dsl.deleteFrom(REDIRECT_HIT_DAILY).execute();
  }

  @Nested
  class InsertHits {

    @Test
    void insertHitsShouldInsertRawHits() {
      // given
      List<HitEvent> hits = List.of(new HitEvent(1, timeInstant), new HitEvent(2, timeInstant));

      // when
      analyticsRecordingRepository.insertHits(hits);

      // then
      List<HitEvent> results = dsl.selectFrom(REDIRECT_HIT).fetchInto(HitEvent.class);
      assertThat(results).hasSize(2).containsExactlyInAnyOrderElementsOf(hits);
    }
  }

  @Nested
  class AggregateHourly {

    @Test
    void aggregateHourlyShouldGroupHitsByRedirectAndHour() {
      // given
      analyticsRecordingRepository.insertHits(
          List.of(
              new HitEvent(1, timeInstant),
              new HitEvent(1, timeInstant.plusSeconds(60)),
              new HitEvent(1, timeInstant.plus(Duration.ofHours(1))),
              new HitEvent(2, timeInstant)));

      // when
      analyticsRecordingRepository.aggregateHourly(timeInstant.plus(Duration.ofHours(2)));

      // then
      var rows = dsl.selectFrom(REDIRECT_HIT_HOURLY).fetch();
      assertThat(rows)
          .hasSize(3)
          .anySatisfy(row -> {
            assertThat(row.getRedirectId()).isEqualTo(1);
            assertThat(row.getHour()).isEqualTo(time);
            assertThat(row.getHits()).isEqualTo(2);
          })
          .anySatisfy(row -> {
            assertThat(row.getRedirectId()).isEqualTo(1);
            assertThat(row.getHour()).isEqualTo(time.plusHours(1));
            assertThat(row.getHits()).isEqualTo(1);
          })
          .anySatisfy(row -> {
            assertThat(row.getRedirectId()).isEqualTo(2);
            assertThat(row.getHour()).isEqualTo(time);
            assertThat(row.getHits()).isEqualTo(1);
          });
    }

    @Test
    void aggregateHourlyShouldNotIncludeCurrentHourHits() {
      // given
      analyticsRecordingRepository.insertHits(List.of(new HitEvent(1, timeInstant)));

      // when
      analyticsRecordingRepository.aggregateHourly(timeInstant.minus(Duration.ofHours(1)));

      // then
      assertThat(dsl.fetchCount(REDIRECT_HIT_HOURLY)).isZero();
    }

    @Test
    void aggregateHourlyShouldAccumulateExistingHourlyHits() {
      // given
      analyticsRecordingRepository.insertHits(List.of(new HitEvent(1, timeInstant)));
      analyticsRecordingRepository.insertHits(List.of(new HitEvent(1, timeInstant)));
      analyticsRecordingRepository.aggregateHourly(timeInstant.plus(Duration.ofHours(1)));
      analyticsRecordingRepository.deleteAggregatedHits(timeInstant.plus(Duration.ofHours(1)));
      analyticsRecordingRepository.insertHits(List.of(new HitEvent(1, timeInstant)));

      // when
      analyticsRecordingRepository.aggregateHourly(timeInstant.plus(Duration.ofHours(1)));

      // then
      var rows = dsl.selectFrom(REDIRECT_HIT_HOURLY).fetch();
      assertThat(rows).hasSize(1);
      assertThat(rows.getFirst().getHits()).isEqualTo(3);
    }
  }

  @Test
  void deleteAggregatedHitsShouldRemoveOldHitsButKeepFutureHour() {
    // given
    analyticsRecordingRepository.insertHits(
        List.of(
            new HitEvent(1, timeInstant),
            new HitEvent(1, timeInstant.plus(Duration.ofMinutes(61)))));

    // when
    analyticsRecordingRepository.deleteAggregatedHits(timeInstant.plus(Duration.ofHours(1)));

    // then
    assertThat(dsl.fetchCount(REDIRECT_HIT)).isEqualTo(1);
  }

  @Nested
  class AggregateDaily {

    @Test
    void aggregateDailyShouldSumHourlyHitsFromPastDays() {
      // given
      LocalDate before = date.minusDays(1);
      dsl.insertInto(REDIRECT_HIT_HOURLY)
          .columns(
              REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR, REDIRECT_HIT_HOURLY.HITS)
          .values(1, before.atTime(10, 0), 3)
          .values(1, before.atTime(11, 0), 4)
          .execute();

      // when
      analyticsRecordingRepository.aggregateDaily(dateStartInstant);

      // then
      var rows = dsl.selectFrom(REDIRECT_HIT_DAILY).fetch();
      assertThat(rows)
          .hasSize(1)
          .anySatisfy(row -> {
            assertThat(row.getRedirectId()).isEqualTo(1);
            assertThat(row.getHits()).isEqualTo(7);
          });
    }

    @Test
    void aggregateDailyShouldNotIncludeToday() {
      // given
      dsl.insertInto(REDIRECT_HIT_HOURLY)
          .columns(
              REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR, REDIRECT_HIT_HOURLY.HITS)
          .values(1, date.atTime(0, 0), 3)
          .execute();

      // when
      analyticsRecordingRepository.aggregateDaily(dateStartInstant);

      // then
      assertThat(dsl.fetchCount(REDIRECT_HIT_DAILY)).isZero();
    }

    @Test
    void aggregateDailyShouldAccumulateExistingDailyHits() {
      // given
      LocalDate before = date.minusDays(1);
      dsl.insertInto(REDIRECT_HIT_DAILY)
          .columns(REDIRECT_HIT_DAILY.REDIRECT_ID, REDIRECT_HIT_DAILY.DAY, REDIRECT_HIT_DAILY.HITS)
          .values(1, before, 10)
          .execute();
      dsl.insertInto(REDIRECT_HIT_HOURLY)
          .columns(
              REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR, REDIRECT_HIT_HOURLY.HITS)
          .values(1, before.atTime(10, 0), 5)
          .execute();

      // when
      analyticsRecordingRepository.aggregateDaily(dateStartInstant);

      // then
      var rows = dsl.selectFrom(REDIRECT_HIT_DAILY).fetch();
      assertThat(rows)
          .hasSize(1)
          .anySatisfy(row -> {
            assertThat(row.getRedirectId()).isEqualTo(1);
            assertThat(row.getHits()).isEqualTo(15);
          });
    }
  }

  @Test
  void deleteAggregatedHourlyHitsShouldRemoveHitsBeforeTodayButKeepToday() {
    // given
    dsl.insertInto(REDIRECT_HIT_HOURLY)
        .columns(
            REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR, REDIRECT_HIT_HOURLY.HITS)
        .values(1, time.minusDays(1), 1)
        .values(1, time, 2)
        .execute();

    // when
    analyticsRecordingRepository.deleteAggregatedHourlyHits(timeInstant);

    // then
    var rows = dsl.selectFrom(REDIRECT_HIT_HOURLY).fetch();
    assertThat(rows)
        .hasSize(1)
        .anySatisfy(row -> {
          assertThat(row.getRedirectId()).isEqualTo(1);
          assertThat(row.getHour()).isEqualTo(time);
        });
  }

  @Test
  void deleteOldDailyHitsShouldRemoveHitsOlderThanRetention() {
    // given
    LocalDate longAgo = date.minusDays(100);
    LocalDate recently = date.minusDays(10);
    dsl.insertInto(REDIRECT_HIT_DAILY)
        .columns(REDIRECT_HIT_DAILY.REDIRECT_ID, REDIRECT_HIT_DAILY.DAY, REDIRECT_HIT_DAILY.HITS)
        .values(1, longAgo, 1)
        .values(1, recently, 2)
        .execute();

    // when
    analyticsRecordingRepository.deleteOldDailyHits(
        date.minusDays(90).atStartOfDay().toInstant(ZoneOffset.UTC));

    // then
    var rows = dsl.selectFrom(REDIRECT_HIT_DAILY).fetch();
    assertThat(rows)
        .hasSize(1)
        .anySatisfy(row -> {
          assertThat(row.getRedirectId()).isEqualTo(1);
          assertThat(row.getDay()).isEqualTo(recently);
        });
  }
}
