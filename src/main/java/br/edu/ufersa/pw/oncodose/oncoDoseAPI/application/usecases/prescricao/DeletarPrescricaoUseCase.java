package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exclusão de prescrição.
 */
@Service
public class DeletarPrescricaoUseCase {

    private final PrescricaoRepository prescricaoRepository;

    public DeletarPrescricaoUseCase(PrescricaoRepository prescricaoRepository) {
        this.prescricaoRepository = prescricaoRepository;
    }

    // ponytail: sem checagem de alocacao_frasco (FK RESTRICT vira 500); tratar quando a otimização gravar alocações
    @Transactional
    public void executar(UUID prescricaoId) {
        if (!prescricaoRepository.existsById(prescricaoId)) {
            throw new RecursoNaoEncontradoException("Prescrição", prescricaoId);
        }
        prescricaoRepository.deleteById(prescricaoId);
    }
}
