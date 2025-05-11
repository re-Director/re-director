package de.jensknipper.re_director.db;

import jakarta.annotation.Nullable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;

@Repository
public class RedirectRepository {

    private final DSLContext dsl;

    public RedirectRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Nullable
    public String findTargetBySource(String source) {
        return dsl.selectFrom(REDIRECTS)
                .where(REDIRECTS.SOURCE.eq(source)
                        .and(REDIRECTS.STATUS.eq(Status.ACTIVE)))
                .fetchOne(REDIRECTS.TARGET);
    }

    public List<Redirect> findAllFiltered(String search, Status status) {
        Condition searchFilterCondition =
                DSL.condition(search == null || search.isBlank())
                .or(REDIRECTS.SOURCE.likeIgnoreCase("%" + search + "%")
                        .or(REDIRECTS.TARGET.likeIgnoreCase("%" + search + "%")));
        Condition statusFilterCondition =
                DSL.condition(status == null)
                        .or(REDIRECTS.STATUS.eq(status));

        return dsl.selectFrom(REDIRECTS)
                .where(searchFilterCondition.and(statusFilterCondition))
                .fetchInto(Redirect.class);
    }

    public void create(String source, String target) {
        dsl
                .insertInto(REDIRECTS)
                .columns(REDIRECTS.SOURCE, REDIRECTS.TARGET)
                .values(source, target)
                .execute();
    }

    public void update(int id, String source, String target) {
        dsl.update(REDIRECTS)
                .set(REDIRECTS.SOURCE, source)
                .set(REDIRECTS.TARGET, target)
                .where(REDIRECTS.ID.eq(id))
                .execute();
    }

    public void updateStatus(int id, Status status) {
        dsl.update(REDIRECTS)
                .set(REDIRECTS.STATUS, status)
                .where(REDIRECTS.ID.eq(id))
                .execute();
    }

    public void delete(int id) {
        dsl
                .deleteFrom(REDIRECTS)
                .where(REDIRECTS.ID.eq(id))
                .execute();
    }
}
