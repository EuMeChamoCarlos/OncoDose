package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.MedicamentoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exclusão de medicamento. Recusa se houver prescrições (FK RESTRICT, V5).
 */
@Service
public class DeletarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final PrescricaoRepository prescricaoRepository;

    public DeletarMedicamentoUseCase(
            MedicamentoRepository medicamentoRepository,
            PrescricaoRepository prescricaoRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.prescricaoRepository = prescricaoRepository;
    }

    @Transactional
    public void executar(UUID medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new RecursoNaoEncontradoException("Medicamento", medicamentoId);
        }
        if (prescricaoRepository.existsByMedicamentoId(medicamentoId)) {
            throw new MedicamentoInvalidoException("prescricoes", "possui prescrições vinculadas");
        }
        medicamentoRepository.deleteById(medicamentoId);
    }
}
