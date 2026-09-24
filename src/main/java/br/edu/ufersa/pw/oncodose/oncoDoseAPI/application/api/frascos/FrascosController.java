package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos.dto.FrascoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos.dto.FrascoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos.BuscarFrascoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos.CriarFrascoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
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
@RequestMapping("/api/medicamentos/{medicamentoId}/frascos")
@RequiredArgsConstructor
public class FrascosController {

    private final CriarFrascoUseCase criarFrascoUseCase;
    private final BuscarFrascoUseCase buscarFrascoUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FrascoResponse> create(
            @PathVariable UUID medicamentoId,
            @Valid @RequestBody FrascoRequest request) {
        Frascos criado = criarFrascoUseCase.executar(medicamentoId, request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(FrascoResponse.fromFrasco(criado));
    }

    @GetMapping
    public ResponseEntity<PagedModel<FrascoResponse>> list(@PathVariable UUID medicamentoId, Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(
                buscarFrascoUseCase.listar(medicamentoId, pageable).map(FrascoResponse::fromFrasco)));
    }

    @GetMapping("/{frascoId}")
    public ResponseEntity<FrascoResponse> getById(@PathVariable UUID medicamentoId, @PathVariable UUID frascoId) {
        return ResponseEntity.ok(FrascoResponse.fromFrasco(buscarFrascoUseCase.porId(medicamentoId, frascoId)));
    }
}
