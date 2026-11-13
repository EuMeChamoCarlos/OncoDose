package br.edu.ufersa.pw.oncotimize.application.usecases.otimizacao;

import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncotimize.domain.otimizacao.Otimizacao;
import br.edu.ufersa.pw.oncotimize.domain.otimizacao.OtimizacaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: o profissional recusa o plano de frascos (não mexe no estoque nem nas aplicações).
 * Plano confirmado não pode ser recusado → 422. Recusar de novo devolve o mesmo estado.
 */
@Service
public class RecusarOtimizacaoUseCase {

    private final OtimizacaoRepository otimizacaoRepository;

    public RecusarOtimizacaoUseCase(OtimizacaoRepository otimizacaoRepository) {
        this.otimizacaoRepository = otimizacaoRepository;
    }

    @Transactional
    public Otimizacao executar(UUID otimizacaoId, String usuario, String motivo) {
        Otimizacao otimizacao = otimizacaoRepository.findWithLockById(otimizacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Otimização", otimizacaoId));
        otimizacao.recusar(usuario, motivo);
        return otimizacao;
    }
}
