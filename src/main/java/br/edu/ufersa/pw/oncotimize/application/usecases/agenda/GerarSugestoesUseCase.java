package br.edu.ufersa.pw.oncotimize.application.usecases.agenda;

import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacao;
import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacaoRepository;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.AplicacaoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: roda o planejador da agenda e grava as sugestões (SUGERIDA) para um profissional decidir.
 * Sugestões pendentes antigas das mesmas aplicações expiram.
 */
@Service
public class GerarSugestoesUseCase {

    private final PreverSugestoesUseCase preverSugestoes;
    private final AplicacaoRepository aplicacaoRepository;
    private final SugestaoRemarcacaoRepository sugestaoRepository;

    public GerarSugestoesUseCase(PreverSugestoesUseCase preverSugestoes, AplicacaoRepository aplicacaoRepository,
                                 SugestaoRemarcacaoRepository sugestaoRepository) {
        this.preverSugestoes = preverSugestoes;
        this.aplicacaoRepository = aplicacaoRepository;
        this.sugestaoRepository = sugestaoRepository;
    }

    @Transactional
    public List<SugestaoRemarcacao> executar(UUID medicamentoId, LocalDate inicio, LocalDate fim) {
        return preverSugestoes.executar(medicamentoId, inicio, fim).sugestoes().stream()
                .map(s -> {
                    Aplicacao aplicacao = aplicacaoRepository.findById(s.aplicacaoId())
                            .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação", s.aplicacaoId()));
                    sugestaoRepository.expirarPendentes(aplicacao.getId(), null);
                    return sugestaoRepository.save(SugestaoRemarcacao.daAgenda(aplicacao, s.destino(),
                            BigDecimal.valueOf(s.ganho()).setScale(4, RoundingMode.HALF_UP)));
                })
                .toList();
    }
}
