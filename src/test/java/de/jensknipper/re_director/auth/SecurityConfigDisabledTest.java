package de.jensknipper.re_director.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "re-director.auth.enabled=false")
@AutoConfigureMockMvc
class SecurityConfigDisabledTest {

  @Autowired MockMvc mockMvc;

  @Test
  void whenAuthDisabled_noRedirectToLogin() throws Exception {
    mockMvc.perform(get("/irrelevant")).andExpect(status().isNotFound());
  }
}
