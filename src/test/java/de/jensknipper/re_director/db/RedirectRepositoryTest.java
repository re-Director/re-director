package de.jensknipper.re_director.db;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RedirectRepositoryTest {

    @Autowired
    private DSLContext dsl;
    @Autowired
    private RedirectRepository redirectRepository;

    @BeforeEach
    void cleanup() {
        dsl
                .deleteFrom(REDIRECTS)
                .execute();
    }
// TODO test create, delete, updateStatus, indAllFiltered with status
    @Test
    void findAllFilteredShouldFindBySource() {
        // given
        String source = "example.com";
        String target = "http://test.internal";
        insertRedirect(source, target, Status.ACTIVE);
        insertRedirect("irrelevant", "irrelevant", Status.ACTIVE);

        // when
        List<Redirect> result = redirectRepository.findAllFiltered("amp", null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().source()).isEqualTo(source);
        assertThat(result.getFirst().target()).isEqualTo(target);
        assertThat(result.getFirst().status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void findAllFilteredShouldFindByTarget() {
        // given
        String source = "example.com";
        String target = "http://test.internal";
        insertRedirect(source, target, Status.ACTIVE);
        insertRedirect("irrelevant", "irrelevant", Status.ACTIVE);

        // when
        List<Redirect> result = redirectRepository.findAllFiltered("tern", null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().source()).isEqualTo(source);
        assertThat(result.getFirst().target()).isEqualTo(target);
        assertThat(result.getFirst().status()).isEqualTo(Status.ACTIVE);
    }

    // TODO test for null and empty should list all
    @Test
    void findTargetBySourceShouldFindRelevant() {
        // given
        String source = "example.com";
        String target = "http://test.internal";
        insertRedirect(source, target, Status.ACTIVE);
        insertRedirect("irrelevant", "irrelevant", Status.ACTIVE);

        // when
        String result = redirectRepository.findTargetBySource(source);

        // then
        assertThat(result).isEqualTo(target);
    }

    @Test
    void findTargetBySourceShouldNotFindInactive() {
        // given
        String source = "example.com";
        String target = "http://test.internal";
        insertRedirect(source, target, Status.INACTIVE);

        // when
        String result = redirectRepository.findTargetBySource(source);

        // then
        assertThat(result).isNull();
    }

    private void insertRedirect(String source, String target, Status active) {
        dsl
                .insertInto(REDIRECTS)
                .columns(REDIRECTS.SOURCE, REDIRECTS.TARGET, REDIRECTS.STATUS)
                .values(source, target, active)
                .execute();
    }
}