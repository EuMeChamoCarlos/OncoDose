package br.edu.ufersa.pw.oncotimize.domain.protocolo;

import br.edu.ufersa.pw.oncotimize.domain.exception.ProtocoloInvalidoException;
import br.edu.ufersa.pw.oncotimize.domain.exception.ReagendamentoInvalidoException;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Entidade rica: protocolo de um paciente anonimizado (LGPD), como na tela Protocolos do Tasy.
 * Tem vários quimioterápicos, cada um com seus dias de aplicação. Nasce por {@link #novo}, que já
 * projeta as aplicações; remarcar passa por {@link #reagendar}, que guarda a janela autorizada e o
 * intervalo mínimo entre aplicações. "Ciclo" aqui é como no Tasy: a repetição numerada do protocolo.
 */
@Getter
@Entity
@Table(name = "protocolo_paciente")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProtocoloPaciente {
    /** Mesmo espírito do PRE0000-000000 da prescrição, ex: PTC2026-000001. */
    public static final String PADRAO_CODIGO = "^PTC\\d{4}-\\d{6}$";
    public static final int MAX_APLICACOES = 100;

    /** Dados de um quimioterápico ao criar o protocolo. */
    public record MedicamentoNovo(Medicamento medicamento, Double doseMg, String via, String diasAplicacao) {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_protocolo", nullable = false, unique = true, length = 14)
    private String codigoProtocolo;

    /** "Medicação/Subtipo" do Tasy, ex: TAX W, AC. */
    @Column(name = "medicacao_subtipo", nullable = false, length = 100)
    private String medicacaoSubtipo;

    /** "Protocolo" do Tasy, ex: Sistema Reprodutivo Feminino. */
    @Column(length = 100)
    private String grupo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private Integer ciclos;

    /** "Dias intervalo" do Tasy: distância entre o início de um ciclo e o do seguinte. */
    @Column(name = "dias_intervalo", nullable = false)
    private Integer diasIntervalo;

    /** Menor distância permitida entre aplicações vizinhas ao remarcar. Não existe no Tasy. */
    @Column(name = "intervalo_minimo_dias", nullable = false)
    private Integer intervaloMinimoDias;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "protocolo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProtocoloMedicamento> medicamentos = new ArrayList<>();

    // BatchSize: a listagem paginada carrega as aplicações de vários protocolos numa consulta só (sem N+1).
    @BatchSize(size = 50)
    @OrderBy("numero")
    @OneToMany(mappedBy = "protocolo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Aplicacao> aplicacoes = new ArrayList<>();

    /**
     * Projeta uma aplicação para cada dia de aplicação de cada ciclo:
     * data prevista = dataInicio + (ciclo - 1) · diasIntervalo + (dia - 1).
     */
    public static ProtocoloPaciente novo(String codigoProtocolo, String medicacaoSubtipo, String grupo, LocalDate dataInicio,
                                         Integer ciclos, Integer diasIntervalo, Integer intervaloMinimoDias,
                                         int diasAntes, int diasDepois, List<MedicamentoNovo> medicamentos) {
        if (codigoProtocolo == null || !codigoProtocolo.matches(PADRAO_CODIGO)) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "código fora do padrão PTC0000-000000");
        }
        if (medicacaoSubtipo == null || medicacaoSubtipo.isBlank()) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "subtipo obrigatório");
        }
        if (dataInicio == null) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "data de início obrigatória");
        }
        if (ciclos == null || ciclos < 1) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "deve haver ao menos 1 ciclo");
        }
        if (diasIntervalo == null || diasIntervalo < 1) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "intervalo deve ser de pelo menos 1 dia");
        }
        if (medicamentos == null || medicamentos.isEmpty()) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "medicamento obrigatório");
        }

        ProtocoloPaciente protocolo = new ProtocoloPaciente();
        protocolo.codigoProtocolo = codigoProtocolo;
        protocolo.medicacaoSubtipo = medicacaoSubtipo;
        protocolo.grupo = grupo;
        protocolo.dataInicio = dataInicio;
        protocolo.ciclos = ciclos;
        protocolo.diasIntervalo = diasIntervalo;

        TreeSet<Integer> dias = new TreeSet<>();
        for (MedicamentoNovo novo : medicamentos) {
            protocolo.adicionar(novo);
            dias.addAll(ProtocoloMedicamento.dias(novo.diasAplicacao()));
        }
        long total = (long) ciclos * dias.size();
        if (total > MAX_APLICACOES) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "total de aplicações deve estar entre 1 e " + MAX_APLICACOES);
        }
        if (ciclos > 1 && dias.last() > diasIntervalo) {
            throw new ProtocoloInvalidoException(codigoProtocolo,
                    "dia D" + dias.last() + " passa do intervalo de " + diasIntervalo + " dias: os ciclos se sobreporiam");
        }

        int numero = 0;
        for (int ciclo = 1; ciclo <= ciclos; ciclo++) {
            for (int dia : dias) {
                LocalDate prevista = dataInicio.plusDays((long) (ciclo - 1) * diasIntervalo + dia - 1);
                protocolo.aplicacoes.add(new Aplicacao(protocolo, ++numero, ciclo, dia, prevista, diasAntes, diasDepois));
            }
        }
        protocolo.definirIntervaloMinimo(intervaloMinimoDias);
        return protocolo;
    }

    private void adicionar(MedicamentoNovo novo) {
        if (novo == null || novo.medicamento() == null) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "medicamento obrigatório");
        }
        if (novo.doseMg() == null || novo.doseMg() <= 0) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "dose deve ser maior que zero");
        }
        if (novo.via() == null || novo.via().isBlank()) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "via obrigatória");
        }
        if (novo.diasAplicacao() == null || !novo.diasAplicacao().matches(ProtocoloMedicamento.PADRAO_DIAS)) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "dias de aplicação fora do padrão D1,D8");
        }
        boolean repetido = medicamentos.stream()
                .anyMatch(m -> m.getMedicamento() == novo.medicamento()
                        || (m.getMedicamento().getId() != null
                        && m.getMedicamento().getId().equals(novo.medicamento().getId())));
        if (repetido) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "medicamento repetido no protocolo");
        }
        medicamentos.add(new ProtocoloMedicamento(this, novo.medicamento(), novo.doseMg(), novo.via(), novo.diasAplicacao()));
    }

    /** O mínimo não pode passar da menor distância já prevista, ou nenhuma remarcação seria possível. */
    private void definirIntervaloMinimo(Integer intervaloMinimoDias) {
        long menorDistancia = Long.MAX_VALUE;
        for (int i = 1; i < aplicacoes.size(); i++) {
            menorDistancia = Math.min(menorDistancia,
                    ChronoUnit.DAYS.between(aplicacoes.get(i - 1).getDataPrevista(), aplicacoes.get(i).getDataPrevista()));
        }
        if (intervaloMinimoDias == null || intervaloMinimoDias < 0 || intervaloMinimoDias > menorDistancia) {
            throw new ProtocoloInvalidoException(codigoProtocolo,
                    "intervalo mínimo deve estar entre 0 e a menor distância entre aplicações");
        }
        this.intervaloMinimoDias = intervaloMinimoDias;
    }

    public int totalAplicacoes() {
        return aplicacoes.size();
    }

    public Optional<ProtocoloMedicamento> medicamentoDe(UUID medicamentoId) {
        return medicamentos.stream()
                .filter(m -> Objects.equals(m.getMedicamento().getId(), medicamentoId))
                .findFirst();
    }

    /** O que já aconteceu (ou foi cancelado) é histórico: protocolo assim não se apaga. */
    public boolean possuiAplicacaoEncerrada() {
        return aplicacoes.stream().anyMatch(a -> a.getStatus() != StatusAplicacao.AGENDADA);
    }

    /**
     * Move a aplicação para novaData se couber na janela autorizada e não ficar a menos de
     * intervaloMinimoDias das vizinhas (canceladas não contam). Usado pelo aceite de sugestão.
     */
    public void reagendar(int numero, LocalDate novaData) {
        mover(numero, novaData, true, null);
    }

    public void reagendar(int numero, LocalDate novaData, String motivo) {
        mover(numero, novaData, true, motivo);
    }

    /**
     * Paciente não apto: volta numa data definida a partir do critério clínico, fora da janela de flexibilidade
     * (que é só para juntar pacientes). Continua valendo o intervalo mínimo com as vizinhas. Motivo obrigatório.
     */
    public void remarcar(int numero, LocalDate novaData, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ProtocoloInvalidoException(codigoProtocolo, "motivo da remarcação é obrigatório");
        }
        mover(numero, novaData, false, motivo);
    }

    private void mover(int numero, LocalDate novaData, boolean exigirJanela, String motivo) {
        Aplicacao alvo = aplicacoes.stream()
                .filter(a -> a.getNumero() == numero)
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação", codigoProtocolo + " nº " + numero));
        if (alvo.getStatus() != StatusAplicacao.AGENDADA) {
            throw new ReagendamentoInvalidoException(codigoProtocolo, numero, novaData, "aplicação já está " + alvo.getStatus());
        }
        if (exigirJanela && !alvo.janelaPermite(novaData)) {
            throw new ReagendamentoInvalidoException(codigoProtocolo, numero, novaData,
                    "fora da janela " + alvo.inicioJanela() + " a " + alvo.fimJanela());
        }
        Optional<Aplicacao> anterior = vizinhas(numero).filter(a -> a.getNumero() < numero)
                .max(Comparator.comparing(Aplicacao::getNumero));
        if (anterior.isPresent()
                && ChronoUnit.DAYS.between(anterior.get().getDataAgendada(), novaData) < intervaloMinimoDias) {
            throw new ReagendamentoInvalidoException(codigoProtocolo, numero, novaData,
                    "menos de " + intervaloMinimoDias + " dias da aplicação " + anterior.get().getNumero());
        }
        Optional<Aplicacao> seguinte = vizinhas(numero).filter(a -> a.getNumero() > numero)
                .min(Comparator.comparing(Aplicacao::getNumero));
        if (seguinte.isPresent()
                && ChronoUnit.DAYS.between(novaData, seguinte.get().getDataAgendada()) < intervaloMinimoDias) {
            throw new ReagendamentoInvalidoException(codigoProtocolo, numero, novaData,
                    "menos de " + intervaloMinimoDias + " dias da aplicação " + seguinte.get().getNumero());
        }
        alvo.agendarPara(novaData, motivo);
    }

    private Stream<Aplicacao> vizinhas(int numero) {
        return aplicacoes.stream().filter(a -> a.getNumero() != numero && a.getStatus() != StatusAplicacao.CANCELADA);
    }
}
