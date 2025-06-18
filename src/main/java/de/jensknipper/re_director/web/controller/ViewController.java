package de.jensknipper.re_director.web.controller;

import de.jensknipper.re_director.db.RedirectRepository;
import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.Status;
import de.jensknipper.re_director.web.controller.dto.CreateRedirectRequest;
import de.jensknipper.re_director.web.controller.dto.DtoMapper;
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
      @RequestParam(required = false) Status status,
      Model model) {
    model.addAttribute(
        "redirects",
        redirectRepository.findAllFiltered(search, status).stream()
            .map(dtoMapper::toRedirectResponse)
            .collect(Collectors.toList())); // thymeleaf needs modifiable list here
    model.addAttribute("createRedirectRequest", new CreateRedirectRequest());
    return "redirects";
  }

  @PostMapping("/redirects")
  public String createRedirect(
      @ModelAttribute CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    Optional<RedirectHttpStatusCode> statusCode =
        RedirectHttpStatusCode.findByCode(createRedirectRequest.getHttpStatusCode());
    redirectRepository.create(
        createRedirectRequest.getSource(),
        createRedirectRequest.getTarget(),
        statusCode.orElse(RedirectHttpStatusCode.FOUND));
    return "redirect:/redirects";
  }

  @PostMapping("/redirects/{id}")
  public String updateRedirect(
      @PathVariable int id,
      @ModelAttribute CreateRedirectRequest createRedirectRequest,
      BindingResult bindingResult,
      Model model) {
    Optional<RedirectHttpStatusCode> statusCode =
        RedirectHttpStatusCode.findByCode(createRedirectRequest.getHttpStatusCode());
    redirectRepository.update(
        id,
        createRedirectRequest.getSource(),
        createRedirectRequest.getTarget(),
        statusCode.orElse(RedirectHttpStatusCode.FOUND));
    return "redirect:/redirects";
  }

  @PostMapping("/redirects/{id}/status/{status}")
  public String setStatus(@PathVariable int id, @PathVariable Status status, Model model) {
    redirectRepository.updateStatus(id, status);
    return "redirect:/redirects";
  }

  @PostMapping("/redirects/{id}/delete")
  public String deleteRedirect(@PathVariable int id, Model model) {
    redirectRepository.delete(id);
    return "redirect:/redirects";
  }
}
