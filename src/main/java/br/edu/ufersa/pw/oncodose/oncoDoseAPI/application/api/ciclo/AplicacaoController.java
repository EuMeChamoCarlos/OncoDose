package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto.AplicacaoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto.JanelaRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo.DefinirJanelaAplicacaoUseCase;
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

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{aplicacaoId}/janela")
    public ResponseEntity<AplicacaoResponse> definirJanela(
            @PathVariable UUID aplicacaoId,
            @Valid @RequestBody JanelaRequest request) {
        return ResponseEntity.ok(AplicacaoResponse.fromAplicacao(
                definirJanelaAplicacaoUseCase.executar(aplicacaoId, request)));
    }
}
