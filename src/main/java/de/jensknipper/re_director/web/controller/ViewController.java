package de.jensknipper.re_director.web.controller;

import de.jensknipper.re_director.db.RedirectRepository;
import de.jensknipper.re_director.db.Status;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class ViewController {

  private final RedirectRepository redirectRepository;

  public ViewController(RedirectRepository redirectRepository) {
    this.redirectRepository = redirectRepository;
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
    model.addAttribute("redirects", redirectRepository.findAllFiltered(search, status));
    model.addAttribute("redirectDto", new RedirectDto());
    return "redirects";
  }

  @PostMapping("/redirects")
  public String createRedirect(
      @ModelAttribute RedirectDto redirectDto, BindingResult bindingResult, Model model) {
    redirectRepository.create(redirectDto.getSource(), redirectDto.getTarget());
    return "redirect:/redirects";
  }

  @PostMapping("/redirects/{id}")
  public String updateRedirect(
      @PathVariable int id,
      @ModelAttribute RedirectDto redirectDto,
      BindingResult bindingResult,
      Model model) {
    redirectRepository.update(id, redirectDto.getSource(), redirectDto.getTarget());
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
