package de.jensknipper.re_director.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeViewController {

  @GetMapping("/")
  public String home(Model model) {
    return "home";
  }
}
