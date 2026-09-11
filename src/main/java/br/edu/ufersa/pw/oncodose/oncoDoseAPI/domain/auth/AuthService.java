package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.auth;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<Auth> createAuth(String username, String rawPassword) {
        if (authRepository.existsByUsername(username)) {
            throw new RecursoDuplicadoException("Auth", "username", username);
        }

        Auth auth = new Auth();
        auth.setUsername(username);
        auth.setPassword(passwordEncoder.encode(rawPassword)); // Codifica corretamente a senha

        return new ResponseEntity<>(authRepository.save(auth), HttpStatus.CREATED);
    }

    // Atualizar a senha de um usuário
    public ResponseEntity<String> updatePassword(UUID authId, String rawPassword) {
        return authRepository.findById(authId).map(auth -> {
            auth.setPassword(passwordEncoder.encode(rawPassword)); // Codifica corretamente a senha
            authRepository.save(auth);
            return ResponseEntity.ok("Senha atualizada.");
        }).orElseThrow(() -> new RecursoNaoEncontradoException("Credenciais", authId));
    }

    // Atualizar o nome de usuário
    public ResponseEntity<String> updateUsername(UUID authId, String newUsername) {
        return authRepository.findById(authId).map(auth -> {
            if (authRepository.existsByUsername(newUsername)
                    && !authRepository.findByUsername(newUsername).get().getId().equals(authId)) {
                throw new RecursoDuplicadoException("Auth", "username", newUsername);
            }
            auth.setUsername(newUsername);
            authRepository.save(auth);
            return ResponseEntity.ok("Nome de usuário atualizado.");
        }).orElseThrow(() -> new RecursoNaoEncontradoException("Credenciais", authId));
    }

    // Buscar credenciais por nome de usuário
    public ResponseEntity<Auth> getByUsername(String username) {
        return authRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", username));
    }

    public ResponseEntity<Auth> getById(UUID id) {
        return authRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Credenciais", id));
    }

    public ResponseEntity<Void> deleteById(UUID id) {
        if (!authRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Credenciais", id);
        }
        authRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public boolean usernameExists(String username) {
        return authRepository.existsByUsername(username);
    }
}
