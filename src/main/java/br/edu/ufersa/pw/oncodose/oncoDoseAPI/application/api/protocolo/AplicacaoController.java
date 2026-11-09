package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.protocolo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.protocolo.dto.AplicacaoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.protocolo.dto.JanelaRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.protocolo.DefinirJanelaAplicacaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto.SugestaoDtos.RemarcacaoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto.SugestaoDtos.SugestaoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda.SugerirRemarcacaoNaoAptoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/aplicacoes")
@RequiredArgsConstructor
public class AplicacaoController {
    private final DefinirJanelaAplicacaoUseCase definirJanelaAplicacaoUseCase;
    private final SugerirRemarcacaoNaoAptoUseCase sugerirRemarcacaoNaoAptoUseCase;

    /**
     * Paciente não apto: sugere o melhor dia a partir da data mínima e grava como sugestão NAO_APTO
     * (aceitar/recusar em /api/agenda/sugestoes/{id}/aceite|recusa).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{aplicacaoId}/remarcacao")
    public ResponseEntity<SugestaoResponse> remarcar(@PathVariable UUID aplicacaoId,
                                                     @Valid @RequestBody RemarcacaoRequest request) {
        return ResponseEntity.status(201).body(SugestaoResponse.fromSugestao(
                sugerirRemarcacaoNaoAptoUseCase.executar(aplicacaoId, request.dataMinima(), request.motivo())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{aplicacaoId}/janela")
    public ResponseEntity<AplicacaoResponse> definirJanela(
            @PathVariable UUID aplicacaoId,
            @Valid @RequestBody JanelaRequest request) {
        return ResponseEntity.ok(AplicacaoResponse.fromAplicacao(
                definirJanelaAplicacaoUseCase.executar(aplicacaoId, request)));
    }
}
