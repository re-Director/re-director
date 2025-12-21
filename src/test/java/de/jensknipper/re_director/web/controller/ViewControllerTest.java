package de.jensknipper.re_director.web.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import de.jensknipper.re_director.service.RedirectService;
import de.jensknipper.re_director.service.ValidationService;
import de.jensknipper.re_director.web.controller.dto.DtoMapper;
import gg.jte.springframework.boot.autoconfigure.JteAutoConfiguration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ViewController.class)
@Import({
  ValidationService.class,
  DtoMapper.class,
  RedirectService.class,
  JteAutoConfiguration.class
})
class ViewControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean RedirectService redirectService;

  @Nested
  class ListRedirects {
    @Test
    void shouldAllow_listRedirects() throws Exception {
      mockMvc
          .perform(get("/redirects"))
          .andExpect(status().isOk())
          .andExpect(view().name("redirects"));
    }

    @Test
    void shouldAllow_listRedirects_nullFilter() throws Exception {
      mockMvc
          .perform(
              get("/redirects")
                  .param("search", (String) null)
                  .param("status", (String) null)
                  .param("code", (String) null))
          .andExpect(status().isOk())
          .andExpect(view().name("redirects"));
    }

    @Test
    void shouldAllow_listRedirects_emptyFilter() throws Exception {
      mockMvc
          .perform(get("/redirects").param("search", "").param("status", "").param("code", ""))
          .andExpect(status().isOk())
          .andExpect(view().name("redirects"));
    }
  }

  @Nested
  class CreateRedirect {

    private static Stream<Arguments> provideValidFields() {
      return Stream.of(
          Arguments.of("source", "http://valid", "301"),
          Arguments.of("source", "https://valid", "302"),
          Arguments.of("source", "http://valid:8080", "307"),
          Arguments.of("source", "ftp://valid", "308"));
    }

    @ParameterizedTest
    @MethodSource("provideValidFields")
    void shouldAllow_validationCreate(String source, String target, String httpStatusCode)
        throws Exception {
      mockMvc
          .perform(
              post("/redirects/create")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("source", "source")
                  .param("target", "http://valid")
                  .param("httpStatusCode", "301"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/redirects"));
    }

    private static Stream<Arguments> provideInvalidSourceFields() {
      return Stream.of(Arguments.of((String) null), Arguments.of(""), Arguments.of("duplicate"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidSourceFields")
    void shouldFail_validationCreate_whenSourceFieldIsInvalid(String invalidSource)
        throws Exception {
      when(redirectService.redirectAlreadyExists(eq("duplicate"))).thenReturn(true);
      mockMvc
          .perform(
              post("/redirects/create")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("source", invalidSource)
                  .param("target", "http://valid")
                  .param("httpStatusCode", "301"))
          .andExpect(status().isOk())
          .andExpect(view().name("redirects"))
          .andExpect(model().attributeHasFieldErrors("createRedirectRequest", "source"));
    }

    private static Stream<Arguments> provideInvalidTargetFields() {
      return Stream.of(
          Arguments.of((String) null),
          Arguments.of(""),
          Arguments.of("invalid"),
          Arguments.of("htt://invalid"),
          Arguments.of("htp://invalid:8080"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTargetFields")
    void shouldFail_validationCreate_whenTargetFieldIsInvalid(String invalidTarget)
        throws Exception {
      mockMvc
          .perform(
              post("/redirects/create")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("source", "source")
                  .param("target", invalidTarget)
                  .param("httpStatusCode", "301"))
          .andExpect(status().isOk())
          .andExpect(view().name("redirects"))
          .andExpect(model().attributeHasFieldErrors("createRedirectRequest", "target"));
    }

    private static Stream<Arguments> provideInvalidCodeFields() {
      return Stream.of(
          Arguments.of((String) null),
          Arguments.of(""),
          Arguments.of("invalid"),
          Arguments.of("0"),
          Arguments.of("399"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCodeFields")
    void shouldFail_validationCreate_whenCodeFieldIsInvalid(String invalidCode) throws Exception {
      mockMvc
          .perform(
              post("/redirects/create")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("source", "source")
                  .param("target", "http://valid")
                  .param("httpStatusCode", invalidCode))
          .andExpect(status().isOk())
          .andExpect(view().name("redirects"))
          .andExpect(model().attributeHasFieldErrors("createRedirectRequest", "httpStatusCode"));
    }
  }
}
