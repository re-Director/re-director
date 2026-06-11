package de.jensknipper.re_director.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthViewControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean UserRepository userRepository;

  @Test
  void login_whenNoUsers_redirectsToSetup() throws Exception {
    when(userRepository.count()).thenReturn(0L);

    mockMvc
        .perform(get("/login"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/setup"));
  }

  @Test
  void setup_get_showsFormWhenNoUsers() throws Exception {
    when(userRepository.count()).thenReturn(0L);

    mockMvc
        .perform(get("/setup"))
        .andExpect(status().isOk())
        .andExpect(view().name("setup"))
        .andExpect(model().attributeExists("form"));
  }

  @Test
  void postSetup_passwordMismatch_returnsSetupView() throws Exception {
    when(userRepository.count()).thenReturn(0L);

    mockMvc
        .perform(
            post("/setup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user")
                .param("password", "a")
                .param("confirmPassword", "b"))
        .andExpect(status().isOk())
        .andExpect(view().name("setup"));
  }

  @Test
  void postSetup_success_createsUser_andRedirects() throws Exception {
    when(userRepository.count()).thenReturn(0L);

    String password = "pass";
    mockMvc
        .perform(
            post("/setup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user")
                .param("password", password)
                .param("confirmPassword", password))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));

    // assert no clear text password saved
    verify(userRepository).createUser(any(), argThat(it -> !it.equals(password)), anyBoolean());
  }

  @Test
  void setup_get_redirectsWhenUsersExist() throws Exception {
    when(userRepository.count()).thenReturn(1L);

    mockMvc
        .perform(get("/setup"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  void login_whenAuthenticated_redirectsToRoot() throws Exception {
    when(userRepository.count()).thenReturn(1L);

    mockMvc
        .perform(get("/login").with(user("user")))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }
}
