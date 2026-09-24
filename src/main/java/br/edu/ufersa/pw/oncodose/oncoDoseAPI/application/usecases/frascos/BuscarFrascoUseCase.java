package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultas de frasco (somente leitura), sempre dentro do medicamento dono.
 */
@Service
public class BuscarFrascoUseCase {

    private final FrascosRepository frascosRepository;
    private final MedicamentoRepository medicamentoRepository;

    public BuscarFrascoUseCase(
            FrascosRepository frascosRepository,
            MedicamentoRepository medicamentoRepository) {
        this.frascosRepository = frascosRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    /** Medicamento inexistente é 404, não lista vazia. */
    @Transactional(readOnly = true)
    public Page<Frascos> listar(UUID medicamentoId, Pageable pageable) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new RecursoNaoEncontradoException("Medicamento", medicamentoId);
        }
        return frascosRepository.findByMedicamentoId(medicamentoId, pageable);
    }

    /** Frasco de outro medicamento é 404: o id da URL pai restringe a busca. */
    @Transactional(readOnly = true)
    public Frascos porId(UUID medicamentoId, UUID frascoId) {
        return frascosRepository.findByIdAndMedicamentoId(frascoId, medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Frasco", frascoId));
    }
}
