package de.jensknipper.re_director.manage_redirects;

import org.jspecify.annotations.Nullable;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;

public record PageContext(
    @Nullable String search,
    @Nullable String status,
    @Nullable Integer code,
    String sort,
    String direction,
    int page,
    int pageSize,
    boolean sortExplicit,
    boolean directionExplicit,
    boolean pageExplicit) {

  public static final String DIRECTION_ASC = "ASC";
  public static final String DIRECTION_DESC = "DESC";

  public static final String SORT_SOURCE = "source";
  public static final String SORT_TARGET = "target";
  public static final String SORT_HTTP_STATUS_CODE = "httpStatusCode";
  public static final Set<String> SORT_ALLOWED_PROPERTIES = Set.of(SORT_SOURCE, SORT_TARGET, SORT_HTTP_STATUS_CODE);

  public static final String DEFAULT_PAGE_SIZE = "20";
  public static final String DEFAULT_PAGE = "0";
  public static final String DEFAULT_SORT = SORT_SOURCE;
  public static final String DEFAULT_DIRECTION = DIRECTION_ASC;

  public PageContext(
      @Nullable String search,
      @Nullable String status,
      @Nullable Integer code,
      String sort,
      String direction,
      int page,
      int pageSize) {
    this(search, status, code, sort, direction, page, pageSize, false, false, false);
  }

  public String toParams() {
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    addParam(uriComponentsBuilder, "search", search);
    addParam(uriComponentsBuilder, "status", status);
    addParam(uriComponentsBuilder, "code", code);
    addParam(uriComponentsBuilder, "sort", sort, DEFAULT_SORT, sortExplicit);
    addParam(uriComponentsBuilder, "direction", direction, DEFAULT_DIRECTION, directionExplicit);
    addParam(uriComponentsBuilder, "page", page, DEFAULT_PAGE, pageExplicit);
    addParam(uriComponentsBuilder, "size", pageSize, DEFAULT_PAGE_SIZE, false);
    return uriComponentsBuilder.build().encode().toUriString();
  }

  public PageContext withPage(int newPage) {
    return new PageContext(
        search,
        status,
        code,
        sort,
        direction,
        newPage,
        pageSize,
        sortExplicit,
        directionExplicit,
        true);
  }

  public PageContext withSort(String newSort) {
    String newDirection = DIRECTION_ASC;
    if (sort.equals(newSort) && direction.equals(DIRECTION_ASC)) {
      newDirection = DIRECTION_DESC;
    }
    return new PageContext(
        search, status, code, newSort, newDirection, 0, pageSize, true, true, false);
  }

  private void addParam(
      UriComponentsBuilder uriComponentsBuilder, String name, @Nullable Object value) {
    if (value != null && !value.toString().isBlank()) {
      uriComponentsBuilder.queryParam(name, value);
    }
  }

  private void addParam(
      UriComponentsBuilder uriComponentsBuilder,
      String name,
      @Nullable Object value,
      String defaultValue,
      boolean includeWhenDefault) {
    if (value == null || value.toString().isBlank()) {
      return;
    }
    if (includeWhenDefault || !value.toString().equals(defaultValue)) {
      uriComponentsBuilder.queryParam(name, value);
    }
  }
}
