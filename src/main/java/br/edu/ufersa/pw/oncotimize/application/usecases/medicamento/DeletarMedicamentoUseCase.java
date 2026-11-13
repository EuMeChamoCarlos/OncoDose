package br.edu.ufersa.pw.oncotimize.application.usecases.medicamento;

import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPacienteRepository;
import br.edu.ufersa.pw.oncotimize.domain.exception.MedicamentoInvalidoException;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exclusão de medicamento. Recusa se houver prescrições (FK RESTRICT, V5) ou protocolos (V7).
 */
@Service
public class DeletarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final ProtocoloPacienteRepository protocoloRepository;

    public DeletarMedicamentoUseCase(
            MedicamentoRepository medicamentoRepository,
            PrescricaoRepository prescricaoRepository,
            ProtocoloPacienteRepository protocoloRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.protocoloRepository = protocoloRepository;
    }

    @Transactional
    public void executar(UUID medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new RecursoNaoEncontradoException("Medicamento", medicamentoId);
        }
        if (prescricaoRepository.existsByMedicamentoId(medicamentoId)) {
            throw new MedicamentoInvalidoException("prescricoes", "possui prescrições vinculadas");
        }
        if (protocoloRepository.existsByMedicamentosMedicamentoId(medicamentoId)) {
            throw new MedicamentoInvalidoException("protocolos", "possui protocolos vinculados");
        }
        medicamentoRepository.deleteById(medicamentoId);
    }
}
