package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao.dto.OtimizacaoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao.dto.OtimizarRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao.BuscarOtimizacaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao.ConfirmarOtimizacaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao.OtimizarDiaUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao.RecusarOtimizacaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto.SugestaoDtos.DecisaoRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.Otimizacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/otimizacoes")
@RequiredArgsConstructor
public class OtimizacaoController {
    private final OtimizarDiaUseCase otimizarDiaUseCase;
    private final BuscarOtimizacaoUseCase buscarOtimizacaoUseCase;
    private final ConfirmarOtimizacaoUseCase confirmarOtimizacaoUseCase;
    private final RecusarOtimizacaoUseCase recusarOtimizacaoUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<OtimizacaoResponse> otimizar(@Valid @RequestBody OtimizarRequest request) {
        Otimizacao criada = otimizarDiaUseCase.executar(request.medicamentoId(), request.data());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.getId())
                .toUri();
        return ResponseEntity.created(location).body(OtimizacaoResponse.fromOtimizacao(criada));
    }

    @GetMapping("/{otimizacaoId}")
    public ResponseEntity<OtimizacaoResponse> getById(@PathVariable UUID otimizacaoId) {
        return ResponseEntity.ok(OtimizacaoResponse.fromOtimizacao(buscarOtimizacaoUseCase.porId(otimizacaoId)));
    }

    /** PUT idempotente: repetir (ex: resposta perdida na rede) devolve o mesmo estado, sem baixar de novo. */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{otimizacaoId}/confirmacao")
    public ResponseEntity<OtimizacaoResponse> confirmar(@PathVariable UUID otimizacaoId) {
        return ResponseEntity.ok(OtimizacaoResponse.fromOtimizacao(confirmarOtimizacaoUseCase.executar(otimizacaoId)));
    }

    /** O profissional recusa o plano (motivo opcional). Plano confirmado não se recusa → 422. */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{otimizacaoId}/recusa")
    public ResponseEntity<OtimizacaoResponse> recusar(@PathVariable UUID otimizacaoId,
                                                      @Valid @RequestBody(required = false) DecisaoRequest request,
                                                      @AuthenticationPrincipal UserDetails usuario) {
        String motivo = request == null ? null : request.motivo();
        return ResponseEntity.ok(OtimizacaoResponse.fromOtimizacao(
                recusarOtimizacaoUseCase.executar(otimizacaoId, usuario.getUsername(), motivo)));
    }
}
