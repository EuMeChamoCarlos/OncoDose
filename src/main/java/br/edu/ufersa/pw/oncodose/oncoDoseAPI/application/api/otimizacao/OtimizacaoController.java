package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao.dto.OtimizacaoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao.dto.OtimizarRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao.BuscarOtimizacaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao.OtimizarDiaUseCase;
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
}
