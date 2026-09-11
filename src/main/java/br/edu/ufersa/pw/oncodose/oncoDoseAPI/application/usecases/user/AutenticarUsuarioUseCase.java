package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.CredenciaisInvalidasException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.jwt.JwtGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: autenticar usuário e emitir JWT. Sem escrita no banco.
 */
@Service
public class AutenticarUsuarioUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;

    public AutenticarUsuarioUseCase(
            AuthenticationManager authenticationManager, JwtGenerator jwtGenerator) {
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
    }

    public String executar(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            return jwtGenerator.generateToken(authentication);
        } catch (AuthenticationException e) {
            throw new CredenciaisInvalidasException();
        }
    }
}
