package de.jensknipper.re_director.auth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigEnabledTest {

  @Autowired MockMvc mockMvc;

  @Test
  void whenAuthEnabled_redirectsToLogin() throws Exception {
    mockMvc
        .perform(get("/irrelevant"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/login")));
  }

  @Test
  void whenAuthenticated_accessProtectedPath_noRedirect() throws Exception {
    mockMvc.perform(get("/irrelevant").with(user("admin"))).andExpect(status().isNotFound());
  }
}
