package br.edu.ufersa.pw.oncotimize.application.api.protocolo;

import br.edu.ufersa.pw.oncotimize.application.api.protocolo.dto.ProtocoloRequest;
import br.edu.ufersa.pw.oncotimize.application.api.protocolo.dto.ProtocoloResponse;
import br.edu.ufersa.pw.oncotimize.application.usecases.protocolo.BuscarProtocoloUseCase;
import br.edu.ufersa.pw.oncotimize.application.usecases.protocolo.CriarProtocoloUseCase;
import br.edu.ufersa.pw.oncotimize.application.usecases.protocolo.DeletarProtocoloUseCase;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPaciente;
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
@RequestMapping("/api/protocolos")
@RequiredArgsConstructor
public class ProtocoloController {
    private final CriarProtocoloUseCase criarProtocoloUseCase;
    private final BuscarProtocoloUseCase buscarProtocoloUseCase;
    private final DeletarProtocoloUseCase deletarProtocoloUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProtocoloResponse> create(@Valid @RequestBody ProtocoloRequest request) {
        ProtocoloPaciente criado = criarProtocoloUseCase.executar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(ProtocoloResponse.fromProtocolo(criado));
    }

    @GetMapping
    public ResponseEntity<PagedModel<ProtocoloResponse>> list(
            @RequestParam(required = false) UUID medicamentoId,
            Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(
                buscarProtocoloUseCase.listar(medicamentoId, pageable).map(ProtocoloResponse::fromProtocolo)));
    }

    @GetMapping("/{protocoloId}")
    public ResponseEntity<ProtocoloResponse> getById(@PathVariable UUID protocoloId) {
        return ResponseEntity.ok(ProtocoloResponse.fromProtocolo(buscarProtocoloUseCase.porId(protocoloId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{protocoloId}")
    public ResponseEntity<Void> delete(@PathVariable UUID protocoloId) {
        deletarProtocoloUseCase.executar(protocoloId);
        return ResponseEntity.noContent().build();
    }
}
