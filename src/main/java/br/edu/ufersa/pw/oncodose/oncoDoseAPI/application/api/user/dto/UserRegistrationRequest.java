package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.role.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegistrationRequest {
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String username;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;
    private String name;
    private String document;
    private String phone;
    private LocalDate birthDate = null;
    private RoleType role = RoleType.ROLE_ADMIN;
}