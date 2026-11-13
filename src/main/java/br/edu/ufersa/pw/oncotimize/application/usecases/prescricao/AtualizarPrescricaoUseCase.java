package br.edu.ufersa.pw.oncotimize.application.usecases.prescricao;

import br.edu.ufersa.pw.oncotimize.application.api.prescricao.dto.AtualizarPrescricaoRequest;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.PrescricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: corrigir dose, status ou data de uma prescrição.
 */
@Service
public class AtualizarPrescricaoUseCase {

    private final PrescricaoRepository prescricaoRepository;

    public AtualizarPrescricaoUseCase(PrescricaoRepository prescricaoRepository) {
        this.prescricaoRepository = prescricaoRepository;
    }

    @Transactional
    public Prescricao executar(UUID prescricaoId, AtualizarPrescricaoRequest request) {
        Prescricao prescricao = prescricaoRepository.findById(prescricaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prescrição", prescricaoId));
        prescricao.atualizar(request.doseMg(), request.status(), request.dataPrescricao());
        // Apto/não apto e data valem para a prescrição inteira: as outras linhas acompanham; a dose de cada uma fica.
        prescricaoRepository.findByCodigoPrescricao(prescricao.getCodigoPrescricao()).stream()
                .filter(outra -> !outra.getId().equals(prescricao.getId()))
                .forEach(outra -> outra.atualizar(outra.getDoseMg(), request.status(), request.dataPrescricao()));
        return prescricao;
    }
}
