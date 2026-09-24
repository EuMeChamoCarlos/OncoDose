package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos.dto.FrascoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar frasco. Único dono de @Transactional na escrita.
 * Validação de faixa fica no DTO (@Positive); aqui só 404 de medicamento.
 */
@Service
public class CriarFrascoUseCase {

    private final FrascosRepository frascosRepository;
    private final MedicamentoRepository medicamentoRepository;

    public CriarFrascoUseCase(
            FrascosRepository frascosRepository,
            MedicamentoRepository medicamentoRepository) {
        this.frascosRepository = frascosRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional
    public Frascos executar(UUID medicamentoId, FrascoRequest request) {
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", medicamentoId));

        Frascos frasco = new Frascos();
        frasco.setMedicamento(medicamento);
        frasco.setVolumeMg(request.getVolumeMg());
        frasco.setCusto(request.getCusto());
        frasco.setQuantidadeEstoque(request.getQuantidadeEstoque());

        return frascosRepository.save(frasco);
    }
}
