package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto.MedicamentoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto.MedicamentoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto.UpdateMedicamentoDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento.AtualizarMedicamentoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento.BuscarMedicamentoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento.CriarMedicamentoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento.DeletarMedicamentoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {
    private final CriarMedicamentoUseCase criarMedicamentoUseCase;
    private final BuscarMedicamentoUseCase buscarMedicamentoUseCase;
    private final AtualizarMedicamentoUseCase atualizarMedicamentoUseCase;
    private final DeletarMedicamentoUseCase deletarMedicamentoUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MedicamentoResponse> create(@RequestBody MedicamentoRequest request) {
        Medicamento criado = criarMedicamentoUseCase.executar(request.getNome(), request.getCodigoInterno());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(MedicamentoResponse.fromMedicamento(criado));
    }

    @GetMapping
    public ResponseEntity<PagedModel<MedicamentoResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(
                buscarMedicamentoUseCase.listar(pageable).map(MedicamentoResponse::fromMedicamento)));
    }

    @GetMapping("/{medicamentoId}")
    public ResponseEntity<MedicamentoResponse> getById(@PathVariable UUID medicamentoId) {
        return ResponseEntity.ok(MedicamentoResponse.fromMedicamento(buscarMedicamentoUseCase.porId(medicamentoId)));
    }

    @GetMapping("/codigo/{codigoInterno}")
    public ResponseEntity<MedicamentoResponse> getByCodigoInterno(@PathVariable String codigoInterno) {
        return ResponseEntity.ok(MedicamentoResponse.fromMedicamento(buscarMedicamentoUseCase.porCodigo(codigoInterno)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{medicamentoId}")
    public ResponseEntity<MedicamentoResponse> update(
            @PathVariable UUID medicamentoId,
            @RequestBody UpdateMedicamentoDTO request) {
        return ResponseEntity.ok(MedicamentoResponse.fromMedicamento(
                atualizarMedicamentoUseCase.executar(medicamentoId, request.getNome(), request.getCodigoInterno())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{medicamentoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID medicamentoId) {
        deletarMedicamentoUseCase.executar(medicamentoId);
        return ResponseEntity.noContent().build();
    }
}
