package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.common.ResponseDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/medicamento")
@RequiredArgsConstructor
public class MedicamentoController {

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Medicamento medicamento) {
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return null;
    }

    @GetMapping("/{medicamentoId}")
    public ResponseEntity<?> getById(@PathVariable UUID medicamentoId) {
        return null;
    }

    @PutMapping("/{medicamentoId}")
    public ResponseEntity<?> update(
            @PathVariable UUID medicamentoId,
            @RequestBody Medicamento medicamento
    ) {
        return null;
    }

    @DeleteMapping("/{medicamentoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID medicamentoId) {
        return null;
    }
}
