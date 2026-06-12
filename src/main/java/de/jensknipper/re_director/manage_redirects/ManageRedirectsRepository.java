package de.jensknipper.re_director.manage_redirects;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.jooq.impl.DSL.not;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
import java.util.*;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;

@Repository
public class ManageRedirectsRepository {

  public static final RedirectHttpStatusCode DEFAULT_REDIRECT =
      RedirectHttpStatusCode.HTTP_301_MOVED_PERMANENTLY;
  private static final Field<?> DEFAULT_SORT_FIELD = REDIRECTS.SOURCE;
  private static final char LIKE_ESCAPE_CHAR = '\\';

  private final DSLContext dsl;

  public ManageRedirectsRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public boolean redirectAlreadyExists(String source) {
    return dsl.fetchExists(dsl.selectOne().from(REDIRECTS).where(REDIRECTS.SOURCE.eq(source)));
  }

  public boolean redirectAlreadyExists(String source, int id) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(REDIRECTS)
            .where(REDIRECTS.SOURCE.eq(source).and(not(REDIRECTS.ID.eq(id)))));
  }

  @Nullable
  public Redirect findById(int id) {
    return dsl.selectFrom(REDIRECTS).where(REDIRECTS.ID.eq(id)).fetchOneInto(Redirect.class);
  }

  public Page<Redirect> findAllFiltered(
      @Nullable String search,
      @Nullable Status status,
      @Nullable RedirectHttpStatusCode httpStatusCodeFilter,
      Pageable pageable) {

    Optional<String> likePattern = createLikePattern(search);
    Condition searchFilterCondition =
        DSL.condition(likePattern.isEmpty())
            .or(
                REDIRECTS
                    .SOURCE
                    .likeIgnoreCase(likePattern.orElse(""), LIKE_ESCAPE_CHAR)
                    .or(REDIRECTS.TARGET.likeIgnoreCase(likePattern.orElse(""), LIKE_ESCAPE_CHAR)));
    Condition statusFilterCondition = DSL.condition(status == null).or(REDIRECTS.STATUS.eq(status));
    Condition httpStatusCodeFilterCondition =
        DSL.condition(httpStatusCodeFilter == null)
            .or(REDIRECTS.HTTP_STATUS_CODE.eq(httpStatusCodeFilter));

    Condition combinedCondition =
        searchFilterCondition.and(statusFilterCondition).and(httpStatusCodeFilterCondition);

    int totalElements = dsl.fetchCount(dsl.selectFrom(REDIRECTS).where(combinedCondition));

    List<Redirect> content =
        dsl.selectFrom(REDIRECTS)
            .where(combinedCondition)
            .orderBy(getSortFields(pageable))
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetchInto(Redirect.class);

    return new PageImpl<>(content, pageable, totalElements);
  }

  private Optional<String> createLikePattern(@Nullable String input) {
    return Optional.ofNullable(input)
        .filter(it -> !it.isBlank())
        .map(it -> it.replace("\\", "\\\\"))
        .map(it -> it.replace("%", "\\%"))
        .map(it -> it.replace("_", "\\_"))
        .map(it -> "%" + it + "%");
  }

  private List<SortField<?>> getSortFields(Pageable pageable) {
    if (pageable.getSort().isUnsorted()) {
      return List.of(DEFAULT_SORT_FIELD.asc());
    }
    List<SortField<?>> sortFields =
        pageable.getSort().stream().map(this::toSortField).collect(Collectors.toList());
    sortFields.add(REDIRECTS.ID.asc());
    return List.copyOf(sortFields);
  }

  private SortField<?> toSortField(Order order) {
    Field<?> field =
        Arrays.stream(REDIRECTS.fields())
            .filter(it -> it.getName().equals(order.getProperty()))
            .findFirst()
            .orElse(DEFAULT_SORT_FIELD);
    if (order.isAscending()) {
      return field.asc();
    }
    return field.desc();
  }

  public int create(
      String source,
      String target,
      Status status,
      boolean pathForwarding,
      boolean queryForwarding,
      RedirectHttpStatusCode statusCode) {
    return Objects.requireNonNull(
            dsl.insertInto(REDIRECTS)
                .columns(
                    REDIRECTS.SOURCE,
                    REDIRECTS.TARGET,
                    REDIRECTS.STATUS,
                    REDIRECTS.PATH_FORWARDING,
                    REDIRECTS.QUERY_FORWARDING,
                    REDIRECTS.HTTP_STATUS_CODE)
                .values(source, target, status, pathForwarding, queryForwarding, statusCode)
                .returningResult(REDIRECTS.ID)
                .fetchOne())
        .getValue(REDIRECTS.ID);
  }

  public void update(
      int id,
      String source,
      String target,
      boolean pathForwarding,
      boolean queryForwarding,
      RedirectHttpStatusCode statusCode) {
    dsl.update(REDIRECTS)
        .set(REDIRECTS.SOURCE, source)
        .set(REDIRECTS.TARGET, target)
        .set(REDIRECTS.PATH_FORWARDING, pathForwarding)
        .set(REDIRECTS.QUERY_FORWARDING, queryForwarding)
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
