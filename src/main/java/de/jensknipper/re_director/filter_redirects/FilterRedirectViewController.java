package de.jensknipper.re_director.filter_redirects;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FilterRedirectViewController {

  @GetMapping("/no-redirect-found")
  public String noRedirectFound() {
    return "no-redirect-found";
  }
}
