package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto.PreviaAgendaResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto.SugestaoDtos.DecisaoRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto.SugestaoDtos.GerarSugestoesRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto.SugestaoDtos.SugestaoResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda.AceitarSugestaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda.GerarSugestoesUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda.ListarSugestoesUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda.PreverSugestoesUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda.RecusarSugestaoUseCase;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.agenda.SugestaoRemarcacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agenda")
@RequiredArgsConstructor
public class AgendaController {
    private final PreverSugestoesUseCase preverSugestoesUseCase;
    private final GerarSugestoesUseCase gerarSugestoesUseCase;
    private final ListarSugestoesUseCase listarSugestoesUseCase;
    private final AceitarSugestaoUseCase aceitarSugestaoUseCase;
    private final RecusarSugestaoUseCase recusarSugestaoUseCase;

    /** Só leitura: roda o planejador sobre a agenda atual sem gravar nada. */
    @GetMapping("/sugestoes/previa")
    public ResponseEntity<PreviaAgendaResponse> previa(
            @RequestParam UUID medicamentoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(PreviaAgendaResponse.fromPrevia(preverSugestoesUseCase.executar(medicamentoId, inicio, fim)));
    }

    /** Roda o planejador e grava as sugestões (SUGERIDA) para um profissional decidir. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sugestoes")
    public ResponseEntity<List<SugestaoResponse>> gerar(@Valid @RequestBody GerarSugestoesRequest request) {
        return ResponseEntity.status(201).body(gerarSugestoesUseCase
                .executar(request.medicamentoId(), request.inicio(), request.fim()).stream()
                .map(SugestaoResponse::fromSugestao).toList());
    }

    /** status opcional: SUGERIDA, ACEITA, RECUSADA ou EXPIRADA. */
    @GetMapping("/sugestoes")
    public ResponseEntity<PagedModel<SugestaoResponse>> listar(
            @RequestParam(required = false) SugestaoRemarcacao.Status status, Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(listarSugestoesUseCase.executar(status, pageable)
                .map(SugestaoResponse::fromSugestao)));
    }

    /** Aceita e remarca a aplicação na mesma transação; quem aceitou fica registrado. */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/sugestoes/{sugestaoId}/aceite")
    public ResponseEntity<SugestaoResponse> aceitar(@PathVariable UUID sugestaoId,
                                                    @Valid @RequestBody(required = false) DecisaoRequest request,
                                                    @AuthenticationPrincipal UserDetails usuario) {
        return ResponseEntity.ok(SugestaoResponse.fromSugestao(
                aceitarSugestaoUseCase.executar(sugestaoId, usuario.getUsername(), motivo(request))));
    }

    /** Recusa (motivo opcional); nenhuma data muda. */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/sugestoes/{sugestaoId}/recusa")
    public ResponseEntity<SugestaoResponse> recusar(@PathVariable UUID sugestaoId,
                                                    @Valid @RequestBody(required = false) DecisaoRequest request,
                                                    @AuthenticationPrincipal UserDetails usuario) {
        return ResponseEntity.ok(SugestaoResponse.fromSugestao(
                recusarSugestaoUseCase.executar(sugestaoId, usuario.getUsername(), motivo(request))));
    }

    private static String motivo(DecisaoRequest request) {
        return request == null ? null : request.motivo();
    }
}
