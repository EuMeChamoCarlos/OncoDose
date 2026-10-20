package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.CicloRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.CicloInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exclusão de ciclo (aplicações vão junto). Ciclo errado se exclui e recria, como a
 * prescrição; recusa se alguma aplicação já foi realizada ou cancelada (histórico do TCC).
 */
@Service
public class DeletarCicloUseCase {

    private final CicloRepository cicloRepository;

    public DeletarCicloUseCase(CicloRepository cicloRepository) {
        this.cicloRepository = cicloRepository;
    }

    @Transactional
    public void executar(UUID cicloId) {
        Ciclo ciclo = cicloRepository.findById(cicloId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclo", cicloId));
        if (ciclo.possuiAplicacaoEncerrada()) {
            throw new CicloInvalidoException(ciclo.getCodigoCiclo(), "possui aplicação realizada ou cancelada");
        }
        cicloRepository.delete(ciclo);
    }
}
