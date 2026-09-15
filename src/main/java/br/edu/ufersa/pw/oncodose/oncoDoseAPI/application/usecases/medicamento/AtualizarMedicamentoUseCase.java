package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: atualização parcial de medicamento.
 */
@Service
public class AtualizarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;

    public AtualizarMedicamentoUseCase(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional
    public Medicamento executar(UUID medicamentoId, String nome, String codigoInterno) {
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", medicamentoId));

        if (nome != null && !nome.equals(medicamento.getNome())) {
            if (medicamentoRepository.existsByNome(nome)) {
                throw new RecursoDuplicadoException("Medicamento", "nome", nome);
            }
            medicamento.setNome(nome);
        }

        if (codigoInterno != null && !codigoInterno.equals(medicamento.getCodigoInterno())) {
            if (medicamentoRepository.existsByCodigoInterno(codigoInterno)) {
                throw new RecursoDuplicadoException("Medicamento", "codigoInterno", codigoInterno);
            }
            medicamento.setCodigoInterno(codigoInterno);
        }

        return medicamentoRepository.save(medicamento);
    }
}
