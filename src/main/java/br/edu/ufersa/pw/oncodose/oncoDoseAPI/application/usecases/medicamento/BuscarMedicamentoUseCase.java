package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultas de medicamento (somente leitura).
 */
@Service
public class BuscarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;

    public BuscarMedicamentoUseCase(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Medicamento> listar(Pageable pageable) {
        return medicamentoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Medicamento porId(UUID medicamentoId) {
        return medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", medicamentoId));
    }

    @Transactional(readOnly = true)
    public Medicamento porCodigo(String codigoInterno) {
        return medicamentoRepository.findByCodigoInterno(codigoInterno)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", codigoInterno));
    }
}
