package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.CicloRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultas de ciclo (somente leitura).
 */
@Service
public class BuscarCicloUseCase {

    private final CicloRepository cicloRepository;

    public BuscarCicloUseCase(CicloRepository cicloRepository) {
        this.cicloRepository = cicloRepository;
    }

    /** medicamentoId opcional: null lista tudo. */
    @Transactional(readOnly = true)
    public Page<Ciclo> listar(UUID medicamentoId, Pageable pageable) {
        return medicamentoId == null
                ? cicloRepository.findAll(pageable)
                : cicloRepository.findByMedicamentoId(medicamentoId, pageable);
    }

    @Transactional(readOnly = true)
    public Ciclo porId(UUID cicloId) {
        return cicloRepository.findById(cicloId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclo", cicloId));
    }
}
