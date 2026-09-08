package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.UserStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserDTO {
    private String name;
    private String email;
    private String document;
    private String phone;
    private String password;
    private UserStatus status = null;
    private LocalDate birthDate = null;
}