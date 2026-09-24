package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.AuthenticatedUserResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.UserRegistrationRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user.GerenciarUsuarioUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user.RegistrarUsuarioUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.CustomUserDetails;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.User;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final GerenciarUsuarioUseCase gerenciarUsuarioUseCase;

    @PostMapping
    public ResponseEntity<AuthenticatedUserResponse> register(@Valid @RequestBody UserRegistrationRequest user) {
        User criado = registrarUsuarioUseCase.executar(user);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(AuthenticatedUserResponse.fromUser(criado));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(
            @AuthenticationPrincipal CustomUserDetails userAuthentication
    ) {
        return ResponseEntity.ok(AuthenticatedUserResponse.fromUser(userAuthentication.getUser()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal CustomUserDetails userAuthentication
    ) {
        gerenciarUsuarioUseCase.excluir(userAuthentication.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<AuthenticatedUserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(AuthenticatedUserResponse.fromUser(gerenciarUsuarioUseCase.porId(userId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        gerenciarUsuarioUseCase.excluir(userId);
        return ResponseEntity.noContent().build();
    }
}
