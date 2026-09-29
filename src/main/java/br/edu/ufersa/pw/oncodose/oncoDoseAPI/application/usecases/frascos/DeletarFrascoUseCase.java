package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.FrascoIndisponivelException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.AlocacaoFrascoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: excluir frasco. Recusa se ele já foi usado numa otimização (FK RESTRICT).
 */
@Service
public class DeletarFrascoUseCase {

    private final FrascosRepository frascosRepository;
    private final AlocacaoFrascoRepository alocacaoFrascoRepository;

    public DeletarFrascoUseCase(
            FrascosRepository frascosRepository,
            AlocacaoFrascoRepository alocacaoFrascoRepository) {
        this.frascosRepository = frascosRepository;
        this.alocacaoFrascoRepository = alocacaoFrascoRepository;
    }

    @Transactional
    public void executar(UUID medicamentoId, UUID frascoId) {
        Frascos frasco = frascosRepository.findByIdAndMedicamentoId(frascoId, medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Frasco", frascoId));
        if (alocacaoFrascoRepository.existsByApresentacaoId(frascoId)) {
            throw new FrascoIndisponivelException(frascoId, "já usado em otimização");
        }
        frascosRepository.delete(frasco);
    }
}
