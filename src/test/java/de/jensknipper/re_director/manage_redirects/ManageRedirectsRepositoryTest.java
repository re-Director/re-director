package de.jensknipper.re_director.manage_redirects;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.assertj.core.api.Assertions.assertThat;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ManageRedirectsRepositoryTest {

  @Autowired private DSLContext dsl;
  @Autowired private ManageRedirectsRepository manageRedirectsRepository;

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    String uniqueDb = "jdbc:sqlite:file::memdb-" + UUID.randomUUID() + ":?mode=memory&cache=shared";
    registry.add("spring.datasource.url", () -> uniqueDb);
  }

  @BeforeEach
  void cleanup() {
    dsl.deleteFrom(REDIRECTS).execute();
  }

  @Nested
  class RedirectAlreadyExists {

    @Test
    void redirectAlreadyExistsShouldReturnFalseWhenNoOtherSource() {
      // given
      String source = "source";
      manageRedirectsRepository.create(
          "irrelevant", "irrelevant", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);

      // when
      boolean result = manageRedirectsRepository.redirectAlreadyExists(source);

      // then
      assertThat(result).isFalse();
    }

    @Test
    void redirectAlreadyExistsShouldReturnTrueWhenAnotherSource() {
      // given
      String source = "source";
      manageRedirectsRepository.create(
          "source", "irrelevant", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);
      manageRedirectsRepository.create(
          "irrelevant", "irrelevant", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);

      // when
      boolean result = manageRedirectsRepository.redirectAlreadyExists(source);

      // then
      assertThat(result).isTrue();
    }

    @Test
    void redirectAlreadyExistsShouldReturnFalseWhenNoOtherSourceAndId() {
      // given
      String source = "source";
      int id =
          manageRedirectsRepository.create(
              "irrelevant",
              "irrelevant",
              Status.ACTIVE,
              false,
              false,
              RedirectHttpStatusCode.FOUND);

      // when
      boolean result = manageRedirectsRepository.redirectAlreadyExists(source, id);

      // then
      assertThat(result).isFalse();
    }

    @Test
    void redirectAlreadyExistsShouldReturnFalseWhenSameSourceAndId() {
      // given
      String source = "source";
      int id =
          manageRedirectsRepository.create(
              "source", "irrelevant", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);
      manageRedirectsRepository.create(
          "irrelevant", "irrelevant", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);

      // when
      boolean result = manageRedirectsRepository.redirectAlreadyExists(source, id);

      // then
      assertThat(result).isFalse();
    }

    @Test
    void redirectAlreadyExistsShouldReturnFalseWhenSameSourceAndDifferentId() {
      // given
      String source = "source";
      manageRedirectsRepository.create(
          "source", "irrelevant", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);
      int id =
          manageRedirectsRepository.create(
              "irrelevant",
              "irrelevant",
              Status.ACTIVE,
              false,
              false,
              RedirectHttpStatusCode.FOUND);

      // when
      boolean result = manageRedirectsRepository.redirectAlreadyExists(source, id);

      // then
      assertThat(result).isTrue();
    }
  }

  @Nested
  class FindAllFiltered {

    @Test
    void findAllFilteredShouldListAllWhenNull() {
      // given
      manageRedirectsRepository.create(
          "irrelevant",
          "irrelevant",
          Status.ACTIVE,
          false,
          false,
          RedirectHttpStatusCode.MOVED_PERMANENTLY);
      manageRedirectsRepository.create(
          "irrelevant2",
          "irrelevant2",
          Status.ACTIVE,
          false,
          false,
          RedirectHttpStatusCode.MOVED_PERMANENTLY);

      // when
      Page<Redirect> result =
          manageRedirectsRepository.findAllFiltered(null, null, null, Pageable.ofSize(10));

      // then
      assertThat(result).hasSize(2);
    }

    @Test
    void findAllFilteredShouldListAllWhenEmpty() {
      // given
      manageRedirectsRepository.create(
          "irrelevant",
          "irrelevant",
          Status.ACTIVE,
          false,
          false,
          RedirectHttpStatusCode.MOVED_PERMANENTLY);
      manageRedirectsRepository.create(
          "irrelevant2",
          "irrelevant2",
          Status.ACTIVE,
          false,
          false,
          RedirectHttpStatusCode.MOVED_PERMANENTLY);

      // when
      Page<Redirect> result =
          manageRedirectsRepository.findAllFiltered("", null, null, Pageable.ofSize(10));

      // then
      assertThat(result).hasSize(2);
    }

    @Test
    void findAllFilteredShouldFindBySource() {
      // given
      String source = "source";
      String target = "target";
      manageRedirectsRepository.create(
          source, target, Status.ACTIVE, false, false, RedirectHttpStatusCode.MOVED_PERMANENTLY);
      manageRedirectsRepository.create(
          "irrelevant",
          "irrelevant",
          Status.ACTIVE,
          false,
          false,
          RedirectHttpStatusCode.MOVED_PERMANENTLY);

      // when
      Page<Redirect> result =
          manageRedirectsRepository.findAllFiltered("our", null, null, Pageable.ofSize(10));

      // then
      assertThat(result).hasSize(1);
      assertThat(result.getContent().getFirst().source()).isEqualTo(source);
    }

    @Test
    void findAllFilteredShouldFindByTarget() {
      // given
      String source = "source";
      String target = "target";
      manageRedirectsRepository.create(
          source, target, Status.ACTIVE, false, false, RedirectHttpStatusCode.MOVED_PERMANENTLY);
      manageRedirectsRepository.create(
          "irrelevant",
          "irrelevant",
          Status.ACTIVE,
          false,
          false,
          RedirectHttpStatusCode.MOVED_PERMANENTLY);

      // when
      Page<Redirect> result =
          manageRedirectsRepository.findAllFiltered("arg", null, null, Pageable.ofSize(10));

      // then
      assertThat(result).hasSize(1);
      assertThat(result.getContent().getFirst().target()).isEqualTo(target);
    }

    @Test
    void findAllFilteredShouldFindByStatus() {
      // given
      String source = "source";
      String target = "target";
      manageRedirectsRepository.create(
          source, target, Status.INACTIVE, false, false, RedirectHttpStatusCode.MOVED_PERMANENTLY);
      manageRedirectsRepository.create(
          "irrelevant",
          "irrelevant",
          Status.ACTIVE,
          false,
          false,
          RedirectHttpStatusCode.MOVED_PERMANENTLY);

      // when
      Page<Redirect> result =
          manageRedirectsRepository.findAllFiltered(
              null, Status.INACTIVE, null, Pageable.ofSize(10));

      // then
      assertThat(result).hasSize(1);
      assertThat(result.getContent().getFirst().status()).isEqualTo(Status.INACTIVE);
    }

    @Test
    void findAllFilteredShouldFindByCode() {
      // given
      String source = "source";
      String target = "target";
      manageRedirectsRepository.create(
          source, target, Status.INACTIVE, false, false, RedirectHttpStatusCode.FOUND);
      manageRedirectsRepository.create(
          "irrelevant",
          "irrelevant",
          Status.ACTIVE,
          false,
          false,
          RedirectHttpStatusCode.MOVED_PERMANENTLY);

      // when
      Page<Redirect> result =
          manageRedirectsRepository.findAllFiltered(
              null, null, RedirectHttpStatusCode.FOUND, Pageable.ofSize(10));

      // then
      assertThat(result).hasSize(1);
      assertThat(result.getContent().getFirst().httpStatusCode())
          .isEqualTo(RedirectHttpStatusCode.FOUND);
    }

    @Test
    void findAllFilteredShouldFindPaged() {
      // given
      String source = "source";
      String target = "target";
      manageRedirectsRepository.create(
          source, target, Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);
      manageRedirectsRepository.create(
          "irrelevant2", "irrelevant2", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);

      // when
      Page<Redirect> result =
          manageRedirectsRepository.findAllFiltered(null, null, null, Pageable.ofSize(1));

      // then
      assertThat(result).hasSize(1);
      assertThat(result.getContent().getFirst().source()).isEqualTo(source);
    }
  }

  @Test
  void updateShouldChangeSourceAndTarget() {
    // given
    String source = "source";
    String target = "target";
    int id =
        manageRedirectsRepository.create(
            "irrelevant", "irrelevant", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);
    int otherId =
        manageRedirectsRepository.create(
            "irrelevant2",
            "irrelevant2",
            Status.ACTIVE,
            false,
            false,
            RedirectHttpStatusCode.FOUND);

    // when
    manageRedirectsRepository.update(
        id, source, target, true, true, RedirectHttpStatusCode.PERMANENT_REDIRECT);

    // then
    Redirect result = manageRedirectsRepository.findById(id);
    assertThat(result).isNotNull();
    assertThat(result.source()).isEqualTo(source);
    assertThat(result.target()).isEqualTo(target);
    assertThat(result.pathForwarding()).isTrue();
    assertThat(result.queryForwarding()).isTrue();
    assertThat(result.httpStatusCode()).isEqualTo(RedirectHttpStatusCode.PERMANENT_REDIRECT);
    Redirect otherResult = manageRedirectsRepository.findById(otherId);
    assertThat(otherResult).isNotNull();
    assertThat(otherResult.source()).isNotEqualTo(source);
    assertThat(otherResult.target()).isNotEqualTo(target);
    assertThat(otherResult.pathForwarding()).isFalse();
    assertThat(otherResult.queryForwarding()).isFalse();
    assertThat(otherResult.httpStatusCode()).isEqualTo(RedirectHttpStatusCode.FOUND);
  }

  @Test
  void updateStatusShouldChangeStatus() {
    // given
    int id =
        manageRedirectsRepository.create(
            "irrelevant", "irrelevant", Status.ACTIVE, false, false, RedirectHttpStatusCode.FOUND);
    int otherId =
        manageRedirectsRepository.create(
            "irrelevant2",
            "irrelevant2",
            Status.ACTIVE,
            false,
            false,
            RedirectHttpStatusCode.FOUND);

    // when
    manageRedirectsRepository.updateStatus(id, Status.INACTIVE);

    // then
    Redirect result = manageRedirectsRepository.findById(id);
    assertThat(result).isNotNull();
    assertThat(result.status()).isEqualTo(Status.INACTIVE);
    Redirect otherResult = manageRedirectsRepository.findById(otherId);
    assertThat(otherResult).isNotNull();
    assertThat(otherResult.status()).isNotEqualTo(Status.INACTIVE);
  }

  @Test
  void deleteShouldDeleteSpecifiedRedirect() {
    // given
    String source = "source";
    String target = "target";
    int id =
        manageRedirectsRepository.create(
            source, target, Status.ACTIVE, false, false, RedirectHttpStatusCode.MOVED_PERMANENTLY);
    int otherId =
        manageRedirectsRepository.create(
            "irrelevant",
            "irrelevant",
            Status.ACTIVE,
            false,
            false,
            RedirectHttpStatusCode.MOVED_PERMANENTLY);

    // when
    manageRedirectsRepository.delete(id);

    // then
    Redirect result = manageRedirectsRepository.findById(id);
    assertThat(result).isNull();
    Redirect other = manageRedirectsRepository.findById(otherId);
    assertThat(other).isNotNull();
  }
}
