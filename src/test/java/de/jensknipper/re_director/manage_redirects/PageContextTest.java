package de.jensknipper.re_director.manage_redirects;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PageContextTest {

  @Test
  void toParamsShouldOmitDefaultValuesWhenNotExplicit() {
    // given
    PageContext pageContext =
        new PageContext(
            null, null, null, PageContext.DEFAULT_SORT, PageContext.DEFAULT_DIRECTION, 0, 20);

    // when
    String result = pageContext.toParams();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void toParamsShouldIncludeNonDefaultValues() {
    // given
    PageContext pageContext = new PageContext("source", "ACTIVE", 302, "target", "DESC", 3, 25);
    // when
    String result = pageContext.toParams();

    // then
    assertThat(result)
        .isEqualTo(
            "?search=source&status=ACTIVE&code=302&sort=target&direction=DESC&page=3&size=25");
  }

  @Test
  void withPageShouldKeepExplicitDefaultPage() {
    // given
    PageContext pageContext =
        new PageContext(
                null, null, null, PageContext.DEFAULT_SORT, PageContext.DEFAULT_DIRECTION, 0, 20)
            .withPage(0);
    // when
    String result = pageContext.toParams();

    // then
    assertThat(result).isEqualTo("?page=0");
  }

  @Test
  void withSortShouldToggleDirectionWhenStayingOnSameColumn() {
    // given
    PageContext pageContext =
        new PageContext(null, null, null, "target", PageContext.DEFAULT_DIRECTION, 4, 20)
            .withSort("target");
    // when
    String result = pageContext.toParams();

    // then
    assertThat(result).isEqualTo("?sort=target&direction=DESC");
  }

  @Test
  void withSortShouldResetDirectionWhenSwitchingColumns() {
    // given
    PageContext pageContext =
        new PageContext(null, null, null, "target", PageContext.DEFAULT_DIRECTION, 0, 20)
            .withSort(PageContext.DEFAULT_SORT);
    // when
    String result = pageContext.toParams();

    // then
    assertThat(result).isEqualTo("?sort=source&direction=ASC");
  }
}
