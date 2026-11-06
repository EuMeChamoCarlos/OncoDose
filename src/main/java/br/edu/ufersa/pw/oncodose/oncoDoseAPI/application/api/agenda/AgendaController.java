package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto.PreviaAgendaResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda.PreverSugestoesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/agenda")
@RequiredArgsConstructor
public class AgendaController {
    private final PreverSugestoesUseCase preverSugestoesUseCase;

    /** Só leitura: roda o planejador sobre a agenda atual sem gravar nada (gravar/aceitar/recusar: fase 4). */
    @GetMapping("/sugestoes/previa")
    public ResponseEntity<PreviaAgendaResponse> previa(
            @RequestParam UUID medicamentoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(PreviaAgendaResponse.fromPrevia(preverSugestoesUseCase.executar(medicamentoId, inicio, fim)));
    }
}
