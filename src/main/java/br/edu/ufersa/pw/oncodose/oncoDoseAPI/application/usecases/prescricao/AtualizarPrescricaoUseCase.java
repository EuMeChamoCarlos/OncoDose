package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto.AtualizarPrescricaoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: corrigir dose, status ou data de uma prescrição.
 */
@Service
public class AtualizarPrescricaoUseCase {

    private final PrescricaoRepository prescricaoRepository;

    public AtualizarPrescricaoUseCase(PrescricaoRepository prescricaoRepository) {
        this.prescricaoRepository = prescricaoRepository;
    }

    @Transactional
    public Prescricao executar(UUID prescricaoId, AtualizarPrescricaoRequest request) {
        Prescricao prescricao = prescricaoRepository.findById(prescricaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prescrição", prescricaoId));
        prescricao.atualizar(request.doseMg(), request.status(), request.dataPrescricao());
        return prescricao;
    }
}
