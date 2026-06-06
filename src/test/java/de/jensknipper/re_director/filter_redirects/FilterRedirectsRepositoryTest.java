package de.jensknipper.re_director.filter_redirects;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.assertj.core.api.Assertions.assertThat;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
import de.jensknipper.re_director.manage_redirects.ManageRedirectsRepository;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FilterRedirectsRepositoryTest {

  @Autowired private DSLContext dsl;
  @Autowired private ManageRedirectsRepository manageRedirectsRepository;
  @Autowired private FilterRedirectsRepository filterRedirectsRepository;

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    String uniqueDb = "jdbc:sqlite:file::memdb-" + UUID.randomUUID() + ":?mode=memory&cache=shared";
    registry.add("spring.datasource.url", () -> uniqueDb);
  }

  @BeforeEach
  void cleanup() {
    dsl.deleteFrom(REDIRECTS).execute();
  }

  @Test
  void findRedirectInformationBySourceShouldFindRelevant() {
    // given
    String source = "source";
    String target = "target";
    manageRedirectsRepository.create(
        source, target, Status.ACTIVE, false, false, RedirectHttpStatusCode.HTTP_302_FOUND);
    manageRedirectsRepository.create(
        "irrelevant",
        "irrelevant",
        Status.ACTIVE,
        false,
        false,
        RedirectHttpStatusCode.HTTP_302_FOUND);

    // when
    RedirectInformation result = filterRedirectsRepository.findRedirectInformationBySource(source);

    // then
    assertThat(result).isNotNull();
    assertThat(result.target()).isEqualTo(target);
    assertThat(result.httpStatusCode()).isEqualTo(RedirectHttpStatusCode.HTTP_302_FOUND);
  }

  @Test
  void findRedirectInformationBySourceShouldReturnNullWhenNotFound() {
    // given
    // when
    RedirectInformation result =
        filterRedirectsRepository.findRedirectInformationBySource("source");

    // then
    assertThat(result).isNull();
  }

  @Test
  void findRedirectInformationBySourceShouldNotFindInactive() {
    // given
    String source = "source";
    String target = "target";
    manageRedirectsRepository.create(
        source, target, Status.INACTIVE, false, false, RedirectHttpStatusCode.HTTP_302_FOUND);

    // when
    RedirectInformation result = filterRedirectsRepository.findRedirectInformationBySource(source);

    // then
    assertThat(result).isNull();
  }
}
