package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.User;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultas e exclusão de usuário.
 */
@Service
public class GerenciarUsuarioUseCase {

    private final UserRepository userRepository;

    public GerenciarUsuarioUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User porId(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", userId));
    }

    @Transactional
    public void excluir(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RecursoNaoEncontradoException("Usuário", userId);
        }
        userRepository.deleteById(userId);
    }
}
