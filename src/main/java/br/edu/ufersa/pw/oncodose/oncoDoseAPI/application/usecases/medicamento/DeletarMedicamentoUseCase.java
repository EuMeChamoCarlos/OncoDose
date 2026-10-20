package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.CicloRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.MedicamentoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exclusão de medicamento. Recusa se houver prescrições (FK RESTRICT, V5) ou ciclos (V7).
 */
@Service
public class DeletarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final CicloRepository cicloRepository;

    public DeletarMedicamentoUseCase(
            MedicamentoRepository medicamentoRepository,
            PrescricaoRepository prescricaoRepository,
            CicloRepository cicloRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.cicloRepository = cicloRepository;
    }

    @Transactional
    public void executar(UUID medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new RecursoNaoEncontradoException("Medicamento", medicamentoId);
        }
        if (prescricaoRepository.existsByMedicamentoId(medicamentoId)) {
            throw new MedicamentoInvalidoException("prescricoes", "possui prescrições vinculadas");
        }
        if (cicloRepository.existsByMedicamentoId(medicamentoId)) {
            throw new MedicamentoInvalidoException("ciclos", "possui ciclos vinculados");
        }
        medicamentoRepository.deleteById(medicamentoId);
    }
}
