package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto.PrescricaoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.AplicacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar prescrição. Código duplicado ou aplicação já prescrita → 409,
 * medicamento/aplicação inexistente → 404, aplicação de outro medicamento ou encerrada → 400.
 */
@Service
public class CriarPrescricaoUseCase {

    private final PrescricaoRepository prescricaoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AplicacaoRepository aplicacaoRepository;

    public CriarPrescricaoUseCase(
            PrescricaoRepository prescricaoRepository,
            MedicamentoRepository medicamentoRepository,
            AplicacaoRepository aplicacaoRepository) {
        this.prescricaoRepository = prescricaoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.aplicacaoRepository = aplicacaoRepository;
    }

    @Transactional
    public Prescricao executar(PrescricaoRequest request) {
        if (prescricaoRepository.existsByCodigoPrescricao(request.codigoPrescricao())) {
            throw new RecursoDuplicadoException("Prescrição", "codigoPrescricao", request.codigoPrescricao());
        }
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", request.medicamentoId()));

        Aplicacao aplicacao = null;
        if (request.aplicacaoId() != null) {
            if (prescricaoRepository.existsByAplicacaoId(request.aplicacaoId())) {
                throw new RecursoDuplicadoException("Prescrição", "aplicacaoId", request.aplicacaoId().toString());
            }
            aplicacao = aplicacaoRepository.findById(request.aplicacaoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação", request.aplicacaoId()));
        }

        return prescricaoRepository.save(Prescricao.nova(
                request.codigoPrescricao(), medicamento,
                request.doseMg(), request.status(), request.dataPrescricao(), aplicacao));
    }
}
