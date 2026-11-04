package de.jensknipper.redirector.common;

import de.jensknipper.redirector.auth.ratelimit.RateLimitService;
import de.jensknipper.redirector.redirects.manage.ManageRedirectsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class SettingsViewController {

  private final ManageRedirectsService manageRedirectsService;
  private final RateLimitService rateLimitService;

  public SettingsViewController(ManageRedirectsService manageRedirectsService, RateLimitService rateLimitService) {
    this.manageRedirectsService = manageRedirectsService;
    this.rateLimitService = rateLimitService;
  }

  @GetMapping("/settings")
  public String settings() {
    return "settings";
  }

  @PostMapping("/settings/cache/redirects/clear")
  public String clearRedirectCache() {
    manageRedirectsService.clearCache();
    return "redirect:/settings";
  }

  @PostMapping("/settings/cache/rate-limit/login/clear")
  public String clearLoginRateLimitCaches() {
    rateLimitService.clearLoginRateLimitCaches();
    return "redirect:/settings";
  }
}
