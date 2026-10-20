package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.CicloRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.CicloInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exclusão de ciclo (aplicações vão junto). Ciclo errado se exclui e recria, como a
 * prescrição; recusa se alguma aplicação já foi realizada, cancelada ou prescrita (histórico do TCC).
 */
@Service
public class DeletarCicloUseCase {

    private final CicloRepository cicloRepository;
    private final PrescricaoRepository prescricaoRepository;

    public DeletarCicloUseCase(CicloRepository cicloRepository, PrescricaoRepository prescricaoRepository) {
        this.cicloRepository = cicloRepository;
        this.prescricaoRepository = prescricaoRepository;
    }

    @Transactional
    public void executar(UUID cicloId) {
        Ciclo ciclo = cicloRepository.findById(cicloId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclo", cicloId));
        if (ciclo.possuiAplicacaoEncerrada()) {
            throw new CicloInvalidoException(ciclo.getCodigoCiclo(), "possui aplicação realizada ou cancelada");
        }
        if (prescricaoRepository.existsByAplicacaoCicloId(cicloId)) {
            throw new CicloInvalidoException(ciclo.getCodigoCiclo(), "possui prescrição vinculada a uma aplicação");
        }
        cicloRepository.delete(ciclo);
    }
}
