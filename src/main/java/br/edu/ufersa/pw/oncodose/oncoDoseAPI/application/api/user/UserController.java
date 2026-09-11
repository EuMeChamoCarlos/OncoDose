package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.common.ResponseDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.AuthRequestDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.AuthenticatedUserResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.UserRegistrationRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user.AutenticarUsuarioUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user.GerenciarUsuarioUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user.RegistrarUsuarioUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.CustomUserDetails;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final GerenciarUsuarioUseCase gerenciarUsuarioUseCase;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest user) {
        User criado = registrarUsuarioUseCase.executar(user);
        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/user/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(new ResponseDTO<>(criado));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO authRequest) {
        ResponseDTO<?> response = new ResponseDTO<>(
                autenticarUsuarioUseCase.executar(authRequest.username(), authRequest.password()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<AuthenticatedUserResponse>> me(
            @AuthenticationPrincipal CustomUserDetails userAuthentication
    ) {
        return ResponseEntity.ok(
                new ResponseDTO<>(
                        AuthenticatedUserResponse.fromUser(userAuthentication.getUser())
                )
        );
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
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        ResponseDTO<?> response = new ResponseDTO<>(gerenciarUsuarioUseCase.porId(userId));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        gerenciarUsuarioUseCase.excluir(userId);
        return ResponseEntity.noContent().build();
    }


}
