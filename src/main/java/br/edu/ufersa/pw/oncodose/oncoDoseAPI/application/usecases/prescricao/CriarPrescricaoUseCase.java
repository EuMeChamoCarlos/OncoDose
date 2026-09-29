package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto.PrescricaoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar prescrição. Código duplicado → 409, medicamento inexistente → 404.
 */
@Service
public class CriarPrescricaoUseCase {

    private final PrescricaoRepository prescricaoRepository;
    private final MedicamentoRepository medicamentoRepository;

    public CriarPrescricaoUseCase(
            PrescricaoRepository prescricaoRepository,
            MedicamentoRepository medicamentoRepository) {
        this.prescricaoRepository = prescricaoRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional
    public Prescricao executar(PrescricaoRequest request) {
        if (prescricaoRepository.existsByCodigoPrescricao(request.codigoPrescricao())) {
            throw new RecursoDuplicadoException("Prescrição", "codigoPrescricao", request.codigoPrescricao());
        }
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", request.medicamentoId()));

        return prescricaoRepository.save(Prescricao.nova(
                request.codigoPrescricao(), medicamento,
                request.doseMg(), request.status(), request.dataPrescricao()));
    }
}
