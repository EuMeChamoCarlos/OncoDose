package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.common.ResponseDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos.dto.FrascoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos.BuscarFrascoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos.CriarFrascoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/frascos")
@RequiredArgsConstructor
public class FrascosController {

    private final CriarFrascoUseCase criarFrascoUseCase;
    private final BuscarFrascoUseCase buscarFrascoUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseDTO<?>> create(@Valid @RequestBody FrascoRequest request) {
        Frascos criado = criarFrascoUseCase.executar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(new ResponseDTO<>(criado));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<?>> list(Pageable pageable) {
        ResponseDTO<?> response = new ResponseDTO<>(buscarFrascoUseCase.listar(pageable));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{frascoId}")
    public ResponseEntity<ResponseDTO<?>> getById(@PathVariable UUID frascoId) {
        ResponseDTO<?> response = new ResponseDTO<>(buscarFrascoUseCase.porId(frascoId));
        return ResponseEntity.ok(response);
    }
}
