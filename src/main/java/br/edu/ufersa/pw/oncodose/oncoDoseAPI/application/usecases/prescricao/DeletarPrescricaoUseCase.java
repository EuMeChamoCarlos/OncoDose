package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.PrescricaoInvalidaException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.AlocacaoFrascoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exclusão de prescrição. Recusa se ela já entrou numa otimização
 * (histórico do TCC; FK RESTRICT em alocacao_frasco).
 */
@Service
public class DeletarPrescricaoUseCase {

    private final PrescricaoRepository prescricaoRepository;
    private final AlocacaoFrascoRepository alocacaoFrascoRepository;

    public DeletarPrescricaoUseCase(
            PrescricaoRepository prescricaoRepository,
            AlocacaoFrascoRepository alocacaoFrascoRepository) {
        this.prescricaoRepository = prescricaoRepository;
        this.alocacaoFrascoRepository = alocacaoFrascoRepository;
    }

    @Transactional
    public void executar(UUID prescricaoId) {
        if (!prescricaoRepository.existsById(prescricaoId)) {
            throw new RecursoNaoEncontradoException("Prescrição", prescricaoId);
        }
        if (alocacaoFrascoRepository.existsByPrescricaoId(prescricaoId)) {
            throw new PrescricaoInvalidaException("possui otimização vinculada");
        }
        prescricaoRepository.deleteById(prescricaoId);
    }
}
