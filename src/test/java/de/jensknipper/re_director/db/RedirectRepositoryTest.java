package de.jensknipper.re_director.db;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.assertj.core.api.Assertions.assertThat;

import de.jensknipper.re_director.db.entity.Redirect;
import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.RedirectInformation;
import de.jensknipper.re_director.db.entity.Status;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RedirectRepositoryTest {

  @Autowired private DSLContext dsl;
  @Autowired private RedirectRepository redirectRepository;

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
  void updateShouldChangeSourceAndTarget() {
    // given
    String source = "source";
    String target = "target";
    int id =
        redirectRepository.create(
            "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.FOUND);
    int otherId =
        redirectRepository.create(
            "irrelevant2", "irrelevant2", Status.ACTIVE, RedirectHttpStatusCode.FOUND);

    // when
    redirectRepository.update(id, source, target, RedirectHttpStatusCode.PERMANENT_REDIRECT);

    // then
    Redirect result = findById(id);
    assertThat(result).isNotNull();
    assertThat(result.source()).isEqualTo(source);
    assertThat(result.target()).isEqualTo(target);
    assertThat(result.httpStatusCode()).isEqualTo(RedirectHttpStatusCode.PERMANENT_REDIRECT);
    Redirect otherResult = findById(otherId);
    assertThat(otherResult).isNotNull();
    assertThat(otherResult.source()).isNotEqualTo(source);
    assertThat(otherResult.target()).isNotEqualTo(target);
    assertThat(otherResult.httpStatusCode()).isEqualTo(RedirectHttpStatusCode.FOUND);
  }

  @Test
  void updateStatusShouldChangeStatus() {
    // given
    int id =
        redirectRepository.create(
            "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.FOUND);
    int otherId =
        redirectRepository.create(
            "irrelevant2", "irrelevant2", Status.ACTIVE, RedirectHttpStatusCode.FOUND);

    // when
    redirectRepository.updateStatus(id, Status.INACTIVE);

    // then
    Redirect result = findById(id);
    assertThat(result).isNotNull();
    assertThat(result.status()).isEqualTo(Status.INACTIVE);
    Redirect otherResult = findById(otherId);
    assertThat(otherResult).isNotNull();
    assertThat(otherResult.status()).isNotEqualTo(Status.INACTIVE);
  }

  @Test
  void deleteShouldDeleteSpecifiedRedirect() {
    // given
    String source = "source";
    String target = "target";
    int id =
        redirectRepository.create(
            source, target, Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);
    int otherId =
        redirectRepository.create(
            "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    redirectRepository.delete(id);

    // then
    Redirect result = findById(id);
    assertThat(result).isNull();
    Redirect other = findById(otherId);
    assertThat(other).isNotNull();
  }

  @Test
  void findAllFilteredShouldListAllWhenNull() {
    // given
    redirectRepository.create(
        "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);
    redirectRepository.create(
        "irrelevant2", "irrelevant2", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    List<Redirect> result = redirectRepository.findAllFiltered(null, null, null);

    // then
    assertThat(result).hasSize(2);
  }

  @Test
  void findAllFilteredShouldListAllWhenEmpty() {
    // given
    redirectRepository.create(
        "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);
    redirectRepository.create(
        "irrelevant2", "irrelevant2", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    List<Redirect> result = redirectRepository.findAllFiltered("", null, null);

    // then
    assertThat(result).hasSize(2);
  }

  @Test
  void findAllFilteredShouldFindBySource() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(
        source, target, Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);
    redirectRepository.create(
        "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    List<Redirect> result = redirectRepository.findAllFiltered("our", null, null);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().source()).isEqualTo(source);
  }

  @Test
  void findAllFilteredShouldFindByTarget() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(
        source, target, Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);
    redirectRepository.create(
        "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    List<Redirect> result = redirectRepository.findAllFiltered("arg", null, null);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().target()).isEqualTo(target);
  }

  @Test
  void findAllFilteredShouldFindByStatus() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(
        source, target, Status.INACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);
    redirectRepository.create(
        "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    List<Redirect> result = redirectRepository.findAllFiltered(null, Status.INACTIVE, null);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().status()).isEqualTo(Status.INACTIVE);
  }

  @Test
  void findAllFilteredShouldFindByCode() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(source, target, Status.INACTIVE, RedirectHttpStatusCode.FOUND);
    redirectRepository.create(
        "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    List<Redirect> result =
        redirectRepository.findAllFiltered(null, null, RedirectHttpStatusCode.FOUND);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().httpStatusCode()).isEqualTo(RedirectHttpStatusCode.FOUND);
  }

  @Test
  void findRedirectInformationBySourceShouldFindRelevant() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(
        source, target, Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);
    redirectRepository.create(
        "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    RedirectInformation result = redirectRepository.findRedirectInformationBySource(source);

    // then
    assertThat(result).isNotNull();
    assertThat(result.target()).isEqualTo(target);
    assertThat(result.httpStatusCode()).isEqualTo(RedirectHttpStatusCode.MOVED_PERMANENTLY);
  }

  @Test
  void findRedirectInformationBySourceShouldReturnNullWhenNotFound() {
    // given
    // when
    RedirectInformation result = redirectRepository.findRedirectInformationBySource("source");

    // then
    assertThat(result).isNull();
  }

  @Test
  void findRedirectInformationBySourceShouldNotFindInactive() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(source, target, Status.INACTIVE, RedirectHttpStatusCode.FOUND);

    // when
    RedirectInformation result = redirectRepository.findRedirectInformationBySource(source);

    // then
    assertThat(result).isNull();
  }

  @Nullable
  public Redirect findById(int id) {
    return dsl.selectFrom(REDIRECTS).where(REDIRECTS.ID.eq(id)).fetchOneInto(Redirect.class);
  }
}
