package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.protocolo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPaciente;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPacienteRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.ProtocoloInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exclusão de protocolo (aplicações vão junto). ProtocoloPaciente errado se exclui e recria, como a
 * prescrição; recusa se alguma aplicação já foi realizada, cancelada ou prescrita (histórico do TCC).
 */
@Service
public class DeletarProtocoloUseCase {

    private final ProtocoloPacienteRepository protocoloRepository;
    private final PrescricaoRepository prescricaoRepository;

    public DeletarProtocoloUseCase(ProtocoloPacienteRepository protocoloRepository, PrescricaoRepository prescricaoRepository) {
        this.protocoloRepository = protocoloRepository;
        this.prescricaoRepository = prescricaoRepository;
    }

    @Transactional
    public void executar(UUID protocoloId) {
        ProtocoloPaciente protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("ProtocoloPaciente", protocoloId));
        if (protocolo.possuiAplicacaoEncerrada()) {
            throw new ProtocoloInvalidoException(protocolo.getCodigoProtocolo(), "possui aplicação realizada ou cancelada");
        }
        if (prescricaoRepository.existsByAplicacaoProtocoloId(protocoloId)) {
            throw new ProtocoloInvalidoException(protocolo.getCodigoProtocolo(), "possui prescrição vinculada a uma aplicação");
        }
        protocoloRepository.delete(protocolo);
    }
}
