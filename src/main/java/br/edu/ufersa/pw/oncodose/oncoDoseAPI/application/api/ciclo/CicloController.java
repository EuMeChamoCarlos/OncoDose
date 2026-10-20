package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto.CicloRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto.CicloResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo.BuscarCicloUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo.CriarCicloUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.ciclo.DeletarCicloUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/ciclos")
@RequiredArgsConstructor
public class CicloController {
    private final CriarCicloUseCase criarCicloUseCase;
    private final BuscarCicloUseCase buscarCicloUseCase;
    private final DeletarCicloUseCase deletarCicloUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CicloResponse> create(@Valid @RequestBody CicloRequest request) {
        Ciclo criado = criarCicloUseCase.executar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(CicloResponse.fromCiclo(criado));
    }

    @GetMapping
    public ResponseEntity<PagedModel<CicloResponse>> list(
            @RequestParam(required = false) UUID medicamentoId,
            Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(
                buscarCicloUseCase.listar(medicamentoId, pageable).map(CicloResponse::fromCiclo)));
    }

    @GetMapping("/{cicloId}")
    public ResponseEntity<CicloResponse> getById(@PathVariable UUID cicloId) {
        return ResponseEntity.ok(CicloResponse.fromCiclo(buscarCicloUseCase.porId(cicloId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cicloId}")
    public ResponseEntity<Void> delete(@PathVariable UUID cicloId) {
        deletarCicloUseCase.executar(cicloId);
        return ResponseEntity.noContent().build();
    }
}
