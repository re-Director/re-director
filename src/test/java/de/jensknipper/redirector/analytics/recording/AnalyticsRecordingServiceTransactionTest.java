package de.jensknipper.redirector.analytics.recording;

import static de.jensknipper.redirector.database.tables.RedirectHit.REDIRECT_HIT;
import static de.jensknipper.redirector.database.tables.RedirectHitDaily.REDIRECT_HIT_DAILY;
import static de.jensknipper.redirector.database.tables.RedirectHitHourly.REDIRECT_HIT_HOURLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "re-director.analytics.enabled=true")
class AnalyticsRecordingServiceTransactionTest {

  private static final LocalDateTime time = LocalDateTime.of(2026, 1, 15, 12, 0);

  @Autowired private DSLContext dsl;
  @Autowired private AnalyticsRecordingService analyticsRecordingService;
  @MockitoSpyBean private AnalyticsRecordingRepository analyticsRecordingRepository;

  @MockitoBean(name = "taskScheduler") // no scheduling
  private TaskScheduler taskScheduler;

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

  @Test
  void aggregateHourlyShouldRollBackWhenDeletingRawHitsFails() {
    // given
    analyticsRecordingRepository.insertHits(
        List.of(new HitEvent(1, time.toInstant(ZoneOffset.UTC))));
    doThrow(new RuntimeException("boom"))
        .when(analyticsRecordingRepository)
        .deleteAggregatedHits(any());

    // when
    assertThatThrownBy(() -> analyticsRecordingService.aggregateHourly()).hasMessage("boom");

    // then
    assertThat(dsl.fetchCount(REDIRECT_HIT_HOURLY)).isZero();
    assertThat(dsl.fetchCount(REDIRECT_HIT)).isEqualTo(1);
  }

  @Test
  void aggregateDailyShouldRollBackWhenDeletingHourlyHitsFails() {
    // given
    dsl.insertInto(REDIRECT_HIT_HOURLY)
        .set(REDIRECT_HIT_HOURLY.REDIRECT_ID, 1)
        .set(REDIRECT_HIT_HOURLY.HOUR, time)
        .set(REDIRECT_HIT_HOURLY.HITS, 3)
        .execute();
    doThrow(new RuntimeException("boom"))
        .when(analyticsRecordingRepository)
        .deleteAggregatedHourlyHits(any());

    // when
    assertThatThrownBy(() -> analyticsRecordingService.aggregateDaily()).hasMessage("boom");

    // then
    assertThat(dsl.fetchCount(REDIRECT_HIT_DAILY)).isZero();
    assertThat(dsl.fetchCount(REDIRECT_HIT_HOURLY)).isEqualTo(1);
  }
}
