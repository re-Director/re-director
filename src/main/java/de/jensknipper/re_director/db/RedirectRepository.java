package de.jensknipper.re_director.db;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;

import de.jensknipper.re_director.db.entity.Redirect;
import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.RedirectInformation;
import de.jensknipper.re_director.db.entity.Status;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class RedirectRepository {

  public static final RedirectHttpStatusCode DEFAULT_REDIRECT =
      RedirectHttpStatusCode.MOVED_PERMANENTLY;
  private final DSLContext dsl;

  public RedirectRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public boolean redirectAlreadyExists(String source) {
    return dsl.fetchExists(dsl.selectOne().from(REDIRECTS).where(REDIRECTS.SOURCE.eq(source)));
  }

  @Nullable
  public RedirectInformation findRedirectInformationBySource(String source) {
    return dsl.select(REDIRECTS.TARGET, REDIRECTS.HTTP_STATUS_CODE)
        .from(REDIRECTS)
        .where(REDIRECTS.SOURCE.eq(source).and(REDIRECTS.STATUS.eq(Status.ACTIVE)))
        .fetchOneInto(RedirectInformation.class);
  }

  public List<Redirect> findAllFiltered(@Nullable String search, @Nullable Status status) {
    Condition searchFilterCondition =
        DSL.condition(search == null || search.isBlank())
            .or(
                REDIRECTS
                    .SOURCE
                    .likeIgnoreCase("%" + search + "%")
                    .or(REDIRECTS.TARGET.likeIgnoreCase("%" + search + "%")));
    Condition statusFilterCondition = DSL.condition(status == null).or(REDIRECTS.STATUS.eq(status));

    return dsl.selectFrom(REDIRECTS)
        .where(searchFilterCondition.and(statusFilterCondition))
        .fetchInto(Redirect.class);
  }

  public int create(
      String source, String target, Status status, RedirectHttpStatusCode statusCode) {
    return Objects.requireNonNull(
            dsl.insertInto(REDIRECTS)
                .columns(
                    REDIRECTS.SOURCE,
                    REDIRECTS.TARGET,
                    REDIRECTS.STATUS,
                    REDIRECTS.HTTP_STATUS_CODE)
                .values(source, target, status, statusCode)
                .returningResult(REDIRECTS.ID)
                .fetchOne())
        .getValue(REDIRECTS.ID);
  }

  public void update(int id, String source, String target, RedirectHttpStatusCode statusCode) {
    dsl.update(REDIRECTS)
        .set(REDIRECTS.SOURCE, source)
        .set(REDIRECTS.TARGET, target)
        .set(REDIRECTS.HTTP_STATUS_CODE, statusCode)
        .where(REDIRECTS.ID.eq(id))
        .execute();
  }

  public void updateStatus(int id, Status status) {
    dsl.update(REDIRECTS).set(REDIRECTS.STATUS, status).where(REDIRECTS.ID.eq(id)).execute();
  }

  public void delete(int id) {
    dsl.deleteFrom(REDIRECTS).where(REDIRECTS.ID.eq(id)).execute();
  }
}
