package de.jensknipper.re_director.web.controller;

import de.jensknipper.re_director.db.RedirectRepository;
import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.Status;
import de.jensknipper.re_director.web.controller.dto.CreateRedirectRequest;
import de.jensknipper.re_director.web.controller.dto.DtoMapper;
import de.jensknipper.re_director.web.controller.dto.RedirectResponse;
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

  private final RedirectRepository redirectRepository;
  private final DtoMapper dtoMapper;

  public ViewController(RedirectRepository redirectRepository, DtoMapper dtoMapper) {
    this.redirectRepository = redirectRepository;
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
      Model model) {
      Status statusFilter = Arrays.stream(Status.values())
          .filter(it->it.name().equals(status))
          .findFirst()
          .orElse(null);
      List<RedirectResponse> redirects = redirectRepository.findAllFiltered(search, statusFilter).stream()
          .map(dtoMapper::toRedirectResponse)
          .collect(Collectors.toList());
      model.addAttribute(
        "redirects",
          redirects); // thymeleaf needs modifiable list here
    model.addAttribute("createRedirectRequest", new CreateRedirectRequest());
    return "redirects";
  }

  @PostMapping("/redirects")
  public String createRedirect(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @ModelAttribute CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    RedirectHttpStatusCode statusCode = getHttpStatusCode(createRedirectRequest);
    redirectRepository.create(
        createRedirectRequest.getSource(), createRedirectRequest.getTarget(), statusCode);
    String params =
        buildParams(new UrlParam("search", search), new UrlParam("status", status))
            .orElse("");
    return "redirect:/redirects" + params;
  }

  private record UrlParam(String name, String value) {
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
      if(paramsString.isBlank()) {
          return Optional.empty();
      }
      return Optional.of("?" + paramsString);
  }

  @PostMapping("/redirects/{id}") // TODO should be put?
  public String updateRedirect(
      @PathVariable int id,
      @ModelAttribute CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    RedirectHttpStatusCode statusCode = getHttpStatusCode(createRedirectRequest);
    redirectRepository.update(
        id, createRedirectRequest.getSource(), createRedirectRequest.getTarget(), statusCode);
    return "redirect:/redirects"; // TODO preserve filter
  }

  @PostMapping("/redirects/{id}/status/{status}")
  public String setStatus(@PathVariable int id, @PathVariable Status status, Model model) {
    redirectRepository.updateStatus(id, status);
    return "redirect:/redirects"; // TODO preserve filter
  }

  @PostMapping("/redirects/{id}/delete")
  public String deleteRedirect(@PathVariable int id, Model model) {
    redirectRepository.delete(id);
    return "redirect:/redirects"; // TODO preserve filter
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
}
