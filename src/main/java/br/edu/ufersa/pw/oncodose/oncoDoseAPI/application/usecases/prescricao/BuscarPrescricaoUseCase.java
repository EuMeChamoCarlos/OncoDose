package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultas de prescrição (somente leitura).
 */
@Service
public class BuscarPrescricaoUseCase {

    private final PrescricaoRepository prescricaoRepository;

    public BuscarPrescricaoUseCase(PrescricaoRepository prescricaoRepository) {
        this.prescricaoRepository = prescricaoRepository;
    }

    /** data e medicamentoId opcionais: null lista tudo. */
    @Transactional(readOnly = true)
    public Page<Prescricao> listar(LocalDate data, UUID medicamentoId, Pageable pageable) {
        return prescricaoRepository.buscar(data, medicamentoId, pageable);
    }

    @Transactional(readOnly = true)
    public Prescricao porId(UUID prescricaoId) {
        return prescricaoRepository.findById(prescricaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prescrição", prescricaoId));
    }
}
