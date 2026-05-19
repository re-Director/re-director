package de.jensknipper.re_director.test_redirects;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestRedirectViewController {

  private final TestRedirectService testRedirectService;

  public TestRedirectViewController(TestRedirectService testRedirectService) {
    this.testRedirectService = testRedirectService;
  }

  @GetMapping("/test-redirect")
  public String home(
      @Valid TestRedirectRequest testRedirectRequest, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("bindingResult", bindingResult);
    }
    String url = testRedirectRequest.url();
    if (url != null && !bindingResult.hasErrors()) {
      TestRedirectResult result = testRedirectService.getRedirectionSteps(url);
      model.addAttribute("result", result);
    } else {
      model.addAttribute("result", TestRedirectResult.EMPTY);
    }
    model.addAttribute("url", url);
    return "test-redirect";
  }
}
