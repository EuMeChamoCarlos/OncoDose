package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.CicloRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.PrescricaoInvalidaException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integração com o Postgres local (como o teste de contexto). @Transactional desfaz tudo ao fim.
 * Datas em 2030 e códigos 9999 para não cruzar com o seed.
 */
@SpringBootTest
@Transactional
class PrescricaoDoDiaTest {

    private static final LocalDate DIA = LocalDate.of(2030, 1, 1);

    @Autowired
    private MedicamentoRepository medicamentoRepository;
    @Autowired
    private CicloRepository cicloRepository;
    @Autowired
    private PrescricaoRepository prescricaoRepository;

    @Test
    void agrupaPelaDataAgendadaDaAplicacaoOuPelaDataDaPrescricaoNoLegado() {
        Medicamento med = medicamentoRepository.save(Medicamento.builder("Teste agrupamento").build());
        Ciclo ciclo = cicloRepository.save(Ciclo.novo("CIC9999-000001", med, 100.0, DIA, 2, 21, 19, 0, 0));
        Aplicacao primeira = ciclo.getAplicacoes().get(0);  // agendada 01/01/2030
        Aplicacao segunda = ciclo.getAplicacoes().get(1);   // agendada 22/01/2030

        salvar("PRE9999-000001", med, StatusPrescricao.APTO, DIA.minusDays(5), primeira); // entra: agendada no dia
        salvar("PRE9999-000002", med, StatusPrescricao.APTO, DIA, null);                  // entra: legado no dia
        salvar("PRE9999-000003", med, StatusPrescricao.NAO_APTO, DIA, null);              // fora: NAO_APTO
        salvar("PRE9999-000004", med, StatusPrescricao.APTO, DIA, segunda);               // fora: agendada em 22/01

        assertThat(prescricaoRepository.buscarDoDia(med.getId(), DIA, StatusPrescricao.APTO))
                .extracting(Prescricao::getCodigoPrescricao)
                .containsExactly("PRE9999-000001", "PRE9999-000002");
        assertThat(prescricaoRepository.buscarDoDia(med.getId(), DIA.minusDays(5), StatusPrescricao.APTO)).isEmpty();
        assertThat(prescricaoRepository.existsByAplicacaoCicloId(ciclo.getId())).isTrue();
    }

    @Test
    void recusaAplicacaoDeOutroMedicamentoOuJaRealizada() {
        Medicamento med = medicamentoRepository.save(Medicamento.builder("Teste vínculo A").build());
        Medicamento outro = medicamentoRepository.save(Medicamento.builder("Teste vínculo B").build());
        Ciclo ciclo = cicloRepository.save(Ciclo.novo("CIC9999-000002", med, 100.0, DIA, 2, 21, 19, 0, 0));
        Aplicacao primeira = ciclo.getAplicacoes().get(0);

        assertThatThrownBy(() -> Prescricao.nova("PRE9999-000010", outro, 100.0, StatusPrescricao.APTO, DIA, primeira))
                .isInstanceOf(PrescricaoInvalidaException.class).hasMessageContaining("outro medicamento");

        primeira.marcarRealizada();
        assertThatThrownBy(() -> Prescricao.nova("PRE9999-000011", med, 100.0, StatusPrescricao.APTO, DIA, primeira))
                .isInstanceOf(PrescricaoInvalidaException.class).hasMessageContaining("REALIZADA");
    }

    private void salvar(String codigo, Medicamento med, StatusPrescricao status, LocalDate data, Aplicacao aplicacao) {
        prescricaoRepository.save(Prescricao.nova(codigo, med, 100.0, status, data, aplicacao));
    }
}
