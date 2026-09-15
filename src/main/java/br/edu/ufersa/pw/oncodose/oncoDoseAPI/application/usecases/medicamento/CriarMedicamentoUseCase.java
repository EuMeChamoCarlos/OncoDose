package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar medicamento. Único dono de @Transactional na escrita.
 * Fail-fast com DomainException; service de domínio fica sem infra.
 */
@Service
public class CriarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;

    public CriarMedicamentoUseCase(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional
    public Medicamento executar(String nome, String codigoInterno) {
        // Builder valida invariantes (nome obrigatório) antes de existir em memória.
        Medicamento prototipo = Medicamento.builder(nome).codigoInterno(codigoInterno).build();

        if (medicamentoRepository.existsByNome(prototipo.getNome())) {
            throw new RecursoDuplicadoException("Medicamento", "nome", prototipo.getNome());
        }
        if (prototipo.getCodigoInterno() != null
                && medicamentoRepository.existsByCodigoInterno(prototipo.getCodigoInterno())) {
            throw new RecursoDuplicadoException(
                    "Medicamento", "codigoInterno", prototipo.getCodigoInterno());
        }

        return medicamentoRepository.save(prototipo);
    }
}
