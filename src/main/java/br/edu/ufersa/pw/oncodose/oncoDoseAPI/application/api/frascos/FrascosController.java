package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos.dto.FrascoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos.dto.FrascoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos.AtualizarFrascoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos.BuscarFrascoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos.DeletarFrascoUseCase;
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
    private final AtualizarFrascoUseCase atualizarFrascoUseCase;
    private final DeletarFrascoUseCase deletarFrascoUseCase;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{frascoId}")
    public ResponseEntity<FrascoResponse> update(
            @PathVariable UUID medicamentoId,
            @PathVariable UUID frascoId,
            @Valid @RequestBody FrascoRequest request) {
        return ResponseEntity.ok(FrascoResponse.fromFrasco(
                atualizarFrascoUseCase.executar(medicamentoId, frascoId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{frascoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID medicamentoId, @PathVariable UUID frascoId) {
        deletarFrascoUseCase.executar(medicamentoId, frascoId);
        return ResponseEntity.noContent().build();
    }
}
