package br.edu.ufersa.pw.oncotimize.application.usecases.agenda;

import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacao;
import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacaoRepository;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: o profissional aceita a sugestão; a aplicação é remarcada na mesma transação (regras de
 * janela/intervalo do protocolo) e as outras sugestões pendentes da aplicação expiram.
 */
@Service
public class AceitarSugestaoUseCase {

    private final SugestaoRemarcacaoRepository sugestaoRepository;

    public AceitarSugestaoUseCase(SugestaoRemarcacaoRepository sugestaoRepository) {
        this.sugestaoRepository = sugestaoRepository;
    }

    @Transactional
    public SugestaoRemarcacao executar(UUID sugestaoId, String usuario, String motivo) {
        SugestaoRemarcacao sugestao = sugestaoRepository.findById(sugestaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sugestão", sugestaoId));
        sugestao.aceitar(usuario, motivo);
        sugestaoRepository.expirarPendentes(sugestao.getAplicacao().getId(), sugestao.getId());
        return sugestao;
    }
}
