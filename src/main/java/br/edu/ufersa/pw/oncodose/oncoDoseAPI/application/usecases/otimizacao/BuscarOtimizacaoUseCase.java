package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.Otimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.OtimizacaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultar uma otimização já calculada (somente leitura).
 */
@Service
public class BuscarOtimizacaoUseCase {

    private final OtimizacaoRepository otimizacaoRepository;

    public BuscarOtimizacaoUseCase(OtimizacaoRepository otimizacaoRepository) {
        this.otimizacaoRepository = otimizacaoRepository;
    }

    @Transactional(readOnly = true)
    public Otimizacao porId(UUID otimizacaoId) {
        return otimizacaoRepository.findById(otimizacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Otimização", otimizacaoId));
    }
}
