package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    public MedicamentoService(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional
    public Medicamento createMedicamento(String nome, String codigoInterno) {
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

    public Page<Medicamento> getAllMedicamentos(Pageable pageable) {
        return medicamentoRepository.findAll(pageable);
    }

    public Medicamento getMedicamentoById(UUID medicamentoId) {
        return medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", medicamentoId));
    }

    public Medicamento getMedicamentoByCodigoInterno(String codigoInterno) {
        return medicamentoRepository.findByCodigoInterno(codigoInterno)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", codigoInterno));
    }

    @Transactional
    public Medicamento updateMedicamento(UUID medicamentoId, String nome, String codigoInterno) {
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

    @Transactional
    public boolean deleteMedicamento(UUID medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new RecursoNaoEncontradoException("Medicamento", medicamentoId);
        }
        medicamentoRepository.deleteById(medicamentoId);
        return true;
    }
}
