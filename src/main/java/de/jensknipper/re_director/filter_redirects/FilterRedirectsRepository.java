package de.jensknipper.re_director.filter_redirects;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;

import de.jensknipper.re_director.common.db.Status;
import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public class FilterRedirectsRepository {

  private final DSLContext dsl;

  public FilterRedirectsRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Nullable
  public RedirectInformation findRedirectInformationBySource(String source) {
    return dsl.select(
            REDIRECTS.ID,
            REDIRECTS.TARGET,
            REDIRECTS.HTTP_STATUS_CODE,
            REDIRECTS.PATH_FORWARDING,
            REDIRECTS.QUERY_FORWARDING)
        .from(REDIRECTS)
        .where(REDIRECTS.SOURCE.eq(source).and(REDIRECTS.STATUS.eq(Status.ACTIVE)))
        .fetchOneInto(RedirectInformation.class);
  }
}
