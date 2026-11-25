package de.jensknipper.redirector.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class WebConfigurationTest {

  @Autowired RestTestClient restTestClient;

  @Test
  void trailingSlashIsHandledLikeWithoutSlash() {
    restTestClient
        .get()
        .uri("/setup/")
        .exchange()
        .expectStatus()
        .is3xxRedirection()
        .expectHeader()
        .location("/setup");
  }
}
