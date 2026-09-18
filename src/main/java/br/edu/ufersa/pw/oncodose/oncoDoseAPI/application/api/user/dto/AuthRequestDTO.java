package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(@NotBlank String username, @NotBlank String password) {
}
