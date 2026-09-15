package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.common.ResponseDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto.MedicamentoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto.UpdateMedicamentoDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento.AtualizarMedicamentoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento.BuscarMedicamentoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento.CriarMedicamentoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.medicamento.DeletarMedicamentoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<ResponseDTO<?>> create(@RequestBody MedicamentoRequest request) {
        Medicamento criado = criarMedicamentoUseCase.executar(request.getNome(), request.getCodigoInterno());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(new ResponseDTO<>(criado));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<?>> list(Pageable pageable) {
        ResponseDTO<?> response = new ResponseDTO<>(buscarMedicamentoUseCase.listar(pageable));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{medicamentoId}")
    public ResponseEntity<ResponseDTO<?>> getById(@PathVariable UUID medicamentoId) {
        ResponseDTO<?> response = new ResponseDTO<>(buscarMedicamentoUseCase.porId(medicamentoId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/codigo/{codigoInterno}")
    public ResponseEntity<ResponseDTO<?>> getByCodigoInterno(@PathVariable String codigoInterno) {
        ResponseDTO<?> response = new ResponseDTO<>(buscarMedicamentoUseCase.porCodigo(codigoInterno));
        return ResponseEntity.ok(response);
    }   

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{medicamentoId}")
    public ResponseEntity<ResponseDTO<?>> update(
            @PathVariable UUID medicamentoId,
            @RequestBody UpdateMedicamentoDTO request) {
        ResponseDTO<?> response = new ResponseDTO<>(
                atualizarMedicamentoUseCase.executar(medicamentoId, request.getNome(), request.getCodigoInterno()));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{medicamentoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID medicamentoId) {
        deletarMedicamentoUseCase.executar(medicamentoId);
        return ResponseEntity.noContent().build();
    }
}
