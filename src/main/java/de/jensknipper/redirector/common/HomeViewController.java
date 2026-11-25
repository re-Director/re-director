package de.jensknipper.redirector.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class HomeViewController {

  @GetMapping("/")
  public String home() {
    return "home";
  }
}
