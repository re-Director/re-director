package de.jensknipper.re_director;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
import de.jensknipper.re_director.filter_redirects.FilterRedirectsRepository;
import de.jensknipper.re_director.filter_redirects.FilterRedirectsService;
import de.jensknipper.re_director.filter_redirects.RedirectInformation;
import de.jensknipper.re_director.manage_redirects.ManageRedirectsRepository;
import de.jensknipper.re_director.manage_redirects.ManageRedirectsService;
import de.jensknipper.re_director.manage_redirects.Redirect;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RedirectCachingTest {

  @Autowired ManageRedirectsService manageRedirectsService;
  @Autowired FilterRedirectsService filterRedirectsService;

  @Autowired CacheManager cacheManager;
  @MockitoBean private ManageRedirectsRepository manageRedirectsRepository;
  @MockitoBean private FilterRedirectsRepository filterRedirectsRepository;

  private static final Redirect exampleRedirect =
      new Redirect(
          0,
          "source",
          "target",
          Status.ACTIVE,
          LocalDateTime.now(),
          RedirectHttpStatusCode.FOUND,
          false,
          false);
  private static final RedirectInformation exampleRedirectInformation =
      new RedirectInformation(
          exampleRedirect.target(),
          exampleRedirect.httpStatusCode(),
          exampleRedirect.pathForwarding(),
          exampleRedirect.queryForwarding());

  @BeforeEach
  void beforeEach() {
    when(filterRedirectsRepository.findRedirectInformationBySource(exampleRedirect.source()))
        .thenReturn(exampleRedirectInformation);
    filterRedirectsService.findRedirectInformationBySource(exampleRedirect.source());

    when(manageRedirectsRepository.findById(exampleRedirect.id())).thenReturn(exampleRedirect);
    when(filterRedirectsRepository.findRedirectInformationBySource("dummy"))
        .thenReturn(new RedirectInformation("dummy", RedirectHttpStatusCode.FOUND, false, false));
    filterRedirectsService.findRedirectInformationBySource("dummy");
  }

  @Test
  void findRedirectInformationBySource_shouldPlaceValuesIntoCache() {
    // given
    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result).isPresent();
    assertThat(result.map(RedirectInformation::target)).hasValue(exampleRedirect.target());
    assertThat(result.map(RedirectInformation::httpStatusCode))
        .hasValue(exampleRedirect.httpStatusCode());
  }

  @Test
  void update_shouldEvictValueFromCache() {
    // given
    manageRedirectsService.update(
        exampleRedirect.id(),
        exampleRedirect.source(),
        exampleRedirect.target(),
        exampleRedirect.pathForwarding(),
        exampleRedirect.queryForwarding(),
        exampleRedirect.httpStatusCode());

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void updateStatus_toInactive_shouldEvictValueFromCache() {
    // given
    manageRedirectsService.updateStatus(exampleRedirect.id(), Status.INACTIVE);

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void updateStatus_toActive_shouldNotEvictValueFromCache() {
    // given
    manageRedirectsService.updateStatus(exampleRedirect.id(), Status.ACTIVE);

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result).isPresent();
    assertThat(result.map(RedirectInformation::target)).hasValue(exampleRedirect.target());
    assertThat(result.map(RedirectInformation::httpStatusCode))
        .hasValue(exampleRedirect.httpStatusCode());
  }

  @Test
  void delete_shouldEvictValueFromCache() {
    // given
    manageRedirectsService.delete(exampleRedirect.id());

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void clearCache_shouldEvictCache() {
    // given
    manageRedirectsService.clearCache();

    // when
    Optional<RedirectInformation> result = getRedirectFromCache();

    // then
    assertThat(result).isEmpty();
  }

  private Optional<RedirectInformation> getRedirectFromCache() {
    return Optional.ofNullable(cacheManager.getCache("redirects"))
        .map(it -> it.get(exampleRedirect.source(), RedirectInformation.class));
  }
}
