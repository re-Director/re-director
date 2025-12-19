package de.jensknipper.re_director.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.jensknipper.re_director.db.RedirectRepository;
import de.jensknipper.re_director.db.entity.Redirect;
import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.RedirectInformation;
import de.jensknipper.re_director.db.entity.Status;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RedirectServiceCachingTest {

  @Autowired RedirectService redirectService;
  @Autowired CacheManager cacheManager;
  @MockitoBean private RedirectRepository redirectRepository;

  private static final Redirect exampleRedirect =
      new Redirect(
          0, "source", "target", Status.ACTIVE, LocalDateTime.now(), RedirectHttpStatusCode.FOUND);
  private static final RedirectInformation exampleRedirectInformation =
      new RedirectInformation(exampleRedirect.target(), exampleRedirect.httpStatusCode());

  @BeforeEach
  void beforeEach() {
    when(redirectRepository.findRedirectInformationBySource(eq(exampleRedirect.source())))
        .thenReturn(exampleRedirectInformation);
    redirectService.findRedirectInformationBySource(exampleRedirect.source());

    when(redirectRepository.findRedirectInformationBySource(eq("dummy")))
        .thenReturn(new RedirectInformation("dummy", RedirectHttpStatusCode.FOUND));
    redirectService.findRedirectInformationBySource("dummy");
  }

  @Test
  void findRedirectInformationBySource_shouldPlaceValuesIntoCache() {
    // given
    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result.isPresent());
    assertThat(result.map(RedirectInformation::target)).hasValue(exampleRedirect.target());
    assertThat(result.map(RedirectInformation::httpStatusCode))
        .hasValue(exampleRedirect.httpStatusCode());
  }

  @Test
  void update_shouldEvictValueFromCache() {
    // given
    redirectService.update(
        exampleRedirect.id(),
        exampleRedirect.source(),
        exampleRedirect.target(),
        exampleRedirect.httpStatusCode());

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result.isEmpty());
  }

  @Test
  void updateStatus_toInactive_shouldEvictValueFromCache() {
    // given
    redirectService.updateStatus(exampleRedirect.id(), Status.INACTIVE);

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result.isEmpty());
  }

  @Test
  void updateStatus_toActive_shouldNotEvictValueFromCache() {
    // given
    redirectService.updateStatus(exampleRedirect.id(), Status.INACTIVE);

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result.isPresent());
    assertThat(result.map(RedirectInformation::target)).hasValue(exampleRedirect.target());
    assertThat(result.map(RedirectInformation::httpStatusCode))
        .hasValue(exampleRedirect.httpStatusCode());
  }

  @Test
  void delete_shouldEvictValueFromCache() {
    // given
    redirectService.delete(exampleRedirect.id());

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result.isEmpty());
  }

  private Optional<RedirectInformation> getRedirectFromCache() {
    return Optional.ofNullable(cacheManager.getCache("redirects"))
        .map(it -> it.get(exampleRedirect.source(), RedirectInformation.class));
  }
}
