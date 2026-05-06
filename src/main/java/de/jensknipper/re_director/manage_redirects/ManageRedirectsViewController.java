package de.jensknipper.re_director.manage_redirects;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
import de.jensknipper.re_director.common.validation.ValidationService;
import de.jensknipper.re_director.manage_redirects.dto.CreateRedirectRequest;
import de.jensknipper.re_director.manage_redirects.dto.DtoMapper;
import de.jensknipper.re_director.manage_redirects.dto.RedirectResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class ManageRedirectsViewController {

  private final ManageRedirectsService manageRedirectsService;
  private final ValidationService validationService;
  private final DtoMapper dtoMapper;

  public ManageRedirectsViewController(
      ManageRedirectsService manageRedirectsService,
      ValidationService validationService,
      DtoMapper dtoMapper) {
    this.manageRedirectsService = manageRedirectsService;
    this.validationService = validationService;
    this.dtoMapper = dtoMapper;
  }

  @GetMapping("/redirects")
  public String redirects(
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      Model model) {
    model.addAttribute("redirects", getAllRedirectsFiltered(search, status, code));
    model.addAttribute("createRedirectRequest", new CreateRedirectRequest());

    model.addAttribute("search", search);
    model.addAttribute("status", status);
    model.addAttribute("code", code);
    model.addAttribute("urlParams", getParams(search, status, code));
    return "redirects";
  }

  @GetMapping("/redirects/create")
  public String redirectsCreateModal(
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      Model model) {
    model.addAttribute("isCreatePage", true);
    return redirects(search, status, code, model);
  }

  @PostMapping("/redirects/create")
  public String createRedirect(
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @Valid CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    validationService.uniqueSource(bindingResult, createRedirectRequest.getSource());
    String urlParams = getParams(search, status, code);
    if (bindingResult.hasErrors()) {
      model.addAttribute("redirects", getAllRedirectsFiltered(search, status, code));
      model.addAttribute("createRedirectRequest", createRedirectRequest);

      model.addAttribute("search", search);
      model.addAttribute("status", status);
      model.addAttribute("code", code);
      model.addAttribute("urlParams", urlParams);

      model.addAttribute("isCreatePage", true);
      model.addAttribute("bindingResult", bindingResult);
      return "redirects";
    }
    manageRedirectsService.create(
        createRedirectRequest.getSource(),
        createRedirectRequest.getTarget(),
        createRedirectRequest.isPathForwarding(),
        createRedirectRequest.isQueryForwarding(),
        getHttpStatusCode(createRedirectRequest));
    return "redirect:/redirects" + urlParams;
  }

  @GetMapping("/redirects/{id}/edit")
  public String redirectsEditModal(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      Model model) {
    model.addAttribute("editPageId", id);
    return redirects(search, status, code, model);
  }

  @PostMapping("/redirects/{id}/edit")
  public String updateRedirect(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @Valid CreateRedirectRequest editRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    validationService.uniqueSource(bindingResult, editRedirectRequest.getSource(), id);
    String urlParams = getParams(search, status, code);
    if (bindingResult.hasErrors()) {
      model.addAttribute("redirects", getAllRedirectsFiltered(search, status, code));
      model.addAttribute("createRedirectRequest", new CreateRedirectRequest());

      model.addAttribute("search", search);
      model.addAttribute("status", status);
      model.addAttribute("code", code);
      model.addAttribute("urlParams", urlParams);

      model.addAttribute("bindingResult", bindingResult);
      model.addAttribute("editPageId", id);
      model.addAttribute("editRedirectRequest", editRedirectRequest);
      return "redirects";
    }
    manageRedirectsService.update(
        id,
        editRedirectRequest.getSource(),
        editRedirectRequest.getTarget(),
        editRedirectRequest.isPathForwarding(),
        editRedirectRequest.isQueryForwarding(),
        getHttpStatusCode(editRedirectRequest));
    return "redirect:/redirects" + getParams(search, status, code);
  }

  @PostMapping("/redirects/{id}/status/{newStatus}")
  public String setStatus(
      @PathVariable int id,
      @PathVariable Status newStatus,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code) {
    manageRedirectsService.updateStatus(id, newStatus);
    return "redirect:/redirects" + getParams(search, status, code);
  }

  @PostMapping("/redirects/{id}/delete")
  public String deleteRedirect(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code) {
    manageRedirectsService.delete(id);
    return "redirect:/redirects" + getParams(search, status, code);
  }

  private List<RedirectResponse> getAllRedirectsFiltered(
      @Nullable String search, @Nullable String status, @Nullable Integer httpStatusCode) {
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
    return manageRedirectsService
        .findAllFiltered(search, statusFilter, httpStatusCodeFilter)
        .stream()
        .map(dtoMapper::toRedirectResponse)
        .toList();
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

  private String getParams(
      @Nullable String search, @Nullable String status, @Nullable Integer code) {
    List<UrlParam> urlParams =
        List.of(
            new UrlParam("search", search),
            new UrlParam("status", status),
            new UrlParam("code", code));
    return getParams(urlParams);
  }

  private String getParams(List<UrlParam> urlParams) {
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    urlParams.stream()
        .filter(it -> it.value() != null && !it.value().isBlank())
        .forEach(it -> uriComponentsBuilder.queryParam(it.name(), it.value()));
    return uriComponentsBuilder.build().encode().toUriString();
  }

  private record UrlParam(String name, @Nullable String value) {
    public UrlParam(String name, @Nullable Object value) {
      this(name, Optional.ofNullable(value).map(Object::toString).orElse(null));
    }
  }
}
