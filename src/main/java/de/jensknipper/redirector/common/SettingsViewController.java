package de.jensknipper.redirector.common;

import de.jensknipper.redirector.manage_redirects.ManageRedirectsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class SettingsViewController {

  private final ManageRedirectsService manageRedirectsService;

  public SettingsViewController(ManageRedirectsService manageRedirectsService) {
    this.manageRedirectsService = manageRedirectsService;
  }

  @GetMapping("/settings")
  public String settings() {
    return "settings";
  }

  @PostMapping("/settings/cache/redirects/clear")
  public String createRedirect() {
    manageRedirectsService.clearCache();
    return "redirect:/settings";
  }
}
