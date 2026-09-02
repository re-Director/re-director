package de.jensknipper.redirector.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigurationTest {

  @Autowired MockMvc mockMvc;

  @Test
  void trailingSlashIsHandledLikeWithoutSlash() throws Exception {
    mockMvc
        .perform(get("/setup/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/setup"));
  }
}
