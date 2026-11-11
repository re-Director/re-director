package de.jensknipper.redirector.analytics.dashboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AnalyticsModelAttributes {

  @ModelAttribute
  public void addAnalyticsEnabled(
      Model model, @Value("${re-director.analytics.enabled:false}") boolean analyticsEnabled) {
    model.addAttribute("analyticsEnabled", analyticsEnabled);
  }
}
