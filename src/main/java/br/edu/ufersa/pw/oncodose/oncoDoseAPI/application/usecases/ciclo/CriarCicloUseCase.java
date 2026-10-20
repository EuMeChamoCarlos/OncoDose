package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto.CicloRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.CicloRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar ciclo e projetar suas aplicações. Código duplicado → 409, medicamento inexistente → 404.
 */
@Service
public class CriarCicloUseCase {

    private final CicloRepository cicloRepository;
    private final MedicamentoRepository medicamentoRepository;

    public CriarCicloUseCase(CicloRepository cicloRepository, MedicamentoRepository medicamentoRepository) {
        this.cicloRepository = cicloRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional
    public Ciclo executar(CicloRequest request) {
        if (cicloRepository.existsByCodigoCiclo(request.codigoCiclo())) {
            throw new RecursoDuplicadoException("Ciclo", "codigoCiclo", request.codigoCiclo());
        }
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", request.medicamentoId()));

        return cicloRepository.save(Ciclo.novo(
                request.codigoCiclo(), medicamento, request.doseMg(), request.dataInicio(),
                request.totalAplicacoes(), request.intervaloDias(), request.intervaloMinimoDias(),
                Objects.requireNonNullElse(request.diasAntes(), 0),
                Objects.requireNonNullElse(request.diasDepois(), 0)));
    }
}
