package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.common.ResponseDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto.MedicamentoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto.UpdateMedicamentoDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/medicamento")
@RequiredArgsConstructor
public class MedicamentoController {
    private final MedicamentoService medicamentoService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseDTO<?>> create(@RequestBody MedicamentoRequest request) {
        ResponseDTO<?> response = new ResponseDTO<>(
                medicamentoService.createMedicamento(request.getNome(), request.getCodigoInterno()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<?>> list(Pageable pageable) {
        ResponseDTO<?> response = new ResponseDTO<>(medicamentoService.getAllMedicamentos(pageable));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{medicamentoId}")
    public ResponseEntity<ResponseDTO<?>> getById(@PathVariable UUID medicamentoId) {
        ResponseDTO<?> response = new ResponseDTO<>(medicamentoService.getMedicamentoById(medicamentoId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/codigo/{codigoInterno}")
    public ResponseEntity<ResponseDTO<?>> getByCodigoInterno(@PathVariable String codigoInterno) {
        ResponseDTO<?> response = new ResponseDTO<>(medicamentoService.getMedicamentoByCodigoInterno(codigoInterno));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{medicamentoId}")
    public ResponseEntity<ResponseDTO<?>> update(
            @PathVariable UUID medicamentoId,
            @RequestBody UpdateMedicamentoDTO request) {
        ResponseDTO<?> response = new ResponseDTO<>(
                medicamentoService.updateMedicamento(medicamentoId, request.getNome(), request.getCodigoInterno()));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{medicamentoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID medicamentoId) {
        medicamentoService.deleteMedicamento(medicamentoId);
        return ResponseEntity.noContent().build();
    }
}
