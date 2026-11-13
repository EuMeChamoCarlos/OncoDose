package br.edu.ufersa.pw.oncotimize.domain.protocolo;

import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Quimioterápico do protocolo, como na tela Medicamentos do Tasy: dose final em mg
 * ("Dose a ser administrada"), via e dias de aplicação no ciclo (ex: "D1,D8").
 * Pré-medicação e diluente não entram. Nasce só por {@link ProtocoloPaciente#novo}.
 */
@Getter
@Entity
@Table(name = "protocolo_medicamento")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProtocoloMedicamento {
    /** Mesmo formato do Tasy, sem espaços: "D1" ou "D1,D8,D15". */
    public static final String PADRAO_DIAS = "^D[1-9]\\d*(,D[1-9]\\d*)*$";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocolo_id", nullable = false)
    private ProtocoloPaciente protocolo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dose_mg", nullable = false)
    private Double doseMg;

    @Column(nullable = false, length = 10)
    private String via;

    @Column(name = "dias_aplicacao", nullable = false)
    private String diasAplicacao;

    ProtocoloMedicamento(ProtocoloPaciente protocolo, Medicamento medicamento, Double doseMg, String via, String diasAplicacao) {
        this.protocolo = protocolo;
        this.medicamento = medicamento;
        this.doseMg = doseMg;
        this.via = via;
        this.diasAplicacao = diasAplicacao;
    }

    /** "D1,D8" → [1, 8], em ordem. */
    public List<Integer> dias() {
        return dias(diasAplicacao);
    }

    public boolean aplicaNoDia(int diaCiclo) {
        return dias().contains(diaCiclo);
    }

    static List<Integer> dias(String diasAplicacao) {
        return Arrays.stream(diasAplicacao.split(","))
                .map(d -> Integer.valueOf(d.substring(1)))
                .distinct()
                .sorted()
                .toList();
    }
}
