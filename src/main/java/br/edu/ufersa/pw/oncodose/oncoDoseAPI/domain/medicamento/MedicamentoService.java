package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Medicamento já existe com este nome!");
        }
        if (codigoInterno != null && medicamentoRepository.existsByCodigoInterno(codigoInterno)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Medicamento já existe com este código interno!");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento não encontrado!"));
    }

    public Medicamento getMedicamentoByCodigoInterno(String codigoInterno) {
        return medicamentoRepository.findByCodigoInterno(codigoInterno)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento não encontrado!"));
    }

    @Transactional
    public Medicamento updateMedicamento(UUID medicamentoId, String nome, String codigoInterno) {
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento não encontrado!"));

        if (nome != null && !nome.equals(medicamento.getNome())) {
            if (medicamentoRepository.existsByNome(nome)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Medicamento já existe com este nome!");
            }
            medicamento.setNome(nome);
        }

        if (codigoInterno != null && !codigoInterno.equals(medicamento.getCodigoInterno())) {
            if (medicamentoRepository.existsByCodigoInterno(codigoInterno)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Medicamento já existe com este código interno!");
            }
            medicamento.setCodigoInterno(codigoInterno);
        }

        return medicamentoRepository.save(medicamento);
    }

    @Transactional
    public boolean deleteMedicamento(UUID medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento não encontrado!");
        }
        medicamentoRepository.deleteById(medicamentoId);
        return true;
    }
}
