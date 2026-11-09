package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.protocolo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPaciente;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPacienteRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultas de protocolo (somente leitura).
 */
@Service
public class BuscarProtocoloUseCase {

    private final ProtocoloPacienteRepository protocoloRepository;

    public BuscarProtocoloUseCase(ProtocoloPacienteRepository protocoloRepository) {
        this.protocoloRepository = protocoloRepository;
    }

    /** medicamentoId opcional: null lista tudo. */
    @Transactional(readOnly = true)
    public Page<ProtocoloPaciente> listar(UUID medicamentoId, Pageable pageable) {
        return medicamentoId == null
                ? protocoloRepository.findAll(pageable)
                : protocoloRepository.findDistinctByMedicamentosMedicamentoId(medicamentoId, pageable);
    }

    @Transactional(readOnly = true)
    public ProtocoloPaciente porId(UUID protocoloId) {
        return protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("ProtocoloPaciente", protocoloId));
    }
}
