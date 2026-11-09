package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.protocolo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.protocolo.dto.ProtocoloRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPaciente;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPaciente.MedicamentoNovo;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPacienteRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar protocolo e projetar suas aplicações. Código duplicado → 409, medicamento inexistente → 404.
 */
@Service
public class CriarProtocoloUseCase {

    private final ProtocoloPacienteRepository protocoloRepository;
    private final MedicamentoRepository medicamentoRepository;

    public CriarProtocoloUseCase(ProtocoloPacienteRepository protocoloRepository, MedicamentoRepository medicamentoRepository) {
        this.protocoloRepository = protocoloRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional
    public ProtocoloPaciente executar(ProtocoloRequest request) {
        if (protocoloRepository.existsByCodigoProtocolo(request.codigoProtocolo())) {
            throw new RecursoDuplicadoException("Protocolo", "codigoProtocolo", request.codigoProtocolo());
        }
        List<MedicamentoNovo> medicamentos = request.medicamentos().stream()
                .map(item -> {
                    Medicamento medicamento = medicamentoRepository.findById(item.medicamentoId())
                            .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", item.medicamentoId()));
                    return new MedicamentoNovo(medicamento, item.doseMg(), item.via(), item.diasAplicacao());
                })
                .toList();

        return protocoloRepository.save(ProtocoloPaciente.novo(
                request.codigoProtocolo(), request.medicacaoSubtipo(), request.grupo(), request.dataInicio(),
                request.ciclos(), request.diasIntervalo(), request.intervaloMinimoDias(),
                Objects.requireNonNullElse(request.diasAntes(), 0),
                Objects.requireNonNullElse(request.diasDepois(), 0),
                medicamentos));
    }
}
