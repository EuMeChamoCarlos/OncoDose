package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.auth;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.auth.dto.AuthRequestDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.auth.dto.TokenResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user.AutenticarUsuarioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Emissão de token: não é CRUD do recurso usuário, por isso fica fora de /api/users. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthRequestDTO authRequest) {
        return ResponseEntity.ok(new TokenResponse(
                autenticarUsuarioUseCase.executar(authRequest.username(), authRequest.password())));
    }
}
