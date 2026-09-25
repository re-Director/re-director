package de.jensknipper.redirector.redirects.manage;

import static de.jensknipper.redirector.redirects.manage.PageContext.*;

import de.jensknipper.redirector.common.db.RedirectHttpStatusCode;
import de.jensknipper.redirector.common.db.Status;
import de.jensknipper.redirector.common.validation.ValidationService;
import de.jensknipper.redirector.redirects.manage.dto.CreateRedirectRequest;
import de.jensknipper.redirector.redirects.manage.dto.DtoMapper;
import de.jensknipper.redirector.redirects.manage.dto.RedirectResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Locale;
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

  private static final String PAGE_REDIRECTS = "redirects";

  private static final String ATTRIBUTE_REDIRECTS_CODE = "code";
  private static final String ATTRIBUTE_REDIRECTS_DIRECTION = "direction";
  private static final String ATTRIBUTE_REDIRECTS_CREATE_REDIRECT_REQUEST = "createRedirectRequest";
  private static final String ATTRIBUTE_REDIRECTS_PAGE_CONTEXT = "pageContext";
  private static final String ATTRIBUTE_REDIRECTS_REDIRECTS = "redirects";
  private static final String ATTRIBUTE_REDIRECTS_SEARCH = "search";
  private static final String ATTRIBUTE_REDIRECTS_SORT = "sort";
  private static final String ATTRIBUTE_REDIRECTS_STATUS = "status";

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
        ATTRIBUTE_REDIRECTS_REDIRECTS,
        getAllRedirectsFiltered(
            search, status, code, createPageable(page, size, normalizedSort, normalizedDirection)));
    model.addAttribute(ATTRIBUTE_REDIRECTS_CREATE_REDIRECT_REQUEST, CreateRedirectRequest.empty());

    model.addAttribute(ATTRIBUTE_REDIRECTS_SEARCH, search);
    model.addAttribute(ATTRIBUTE_REDIRECTS_STATUS, status);
    model.addAttribute(ATTRIBUTE_REDIRECTS_CODE, code);
    model.addAttribute(ATTRIBUTE_REDIRECTS_SORT, normalizedSort);
    model.addAttribute(ATTRIBUTE_REDIRECTS_DIRECTION, normalizedDirection);
    model.addAttribute(
        ATTRIBUTE_REDIRECTS_PAGE_CONTEXT,
        new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size));
    return PAGE_REDIRECTS;
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
    validationService.uniqueSource(bindingResult, createRedirectRequest.source());
    String normalizedSort = normalizeSort(sort);
    String normalizedDirection = normalizeDirection(direction);
    PageContext pageContext =
        new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size);
    if (bindingResult.hasErrors()) {
      PageRequest pageable = createPageable(page, size, normalizedSort, normalizedDirection);
      model.addAttribute(
          ATTRIBUTE_REDIRECTS_REDIRECTS, getAllRedirectsFiltered(search, status, code, pageable));
      model.addAttribute(ATTRIBUTE_REDIRECTS_CREATE_REDIRECT_REQUEST, createRedirectRequest);

      model.addAttribute(ATTRIBUTE_REDIRECTS_SEARCH, search);
      model.addAttribute(ATTRIBUTE_REDIRECTS_STATUS, status);
      model.addAttribute(ATTRIBUTE_REDIRECTS_CODE, code);
      model.addAttribute(ATTRIBUTE_REDIRECTS_SORT, normalizedSort);
      model.addAttribute(ATTRIBUTE_REDIRECTS_DIRECTION, normalizedDirection);
      model.addAttribute(ATTRIBUTE_REDIRECTS_PAGE_CONTEXT, pageContext);

      model.addAttribute("isCreatePage", true);
      model.addAttribute("bindingResult", bindingResult);
      return PAGE_REDIRECTS;
    }
    manageRedirectsService.create(
        createRedirectRequest.source(),
        createRedirectRequest.target(),
        createRedirectRequest.pathForwarding(),
        createRedirectRequest.queryForwarding(),
        createRedirectRequest.httpStatusCode());
    return getRedirectToRedirectsPage(pageContext);
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
    validationService.uniqueSource(bindingResult, editRedirectRequest.source(), id);
    String normalizedSort = normalizeSort(sort);
    String normalizedDirection = normalizeDirection(direction);
    PageContext pageContext =
        new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size);
    if (bindingResult.hasErrors()) {
      PageRequest pageable = createPageable(page, size, normalizedSort, normalizedDirection);
      model.addAttribute(
          ATTRIBUTE_REDIRECTS_REDIRECTS, getAllRedirectsFiltered(search, status, code, pageable));
      model.addAttribute(
          ATTRIBUTE_REDIRECTS_CREATE_REDIRECT_REQUEST, CreateRedirectRequest.empty());

      model.addAttribute(ATTRIBUTE_REDIRECTS_SEARCH, search);
      model.addAttribute(ATTRIBUTE_REDIRECTS_STATUS, status);
      model.addAttribute(ATTRIBUTE_REDIRECTS_CODE, code);
      model.addAttribute(ATTRIBUTE_REDIRECTS_SORT, normalizedSort);
      model.addAttribute(ATTRIBUTE_REDIRECTS_DIRECTION, normalizedDirection);
      model.addAttribute(ATTRIBUTE_REDIRECTS_PAGE_CONTEXT, pageContext);

      model.addAttribute("bindingResult", bindingResult);
      model.addAttribute("editPageId", id);
      model.addAttribute("editRedirectRequest", editRedirectRequest);
      return PAGE_REDIRECTS;
    }
    manageRedirectsService.update(
        id,
        editRedirectRequest.source(),
        editRedirectRequest.target(),
        editRedirectRequest.pathForwarding(),
        editRedirectRequest.queryForwarding(),
        editRedirectRequest.httpStatusCode());
    return getRedirectToRedirectsPage(pageContext);
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
    return getRedirectToRedirectsPage(
        new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size));
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
    return getRedirectToRedirectsPage(
        new PageContext(search, status, code, normalizedSort, normalizedDirection, page, size));
  }

  private static String getRedirectToRedirectsPage(PageContext pageContext) {
    return "redirect:/redirects" + pageContext.toParams();
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

  private Sort.Direction parseDirection(String direction) {
    try {
      return Sort.Direction.valueOf(direction.toUpperCase(Locale.ROOT));
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
