package de.jensknipper.re_director.filter_redirects;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BaseUrlTest {

  @Test
  void shouldReturnNull_whenBaseUrlNull() {
    // when
    BaseUrl a = new BaseUrl(null);

    // then
    assertThat(a.getBaseUrl()).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "ht!tps://bad-url", "https://\\bad-url"})
  void shouldReturnNull_whenBaseUrlNotValid(String baseUrl) {
    // when
    BaseUrl a = new BaseUrl(baseUrl);

    // then
    assertThat(a.getBaseUrl()).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"   HTTP://Example.COM:8080/some/path ", "Example.COM:8080"})
  void shouldNormalize_andExtractHost_withScheme(String baseUrl) {
    // when
    BaseUrl a = new BaseUrl(baseUrl);

    // then
    assertThat(a.getBaseUrl()).isEqualTo("example.com");
  }
}
