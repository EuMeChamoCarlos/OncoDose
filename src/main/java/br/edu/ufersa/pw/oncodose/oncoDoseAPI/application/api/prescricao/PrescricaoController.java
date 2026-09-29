package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto.AtualizarPrescricaoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto.PrescricaoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto.PrescricaoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao.AtualizarPrescricaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao.BuscarPrescricaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao.CriarPrescricaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.prescricao.DeletarPrescricaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/prescricoes")
@RequiredArgsConstructor
public class    PrescricaoController {
    private final CriarPrescricaoUseCase criarPrescricaoUseCase;
    private final BuscarPrescricaoUseCase buscarPrescricaoUseCase;
    private final AtualizarPrescricaoUseCase atualizarPrescricaoUseCase;
    private final DeletarPrescricaoUseCase deletarPrescricaoUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PrescricaoResponse> create(@Valid @RequestBody PrescricaoRequest request) {
        Prescricao criada = criarPrescricaoUseCase.executar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.getId())
                .toUri();
        return ResponseEntity.created(location).body(PrescricaoResponse.fromPrescricao(criada));
    }

    @GetMapping
    public ResponseEntity<PagedModel<PrescricaoResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) UUID medicamentoId,
            Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(
                buscarPrescricaoUseCase.listar(data, medicamentoId, pageable).map(PrescricaoResponse::fromPrescricao)));
    }

    @GetMapping("/{prescricaoId}")
    public ResponseEntity<PrescricaoResponse> getById(@PathVariable UUID prescricaoId) {
        return ResponseEntity.ok(PrescricaoResponse.fromPrescricao(buscarPrescricaoUseCase.porId(prescricaoId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{prescricaoId}")
    public ResponseEntity<PrescricaoResponse> update(
            @PathVariable UUID prescricaoId,
            @Valid @RequestBody AtualizarPrescricaoRequest request) {
        return ResponseEntity.ok(PrescricaoResponse.fromPrescricao(
                atualizarPrescricaoUseCase.executar(prescricaoId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{prescricaoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID prescricaoId) {
        deletarPrescricaoUseCase.executar(prescricaoId);
        return ResponseEntity.noContent().build();
    }
}
