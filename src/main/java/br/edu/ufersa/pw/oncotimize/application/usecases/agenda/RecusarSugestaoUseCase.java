package br.edu.ufersa.pw.oncotimize.application.usecases.agenda;

import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacao;
import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacaoRepository;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: o profissional recusa a sugestão (motivo opcional); nenhuma data muda. */
@Service
public class RecusarSugestaoUseCase {

    private final SugestaoRemarcacaoRepository sugestaoRepository;

    public RecusarSugestaoUseCase(SugestaoRemarcacaoRepository sugestaoRepository) {
        this.sugestaoRepository = sugestaoRepository;
    }

    @Transactional
    public SugestaoRemarcacao executar(UUID sugestaoId, String usuario, String motivo) {
        SugestaoRemarcacao sugestao = sugestaoRepository.findById(sugestaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sugestão", sugestaoId));
        sugestao.recusar(usuario, motivo);
        return sugestao;
    }
}
