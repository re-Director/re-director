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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class ManageRedirectsViewController {

  public static final String DEFAULT_PAGE_SIZE = "20";
  public static final String DEFAULT_PAGE = "0";
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
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      Model model) {
    model.addAttribute(
        "redirects", getAllRedirectsFiltered(search, status, code, createPageable(page, size)));
    model.addAttribute("createRedirectRequest", new CreateRedirectRequest());

    model.addAttribute("search", search);
    model.addAttribute("status", status);
    model.addAttribute("code", code);
    model.addAttribute("pageContext", PageContext.of(search, status, code, page, size));
    return "redirects";
  }

  @GetMapping("/redirects/create")
  public String redirectsCreateModal(
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      Model model) {
    model.addAttribute("isCreatePage", true);
    return redirects(search, status, code, page, size, model);
  }

  @PostMapping("/redirects/create")
  public String createRedirect(
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      @Valid CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    validationService.uniqueSource(bindingResult, createRedirectRequest.getSource());
    PageContext pageContext = PageContext.of(search, status, code, page, size);
    if (bindingResult.hasErrors()) {
      model.addAttribute(
          "redirects", getAllRedirectsFiltered(search, status, code, createPageable(page, size)));
      model.addAttribute("createRedirectRequest", createRedirectRequest);

      model.addAttribute("search", search);
      model.addAttribute("status", status);
      model.addAttribute("code", code);
      model.addAttribute("pageContext", pageContext);

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
    return "redirect:/redirects" + pageContext.baseParams();
  }

  @GetMapping("/redirects/{id}/edit")
  public String redirectsEditModal(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      Model model) {
    model.addAttribute("editPageId", id);
    return redirects(search, status, code, page, size, model);
  }

  @PostMapping("/redirects/{id}/edit")
  public String updateRedirect(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      @Valid CreateRedirectRequest editRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    validationService.uniqueSource(bindingResult, editRedirectRequest.getSource(), id);
    PageContext pageContext = PageContext.of(search, status, code, page, size);
    if (bindingResult.hasErrors()) {
      model.addAttribute(
          "redirects", getAllRedirectsFiltered(search, status, code, createPageable(page, size)));
      model.addAttribute("createRedirectRequest", new CreateRedirectRequest());

      model.addAttribute("search", search);
      model.addAttribute("status", status);
      model.addAttribute("code", code);
      model.addAttribute("pageContext", pageContext);

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
    return "redirect:/redirects" + getParams(search, status, code, page, size);
  }

  @PostMapping("/redirects/{id}/status/{newStatus}")
  public String setStatus(
      @PathVariable int id,
      @PathVariable Status newStatus,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    manageRedirectsService.updateStatus(id, newStatus);
    return "redirect:/redirects" + getParams(search, status, code, page, size);
  }

  @GetMapping("/redirects/{id}/delete")
  public String redirectsDeleteModal(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      Model model) {
    model.addAttribute("deletePageId", id);
    return redirects(search, status, code, page, size, model);
  }

  @PostMapping("/redirects/{id}/delete")
  public String deleteRedirect(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    manageRedirectsService.delete(id);
    return "redirect:/redirects" + getParams(search, status, code, page, size);
  }

  private PageRequest createPageable(int page, int size) {
    return PageRequest.of(page, size, Sort.by("id").ascending());
  }

  private Page<RedirectResponse> getAllRedirectsFiltered(
      @Nullable String search,
      @Nullable String status,
      @Nullable Integer httpStatusCode,
      Pageable pageable) {
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
        .findAllFiltered(search, statusFilter, httpStatusCodeFilter, pageable)
        .map(dtoMapper::toRedirectResponse);
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

  public record PageContext(String baseParams, String nextUrlParams, String previousUrlParams) {
    public static PageContext of(
        @Nullable String search,
        @Nullable String status,
        @Nullable Integer code,
        int page,
        int pageSize) {
      return new PageContext(
          getParams(search, status, code, page, pageSize),
          getParams(search, status, code, page + 1, pageSize),
          getParams(search, status, code, page - 1, pageSize));
    }
  }

  private static String getParams(
      @Nullable String search,
      @Nullable String status,
      @Nullable Integer code,
      int page,
      int pageSize) {
    List<UrlParam> urlParams =
        List.of(
            new UrlParam("search", search),
            new UrlParam("status", status),
            new UrlParam("code", code),
            new UrlParam("page", page, DEFAULT_PAGE),
            new UrlParam("size", pageSize, DEFAULT_PAGE_SIZE));
    return getParams(urlParams);
  }

  private static String getParams(List<UrlParam> urlParams) {
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    urlParams.stream()
        .filter(it -> it.value() != null && !it.value().isBlank())
        .filter(it -> !it.value().equals(it.defaultValue()))
        .forEach(it -> uriComponentsBuilder.queryParam(it.name(), it.value()));
    return uriComponentsBuilder.build().encode().toUriString();
  }

  private record UrlParam(String name, @Nullable String value, @Nullable String defaultValue) {
    public UrlParam(String name, @Nullable Object value) {
      this(name, Optional.ofNullable(value).map(Object::toString).orElse(null), null);
    }

    public UrlParam(String name, @Nullable Object value, @Nullable Object defaultValue) {
      this(
          name,
          Optional.ofNullable(value).map(Object::toString).orElse(null),
          Optional.ofNullable(defaultValue).map(Object::toString).orElse(null));
    }
  }
}
