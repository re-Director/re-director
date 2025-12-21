package de.jensknipper.re_director.web.controller;

import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.Status;
import de.jensknipper.re_director.service.RedirectService;
import de.jensknipper.re_director.service.ValidationService;
import de.jensknipper.re_director.web.controller.dto.CreateRedirectRequest;
import de.jensknipper.re_director.web.controller.dto.DtoMapper;
import de.jensknipper.re_director.web.controller.dto.RedirectResponse;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class ViewController {

  private final RedirectService redirectService;
  private final ValidationService validationService;
  private final DtoMapper dtoMapper;

  public ViewController(
      RedirectService redirectService, ValidationService validationService, DtoMapper dtoMapper) {
    this.redirectService = redirectService;
    this.validationService = validationService;
    this.dtoMapper = dtoMapper;
  }

  @GetMapping("/")
  public String home(Model model) {
    return "home";
  }

  @GetMapping("/redirects")
  public String redirects(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer code,
      Model model) {
    model.addAttribute("redirects", getAllRedirectsFiltered(search, status, code));
    model.addAttribute("createRedirectRequest", new CreateRedirectRequest());
    model.addAttribute("search", search);
    model.addAttribute("status", status);
    model.addAttribute("code", code);
    return "redirects";
  }

  @GetMapping("/redirects/create")
  public String redirectsCreateModal(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer code,
      Model model) {
    model.addAttribute("isCreatePage", true);
    return redirects(search, status, code, model);
  }

  @PostMapping("/redirects/create")
  public String createRedirect(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer code,
      @Valid CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    validationService.uniqueSource(bindingResult, createRedirectRequest.getSource());
    if (bindingResult.hasErrors()) {
      model.addAttribute("createRedirectRequest", createRedirectRequest);
      model.addAttribute("redirects", getAllRedirectsFiltered(search, status, code));
      model.addAttribute("search", search);
      model.addAttribute("status", status);
      model.addAttribute("code", code);
      model.addAttribute("isCreatePage", true);
      model.addAttribute("bindingResult", bindingResult);
      return "redirects";
    }
    redirectService.create(
        createRedirectRequest.getSource(),
        createRedirectRequest.getTarget(),
        getHttpStatusCode(createRedirectRequest));
    return "redirect:/redirects" + getParams(search, status, code);
  }

  @GetMapping("/redirects/edit/{id}")
  public String redirectsEditModal(
      @PathVariable int id,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer code,
      Model model) {
    model.addAttribute("editPageId", id);
    return redirects(search, status, code, model);
  }

  @PostMapping("/redirects/edit/{id}")
  public String updateRedirect(
      @PathVariable int id,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer code,
      @Valid CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    validationService.uniqueSource(bindingResult, createRedirectRequest.getSource(), id);
    if (bindingResult.hasErrors()) {
      model.addAttribute("createRedirectRequest", createRedirectRequest);
      model.addAttribute("redirects", getAllRedirectsFiltered(search, status, code));
      model.addAttribute("search", search);
      model.addAttribute("status", status);
      model.addAttribute("code", code);
      model.addAttribute("bindingResult", bindingResult);
      model.addAttribute("editPageId", id);
      return "redirects";
    }
    redirectService.update(
        id,
        createRedirectRequest.getSource(),
        createRedirectRequest.getTarget(),
        getHttpStatusCode(createRedirectRequest));
    return "redirect:/redirects" + getParams(search, status, code);
  }

  @PostMapping("/redirects/{id}/status/{newStatus}")
  public String setStatus(
      @PathVariable int id,
      @PathVariable Status newStatus,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer code,
      Model model) {
    redirectService.updateStatus(id, newStatus);
    return "redirect:/redirects" + getParams(search, status, code);
  }

  @PostMapping("/redirects/{id}/delete")
  public String deleteRedirect(
      @PathVariable int id,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer code,
      Model model) {
    redirectService.delete(id);
    return "redirect:/redirects" + getParams(search, status, code);
  }

  private List<RedirectResponse> getAllRedirectsFiltered(
      String search, String status, Integer httpStatusCode) {
    final Status statusFilter =
        Arrays.stream(Status.values())
            .filter(it -> it.name().equals(status))
            .findFirst()
            .orElse(null);
    final RedirectHttpStatusCode httpStatusCodeFilter =
        Arrays.stream(RedirectHttpStatusCode.values())
            .filter(it -> httpStatusCode != null && it.getCode() == httpStatusCode)
            .findFirst()
            .orElse(null);
    return redirectService.findAllFiltered(search, statusFilter, httpStatusCodeFilter).stream()
        .map(dtoMapper::toRedirectResponse)
        .collect(Collectors.toList()); // thymeleaf needs modifiable list here
  }

  private RedirectHttpStatusCode getHttpStatusCode(CreateRedirectRequest createRedirectRequest) {
    return RedirectHttpStatusCode.findByCode(createRedirectRequest.getHttpStatusCode())
        // if this happens something with validation has gone wrong
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Could not match redirect status code '"
                        + createRedirectRequest.getHttpStatusCode()
                        + "' to any of the allowed values: '"
                        + Arrays.toString(RedirectHttpStatusCode.values())
                        + "'"));
  }

  private String getParams(String search, String status, Integer code) {
    return buildParams(
            new UrlParam("search", search),
            new UrlParam("status", status),
            new UrlParam("code", code))
        .orElse("");
  }

  private record UrlParam(String name, @Nullable String value) {
    public UrlParam(String name, Object value) {
      this(name, Optional.ofNullable(value).map(Object::toString).orElse(null));
    }

    public String getString() {
      return name + "=" + value;
    }
  }

  private Optional<String> buildParams(UrlParam... params) {
    String paramsString =
        Arrays.stream(params)
            .filter(it -> it.value() != null && !it.value().isBlank())
            .map(UrlParam::getString)
            .collect(Collectors.joining("&"));
    if (paramsString.isBlank()) {
      return Optional.empty();
    }
    return Optional.of("?" + paramsString);
  }
}
