package de.jensknipper.re_director.db;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.assertj.core.api.Assertions.assertThat;

import de.jensknipper.re_director.db.entity.Redirect;
import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.RedirectInformation;
import de.jensknipper.re_director.db.entity.Status;
import java.util.List;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RedirectRepositoryTest {

  @Autowired private DSLContext dsl;
  @Autowired private RedirectRepository redirectRepository;

  @BeforeEach
  void cleanup() {
    dsl.deleteFrom(REDIRECTS).execute();
  }

  @Test
  void updateShouldChangeSourceAndTarget() {
    // given
    String source = "source";
    String target = "target";
    int id = redirectRepository.create("irrelevant", "irrelevant");
    int otherId = redirectRepository.create("irrelevant2", "irrelevant2");

    // when
    redirectRepository.update(id, source, target);

    // then
    Redirect result = redirectRepository.findById(id);
    assertThat(result).isNotNull();
    assertThat(result.source()).isEqualTo(source);
    assertThat(result.target()).isEqualTo(target);
    Redirect otherResult = redirectRepository.findById(otherId);
    assertThat(otherResult).isNotNull();
    assertThat(otherResult.source()).isNotEqualTo(source);
    assertThat(otherResult.target()).isNotEqualTo(target);
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
    Redirect result = redirectRepository.findById(id);
    assertThat(result).isNotNull();
    assertThat(result.status()).isEqualTo(Status.INACTIVE);
    Redirect otherResult = redirectRepository.findById(otherId);
    assertThat(otherResult).isNotNull();
    assertThat(otherResult.status()).isNotEqualTo(Status.INACTIVE);
  }

  @Test
  void deleteShouldDeleteSpecifiedRedirect() {
    // given
    String source = "source";
    String target = "target";
    int id = redirectRepository.create(source, target);
    int otherId = redirectRepository.create("irrelevant", "irrelevant");

    // when
    redirectRepository.delete(id);

    // then
    Redirect result = redirectRepository.findById(id);
    assertThat(result).isNull();
    Redirect other = redirectRepository.findById(otherId);
    assertThat(other).isNotNull();
  }

  @Test
  void findAllFilteredShouldListAllWhenNull() {
    // given
    redirectRepository.create("irrelevant", "irrelevant");
    redirectRepository.create("irrelevant2", "irrelevant2");

    // when
    List<Redirect> result = redirectRepository.findAllFiltered(null, null);

    // then
    assertThat(result).hasSize(2);
  }

  @Test
  void findAllFilteredShouldListAllWhenEmpty() {
    // given
    redirectRepository.create("irrelevant", "irrelevant");
    redirectRepository.create("irrelevant2", "irrelevant2");

    // when
    List<Redirect> result = redirectRepository.findAllFiltered("", null);

    // then
    assertThat(result).hasSize(2);
  }

  @Test
  void findAllFilteredShouldFindBySource() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(source, target);
    redirectRepository.create("irrelevant", "irrelevant");

    // when
    List<Redirect> result = redirectRepository.findAllFiltered("our", null);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().source()).isEqualTo(source);
    assertThat(result.getFirst().target()).isEqualTo(target);
    assertThat(result.getFirst().status()).isEqualTo(Status.ACTIVE);
  }

  @Test
  void findAllFilteredShouldFindByTarget() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(source, target);
    redirectRepository.create("irrelevant", "irrelevant");

    // when
    List<Redirect> result = redirectRepository.findAllFiltered("arg", null);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().source()).isEqualTo(source);
    assertThat(result.getFirst().target()).isEqualTo(target);
    assertThat(result.getFirst().status()).isEqualTo(Status.ACTIVE);
  }

  @Test
  void findAllFilteredShouldFindByStatus() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(source, target, Status.INACTIVE, RedirectHttpStatusCode.FOUND);
    redirectRepository.create("irrelevant", "irrelevant");

    // when
    List<Redirect> result = redirectRepository.findAllFiltered(null, Status.INACTIVE);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().source()).isEqualTo(source);
    assertThat(result.getFirst().target()).isEqualTo(target);
    assertThat(result.getFirst().status()).isEqualTo(Status.INACTIVE);
  }

  @Test
  void findRedirectInformationBySourceShouldFindRelevant() {
    // given
    String source = "source";
    String target = "target";
    redirectRepository.create(source, target);
    redirectRepository.create(
        "irrelevant", "irrelevant", Status.ACTIVE, RedirectHttpStatusCode.FOUND);

    // when
    RedirectInformation result = redirectRepository.findRedirectInformationBySource(source);

    // then
    assertThat(result).isNotNull();
    assertThat(result.target()).isEqualTo(target);
    assertThat(result.httpStatusCode()).isEqualTo(RedirectHttpStatusCode.FOUND);
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
}
