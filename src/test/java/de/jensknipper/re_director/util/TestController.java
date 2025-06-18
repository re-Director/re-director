package de.jensknipper.re_director.util;

import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

  @GetMapping("/test")
  public String test() {
    return "it works!";
  }
}
