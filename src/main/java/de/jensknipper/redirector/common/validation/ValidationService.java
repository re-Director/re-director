package de.jensknipper.redirector.common.validation;

import de.jensknipper.redirector.redirects.manage.ManageRedirectsService;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class ValidationService {

  private final ManageRedirectsService manageRedirectsService;

  public ValidationService(ManageRedirectsService manageRedirectsService) {
    this.manageRedirectsService = manageRedirectsService;
  }

  public void uniqueSource(BindingResult bindingResult, String source, Integer excludeId) {
    boolean exists = manageRedirectsService.redirectAlreadyExists(source, excludeId);
    if (exists) {
      bindingResult.rejectValue("source", "unique.source", "already exists");
    }
  }

  public void uniqueSource(BindingResult bindingResult, String source) {
    boolean exists = manageRedirectsService.redirectAlreadyExists(source);
    if (exists) {
      bindingResult.rejectValue("source", "unique.source", "already exists");
    }
  }
}
