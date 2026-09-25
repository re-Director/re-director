package de.jensknipper.redirector.auth;

import jakarta.validation.constraints.NotBlank;

public record SetupFormDto(
    @NotBlank String username, @NotBlank String password, @NotBlank String confirmPassword) {

  public static SetupFormDto empty() {
    return new SetupFormDto("", "", "");
  }
}
