package br.edu.ufersa.pw.oncotimize.application.usecases.prescricao;

import br.edu.ufersa.pw.oncotimize.application.api.prescricao.dto.PrescricaoRequest;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.PrescricaoRepository;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.AplicacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar uma linha de prescrição. O mesmo código pode repetir em outro medicamento.
 * Código e medicamento duplicados, ou medicamento já prescrito na aplicação → 409,
 * medicamento/aplicação inexistente → 404, aplicação sem esse medicamento ou encerrada → 400.
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
        if (prescricaoRepository.existsByCodigoPrescricaoAndMedicamentoId(request.codigoPrescricao(), request.medicamentoId())) {
            throw new RecursoDuplicadoException("Prescrição", "codigoPrescricao e medicamentoId", request.codigoPrescricao());
        }
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", request.medicamentoId()));

        Aplicacao aplicacao = null;
        if (request.aplicacaoId() != null) {
            if (prescricaoRepository.existsByAplicacaoIdAndMedicamentoIdAndDataPrescricao(
                    request.aplicacaoId(), request.medicamentoId(), request.dataPrescricao())) {
                throw new RecursoDuplicadoException("Prescrição", "aplicacaoId, medicamentoId e dataPrescricao",
                        request.aplicacaoId().toString());
            }
            aplicacao = aplicacaoRepository.findById(request.aplicacaoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação", request.aplicacaoId()));
        }

        Prescricao nova = Prescricao.nova(
                request.codigoPrescricao(), medicamento,
                request.doseMg(), request.status(), request.dataPrescricao(), aplicacao);
        prescricaoRepository.findByCodigoPrescricao(request.codigoPrescricao())
                .forEach(nova::exigirMesmaPrescricaoQue);
        return prescricaoRepository.save(nova);
    }
}
