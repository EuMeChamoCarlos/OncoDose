package br.edu.ufersa.pw.oncotimize.application.usecases.agenda;

import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacao;
import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: listar sugestões de remarcação; status opcional (null lista todas). */
@Service
public class ListarSugestoesUseCase {

    private final SugestaoRemarcacaoRepository sugestaoRepository;

    public ListarSugestoesUseCase(SugestaoRemarcacaoRepository sugestaoRepository) {
        this.sugestaoRepository = sugestaoRepository;
    }

    @Transactional(readOnly = true)
    public Page<SugestaoRemarcacao> executar(SugestaoRemarcacao.Status status, Pageable pageable) {
        return status == null ? sugestaoRepository.findAll(pageable) : sugestaoRepository.findByStatus(status, pageable);
    }
}
