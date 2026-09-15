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
        if (medicamentoRepository.existsByNome(nome)) {
            throw new RecursoDuplicadoException("Medicamento", "nome", nome);
        }
        if (codigoInterno != null && medicamentoRepository.existsByCodigoInterno(codigoInterno)) {
            throw new RecursoDuplicadoException("Medicamento", "codigoInterno", codigoInterno);
        }

        Medicamento medicamento = new Medicamento();
        medicamento.setNome(nome);
        medicamento.setCodigoInterno(codigoInterno);

        return medicamentoRepository.save(medicamento);
    }
}
