package de.jensknipper.re_director.manage_redirects;

import static de.jensknipper.re_director.manage_redirects.PageContext.*;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
import de.jensknipper.re_director.common.validation.ValidationService;
import de.jensknipper.re_director.manage_redirects.dto.CreateRedirectRequest;
import de.jensknipper.re_director.manage_redirects.dto.DtoMapper;
import de.jensknipper.re_director.manage_redirects.dto.RedirectResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
      @RequestParam(defaultValue = DEFAULT_SORT) String sort,
      @RequestParam(defaultValue = DEFAULT_DIRECTION) String direction,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      Model model) {
    String normalizedSort = normalizeSort(sort);
    String normalizedDirection = normalizeDirection(direction);
    model.addAttribute(
        "redirects",
        getAllRedirectsFiltered(
            search, status, code, createPageable(page, size, normalizedSort, normalizedDirection)));
    model.addAttribute("createRedirectRequest", new CreateRedirectRequest());

    model.addAttribute("search", search);
    model.addAttribute("status", status);
    model.addAttribute("code", code);
    model.addAttribute("sort", normalizedSort);
    model.addAttribute("direction", normalizedDirection);
    model.addAttribute(
        "pageContext",
        new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size));
    return "redirects";
  }

  @GetMapping("/redirects/create")
  public String redirectsCreateModal(
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_SORT) String sort,
      @RequestParam(defaultValue = DEFAULT_DIRECTION) String direction,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      Model model) {
    model.addAttribute("isCreatePage", true);
    return redirects(search, status, code, sort, direction, page, size, model);
  }

  @PostMapping("/redirects/create")
  public String createRedirect(
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_SORT) String sort,
      @RequestParam(defaultValue = DEFAULT_DIRECTION) String direction,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      @Valid CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    validationService.uniqueSource(bindingResult, createRedirectRequest.getSource());
    String normalizedSort = normalizeSort(sort);
    String normalizedDirection = normalizeDirection(direction);
    PageContext pageContext =
        new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size);
    if (bindingResult.hasErrors()) {
      PageRequest pageable = createPageable(page, size, normalizedSort, normalizedDirection);
      model.addAttribute("redirects", getAllRedirectsFiltered(search, status, code, pageable));
      model.addAttribute("createRedirectRequest", createRedirectRequest);

      model.addAttribute("search", search);
      model.addAttribute("status", status);
      model.addAttribute("code", code);
      model.addAttribute("sort", normalizedSort);
      model.addAttribute("direction", normalizedDirection);
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
    return "redirect:/redirects" + pageContext.toParams();
  }

  @GetMapping("/redirects/{id}/edit")
  public String redirectsEditModal(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_SORT) String sort,
      @RequestParam(defaultValue = DEFAULT_DIRECTION) String direction,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      Model model) {
    model.addAttribute("editPageId", id);
    return redirects(search, status, code, sort, direction, page, size, model);
  }

  @PostMapping("/redirects/{id}/edit")
  public String updateRedirect(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_SORT) String sort,
      @RequestParam(defaultValue = DEFAULT_DIRECTION) String direction,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      @Valid CreateRedirectRequest editRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    validationService.uniqueSource(bindingResult, editRedirectRequest.getSource(), id);
    String normalizedSort = normalizeSort(sort);
    String normalizedDirection = normalizeDirection(direction);
    PageContext pageContext =
        new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size);
    if (bindingResult.hasErrors()) {
      PageRequest pageable = createPageable(page, size, normalizedSort, normalizedDirection);
      model.addAttribute("redirects", getAllRedirectsFiltered(search, status, code, pageable));
      model.addAttribute("createRedirectRequest", new CreateRedirectRequest());

      model.addAttribute("search", search);
      model.addAttribute("status", status);
      model.addAttribute("code", code);
      model.addAttribute("sort", normalizedSort);
      model.addAttribute("direction", normalizedDirection);
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
    return "redirect:/redirects" + pageContext.toParams();
  }

  @PostMapping("/redirects/{id}/status/{newStatus}")
  public String setStatus(
      @PathVariable int id,
      @PathVariable Status newStatus,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_SORT) String sort,
      @RequestParam(defaultValue = DEFAULT_DIRECTION) String direction,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    manageRedirectsService.updateStatus(id, newStatus);
    String normalizedSort = normalizeSort(sort);
    String normalizedDirection = normalizeDirection(direction);
    return "redirect:/redirects"
        + new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size)
            .toParams();
  }

  @GetMapping("/redirects/{id}/delete")
  public String redirectsDeleteModal(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_SORT) String sort,
      @RequestParam(defaultValue = DEFAULT_DIRECTION) String direction,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      Model model) {
    model.addAttribute("deletePageId", id);
    return redirects(search, status, code, sort, direction, page, size, model);
  }

  @PostMapping("/redirects/{id}/delete")
  public String deleteRedirect(
      @PathVariable int id,
      @Nullable @RequestParam(required = false) String search,
      @Nullable @RequestParam(required = false) String status,
      @Nullable @RequestParam(required = false) Integer code,
      @RequestParam(defaultValue = DEFAULT_SORT) String sort,
      @RequestParam(defaultValue = DEFAULT_DIRECTION) String direction,
      @RequestParam(defaultValue = DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    manageRedirectsService.delete(id);
    String normalizedSort = normalizeSort(sort);
    String normalizedDirection = normalizeDirection(direction);
    return "redirect:/redirects"
        + new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size)
            .toParams();
  }

  private PageRequest createPageable(int page, int size, String sort, String direction) {
    return PageRequest.of(page, size, Sort.by(new Sort.Order(parseDirection(direction), sort)));
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

  private Sort.Direction parseDirection(String direction) {
    try {
      return Sort.Direction.valueOf(direction.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException _) {
      return Sort.Direction.ASC;
    }
  }

  private String normalizeSort(String sort) {
    if (SORT_ALLOWED_PROPERTIES.contains(sort)) {
      return sort;
    }
    return DEFAULT_SORT;
  }

  private String normalizeDirection(String direction) {
    return parseDirection(direction).name();
  }
}
