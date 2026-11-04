package de.jensknipper.redirector.auth.ratelimit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class LoginRateLimitIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private LoginRateLimitCaches loginRateLimitCaches;

  @BeforeEach
  void resetRateLimitState() {
    loginRateLimitCaches.clearAll();
  }

  @Test
  void sixthLoginAttemptWithinAMinute_forSameUsername_isRejectedWith429() throws Exception {
    for (int i = 1; i <= 5; i++) {
      mockMvc.perform(loginAttempt("victim", "wrong")).andExpect(status().is3xxRedirection());
    }

    mockMvc.perform(loginAttempt("victim", "wrong")).andExpect(status().isTooManyRequests());
  }

  @Test
  void differentUsername_fromSameClient_isNotAffectedByAnotherUsersLimit() throws Exception {
    for (int i = 1; i <= 5; i++) {
      mockMvc.perform(loginAttempt("victim", "wrong")).andExpect(status().is3xxRedirection());
    }
    mockMvc.perform(loginAttempt("victim", "wrong")).andExpect(status().isTooManyRequests());

    mockMvc.perform(loginAttempt("someone-else", "wrong")).andExpect(status().is3xxRedirection());
  }

  private RequestBuilder loginAttempt(String username, String password) {
    return post("/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("username", username)
        .param("password", password);
  }
}
