package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.User;

import java.util.UUID;

public record AuthenticatedUserResponse(UUID id, String username, String name, String role) {
    public static AuthenticatedUserResponse fromUser(User user) {
        String name = user.getProfile() != null ? user.getProfile().getName() : null;
        String role = user.getRole() != null ? user.getRole().getName() : null;

        return new AuthenticatedUserResponse(
                user.getId(),
                user.getAuth().getUsername(),
                name,
                role
        );
    }
}