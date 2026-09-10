package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/frascos")
@RequiredArgsConstructor
public class FrascosController {

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Frascos frascos) {
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return null;
    }

    @GetMapping("/{frascosId}")
    public ResponseEntity<?> getById(@PathVariable UUID frascosId) {
        return null;
    }

    @PutMapping("/{frascosId}")
    public ResponseEntity<?> update(
            @PathVariable UUID frascosId,
            @RequestBody Frascos frascos
    ) {
        return null;
    }

    @DeleteMapping("/{frascosId}")
    public ResponseEntity<Void> delete(@PathVariable UUID frascosId) {
        return null;
    }
}