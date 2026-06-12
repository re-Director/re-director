package de.jensknipper.re_director.analytics;

import static de.jensknipper.re_director.database.tables.RedirectHit.REDIRECT_HIT;
import static de.jensknipper.re_director.database.tables.RedirectHitDaily.REDIRECT_HIT_DAILY;
import static de.jensknipper.re_director.database.tables.RedirectHitHourly.REDIRECT_HIT_HOURLY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AnalyticsRepositoryTest {

  @Autowired private DSLContext dsl;
  @Autowired private AnalyticsRepository analyticsRepository;

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
      List<HitEvent> hits = List.of(new HitEvent(1, Instant.now()), new HitEvent(2, Instant.now()));

      // when
      analyticsRepository.insertHits(hits);

      // then
      assertThat(dsl.fetchCount(REDIRECT_HIT)).isEqualTo(2);
      assertThat(dsl.fetchCount(REDIRECT_HIT, REDIRECT_HIT.REDIRECT_ID.eq(1))).isEqualTo(1);
      assertThat(dsl.fetchCount(REDIRECT_HIT, REDIRECT_HIT.REDIRECT_ID.eq(2))).isEqualTo(1);
    }
  }

  @Nested
  class AggregateHourly {

    @Test
    void aggregateHourlyShouldGroupOldHitsByRedirectAndHour() {
      // given
      Instant twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
      analyticsRepository.insertHits(
          List.of(
              new HitEvent(1, twoHoursAgo),
              new HitEvent(1, twoHoursAgo.plusSeconds(60)),
              new HitEvent(2, twoHoursAgo)));

      // when
      analyticsRepository.aggregateHourly(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));

      // then
      LocalDateTime hour =
          LocalDateTime.ofInstant(twoHoursAgo, ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
      var rows = dsl.selectFrom(REDIRECT_HIT_HOURLY).fetch();
      assertThat(rows).hasSize(2);
      assertThat(rows)
          .anySatisfy(
              row -> {
                assertThat(row.getRedirectId()).isEqualTo(1);
                assertThat(row.getHour()).isEqualTo(hour);
                assertThat(row.getHits()).isEqualTo(2);
              });
      assertThat(rows)
          .anySatisfy(
              row -> {
                assertThat(row.getRedirectId()).isEqualTo(2);
                assertThat(row.getHour()).isEqualTo(hour);
                assertThat(row.getHits()).isEqualTo(1);
              });
    }

    @Test
    void aggregateHourlyShouldNotIncludeCurrentHourHits() {
      // given
      analyticsRepository.insertHits(List.of(new HitEvent(1, Instant.now())));

      // when
      analyticsRepository.aggregateHourly(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));

      // then
      assertThat(dsl.fetchCount(REDIRECT_HIT_HOURLY)).isZero();
    }

    @Test
    void aggregateHourlyShouldAccumulateExistingHourlyHits() {
      // given
      Instant twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
      LocalDateTime hour =
          LocalDateTime.ofInstant(twoHoursAgo, ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
      // insert via the same strftime expression aggregateHourly() uses, so the (redirect_id,
      // hour) primary key matches and ON CONFLICT DO UPDATE applies
      Field<LocalDateTime> hourField =
          DSL.field("strftime('%Y-%m-%d %H:00:00', {0})", LocalDateTime.class, DSL.val(hour));
      dsl.insertInto(
              REDIRECT_HIT_HOURLY,
              REDIRECT_HIT_HOURLY.REDIRECT_ID,
              REDIRECT_HIT_HOURLY.HOUR,
              REDIRECT_HIT_HOURLY.HITS)
          .select(dsl.select(DSL.val(1), hourField, DSL.val(5)))
          .execute();
      analyticsRepository.insertHits(List.of(new HitEvent(1, twoHoursAgo)));

      // when
      analyticsRepository.aggregateHourly(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));

      // then
      var rows = dsl.selectFrom(REDIRECT_HIT_HOURLY).fetch();
      assertThat(rows).hasSize(1);
      assertThat(rows.getFirst().getHits()).isEqualTo(6);
    }
  }

  @Test
  void deleteAggregatedHitsShouldRemoveOldHitsButKeepCurrentHour() {
    // given
    Instant twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
    analyticsRepository.insertHits(
        List.of(new HitEvent(1, twoHoursAgo), new HitEvent(1, Instant.now())));

    // when
    analyticsRepository.deleteAggregatedHits(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));

    // then
    assertThat(dsl.fetchCount(REDIRECT_HIT)).isEqualTo(1);
  }

  @Nested
  class AggregateDaily {

    @Test
    void aggregateDailyShouldSumHourlyHitsFromPastDays() {
      // given
      LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
      dsl.insertInto(REDIRECT_HIT_HOURLY)
          .columns(
              REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR, REDIRECT_HIT_HOURLY.HITS)
          .values(1, yesterday.atTime(10, 0), 3)
          .values(1, yesterday.atTime(11, 0), 4)
          .execute();

      // when
      analyticsRepository.aggregateDaily(LocalDate.now(ZoneOffset.UTC).atStartOfDay());

      // then
      assertThat(dsl.fetchCount(REDIRECT_HIT_DAILY)).isEqualTo(1);
      assertThat(
              dsl.select(REDIRECT_HIT_DAILY.HITS)
                  .from(REDIRECT_HIT_DAILY)
                  .where(
                      REDIRECT_HIT_DAILY
                          .REDIRECT_ID
                          .eq(1)
                          .and(REDIRECT_HIT_DAILY.DAY.eq(yesterday)))
                  .fetchOne(REDIRECT_HIT_DAILY.HITS))
          .isEqualTo(7);
    }

    @Test
    void aggregateDailyShouldNotIncludeToday() {
      // given
      LocalDate today = LocalDate.now(ZoneOffset.UTC);
      dsl.insertInto(REDIRECT_HIT_HOURLY)
          .columns(
              REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR, REDIRECT_HIT_HOURLY.HITS)
          .values(1, today.atTime(0, 0), 3)
          .execute();

      // when
      analyticsRepository.aggregateDaily(LocalDate.now(ZoneOffset.UTC).atStartOfDay());

      // then
      assertThat(dsl.fetchCount(REDIRECT_HIT_DAILY)).isZero();
    }

    @Test
    void aggregateDailyShouldAccumulateExistingDailyHits() {
      // given
      LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
      dsl.insertInto(REDIRECT_HIT_DAILY)
          .columns(REDIRECT_HIT_DAILY.REDIRECT_ID, REDIRECT_HIT_DAILY.DAY, REDIRECT_HIT_DAILY.HITS)
          .values(1, yesterday, 10)
          .execute();
      dsl.insertInto(REDIRECT_HIT_HOURLY)
          .columns(
              REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR, REDIRECT_HIT_HOURLY.HITS)
          .values(1, yesterday.atTime(10, 0), 5)
          .execute();

      // when
      analyticsRepository.aggregateDaily(LocalDate.now(ZoneOffset.UTC).atStartOfDay());

      // then
      assertThat(
              dsl.select(REDIRECT_HIT_DAILY.HITS)
                  .from(REDIRECT_HIT_DAILY)
                  .where(
                      REDIRECT_HIT_DAILY
                          .REDIRECT_ID
                          .eq(1)
                          .and(REDIRECT_HIT_DAILY.DAY.eq(yesterday)))
                  .fetchOne(REDIRECT_HIT_DAILY.HITS))
          .isEqualTo(15);
    }
  }

  @Test
  void deleteAggregatedHourlyHitsShouldRemoveHitsBeforeTodayButKeepToday() {
    // given
    LocalDateTime yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1).atTime(10, 0);
    LocalDateTime today = LocalDate.now(ZoneOffset.UTC).atTime(10, 0);
    dsl.insertInto(REDIRECT_HIT_HOURLY)
        .columns(
            REDIRECT_HIT_HOURLY.REDIRECT_ID, REDIRECT_HIT_HOURLY.HOUR, REDIRECT_HIT_HOURLY.HITS)
        .values(1, yesterday, 1)
        .values(1, today, 2)
        .execute();

    // when
    analyticsRepository.deleteAggregatedHourlyHits(LocalDate.now(ZoneOffset.UTC).atStartOfDay());

    // then
    assertThat(dsl.fetchCount(REDIRECT_HIT_HOURLY)).isEqualTo(1);
    assertThat(
            dsl.select(REDIRECT_HIT_HOURLY.HOUR)
                .from(REDIRECT_HIT_HOURLY)
                .fetchOne(REDIRECT_HIT_HOURLY.HOUR))
        .isEqualTo(today);
  }

  @Test
  void deleteOldDailyHitsShouldRemoveHitsOlderThanRetention() {
    // given
    LocalDate longAgo = LocalDate.now(ZoneOffset.UTC).minusDays(100);
    LocalDate recently = LocalDate.now(ZoneOffset.UTC).minusDays(10);
    dsl.insertInto(REDIRECT_HIT_DAILY)
        .columns(REDIRECT_HIT_DAILY.REDIRECT_ID, REDIRECT_HIT_DAILY.DAY, REDIRECT_HIT_DAILY.HITS)
        .values(1, longAgo, 1)
        .values(1, recently, 2)
        .execute();

    // when
    analyticsRepository.deleteOldDailyHits(LocalDate.now(ZoneOffset.UTC).minusDays(90));

    // then
    assertThat(dsl.fetchCount(REDIRECT_HIT_DAILY)).isEqualTo(1);
    assertThat(
            dsl.select(REDIRECT_HIT_DAILY.DAY)
                .from(REDIRECT_HIT_DAILY)
                .fetchOne(REDIRECT_HIT_DAILY.DAY))
        .isEqualTo(recently);
  }
}
