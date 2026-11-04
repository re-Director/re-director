package de.jensknipper.redirector.auth.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRateLimitFilterTest {

  private final LoginRateLimitFilter filter =
      new LoginRateLimitFilter(new LoginRateLimitCacheConfig().loginRateLimitCaches());

  @Test
  void nonLoginPost_passesThrough_withoutStatusChange() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    MockHttpServletRequest request = loginRequest("1.1.1.1", "someone");
    request.setRequestURI("/other");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
  }

  @Test
  void loginGet_passesThrough_withoutStatusChange() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    MockHttpServletRequest request = loginRequest("1.1.1.1", "someone");
    request.setMethod("GET");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
  }

  @Test
  void missingUsernameParam_fallsBackToEmptyString_withoutThrowing() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
    request.setRemoteAddr("1.1.1.1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void sameIpAndUsername_sixthAttemptWithinAMinute_isRejected() throws Exception {
    for (int i = 1; i <= 5; i++) {
      assertAllowed("2.2.2.2", "victim");
    }
    assertRejected("2.2.2.2", "victim");
  }

  @Test
  void differentUsernames_sameIp_areIndependent_atIpUsernameTier() throws Exception {
    for (int i = 1; i <= 5; i++) {
      assertAllowed("3.3.3.3", "userA");
    }
    for (int i = 1; i <= 5; i++) {
      assertAllowed("3.3.3.3", "userB");
    }
  }

  @Test
  void sameUsername_differentIps_areIndependent_atIpUsernameTier() throws Exception {
    for (int i = 1; i <= 5; i++) {
      assertAllowed("4.4.4.1", "sharedVictim");
    }
    for (int i = 1; i <= 5; i++) {
      assertAllowed("4.4.4.2", "sharedVictim");
    }
  }

  @Test
  void distributedBruteForce_moreThanTwentyDistinctIpsAgainstOneUsername_isRejected()
      throws Exception {
    for (int i = 1; i <= 20; i++) {
      assertAllowed("10.0.0." + i, "targetUser");
    }
    assertRejected("10.0.0.21", "targetUser");
  }

  @Test
  void spraying_manyDistinctUsernamesFromOneIp_isEventuallyRejected() throws Exception {
    for (int i = 1; i <= 20; i++) {
      assertAllowed("5.5.5.5", "sprayUser" + i);
    }
    assertRejected("5.5.5.5", "sprayUser21");
  }

  private void assertAllowed(String ip, String username) throws Exception {
    FilterChain chain = mock(FilterChain.class);
    MockHttpServletRequest request = loginRequest(ip, username);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain, times(1)).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
  }

  private void assertRejected(String ip, String username) throws Exception {
    FilterChain chain = mock(FilterChain.class);
    MockHttpServletRequest request = loginRequest(ip, username);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain, times(0)).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(429);
  }

  private MockHttpServletRequest loginRequest(String ip, String username) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
    request.setRemoteAddr(ip);
    request.setParameter("username", username);
    return request;
  }
}
