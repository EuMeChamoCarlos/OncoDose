package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto.JanelaRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.AplicacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: registrar a janela de flexibilidade que o médico autorizou para uma aplicação.
 */
@Service
public class DefinirJanelaAplicacaoUseCase {

    private final AplicacaoRepository aplicacaoRepository;

    public DefinirJanelaAplicacaoUseCase(AplicacaoRepository aplicacaoRepository) {
        this.aplicacaoRepository = aplicacaoRepository;
    }

    @Transactional
    public Aplicacao executar(UUID aplicacaoId, JanelaRequest request) {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação", aplicacaoId));
        aplicacao.definirJanela(request.diasAntes(), request.diasDepois());
        return aplicacao;
    }
}
