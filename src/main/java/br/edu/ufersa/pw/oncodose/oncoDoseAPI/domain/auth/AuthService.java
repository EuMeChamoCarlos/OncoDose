package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.auth;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
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

    public Auth createAuth(String username, String rawPassword) {
        if (authRepository.existsByUsername(username)) {
            throw new RecursoDuplicadoException("Auth", "username", username);
        }

        Auth auth = new Auth();
        auth.setUsername(username);
        auth.setPassword(passwordEncoder.encode(rawPassword)); // Codifica corretamente a senha

        return authRepository.save(auth);
    }

    // Atualizar a senha de um usuário
    public String updatePassword(UUID authId, String rawPassword) {
        Auth auth = authRepository.findById(authId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Credenciais", authId));
        auth.setPassword(passwordEncoder.encode(rawPassword)); // Codifica corretamente a senha
        authRepository.save(auth);
        return "Senha atualizada.";
    }

    // Atualizar o nome de usuário
    public String updateUsername(UUID authId, String newUsername) {
        Auth auth = authRepository.findById(authId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Credenciais", authId));
        if (authRepository.existsByUsername(newUsername)
                && !authRepository.findByUsername(newUsername).get().getId().equals(authId)) {
            throw new RecursoDuplicadoException("Auth", "username", newUsername);
        }
        auth.setUsername(newUsername);
        authRepository.save(auth);
        return "Nome de usuário atualizado.";
    }

    // Buscar credenciais por nome de usuário
    public Auth getByUsername(String username) {
        return authRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", username));
    }

    public Auth getById(UUID id) {
        return authRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Credenciais", id));
    }

    public void deleteById(UUID id) {
        if (!authRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Credenciais", id);
        }
        authRepository.deleteById(id);
    }

    public boolean usernameExists(String username) {
        return authRepository.existsByUsername(username);
    }
}
