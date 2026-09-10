package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prescricao")
@RequiredArgsConstructor
public class PrescricaoController {

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Prescricao prescricao) {
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return null;
    }

    @GetMapping("/{prescricaoId}")
    public ResponseEntity<?> getById(@PathVariable UUID prescricaoId) {
        return null;
    }

    @PutMapping("/{prescricaoId}")
    public ResponseEntity<?> update(
            @PathVariable UUID prescricaoId,
            @RequestBody Prescricao prescricao
    ) {
        return null;
    }

    @DeleteMapping("/{prescricaoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID prescricaoId) {
        return null;
    }
}
