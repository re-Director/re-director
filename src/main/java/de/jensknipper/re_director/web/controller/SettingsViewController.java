package de.jensknipper.re_director.web.controller;

import de.jensknipper.re_director.service.RedirectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SettingsViewController {

  private final RedirectService redirectService;

  public SettingsViewController(RedirectService redirectService) {
    this.redirectService = redirectService;
  }

  @GetMapping("/settings")
  public String settings(Model model) {
    return "settings";
  }

  @PostMapping("/settings/cache/redirects/clear")
  public String createRedirect() {
    redirectService.clearCache();
    return "redirect:/settings";
  }
}
