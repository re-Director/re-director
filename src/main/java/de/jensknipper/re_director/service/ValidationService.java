package de.jensknipper.re_director.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class ValidationService {

  private final RedirectService redirectService;

  public ValidationService(RedirectService redirectService) {
    this.redirectService = redirectService;
  }

  public void uniqueSource(BindingResult bindingResult, String source, Integer excludeId) {
    boolean exists = redirectService.redirectAlreadyExists(source, excludeId);
    if (exists) {
      bindingResult.rejectValue("source", "unique.source", "already exists");
    }
  }

  public void uniqueSource(BindingResult bindingResult, String source) {
    boolean exists = redirectService.redirectAlreadyExists(source);
    if (exists) {
      bindingResult.rejectValue("source", "unique.source", "already exists");
    }
  }
}
