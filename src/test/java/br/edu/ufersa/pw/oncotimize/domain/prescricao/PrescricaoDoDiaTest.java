package br.edu.ufersa.pw.oncotimize.domain.prescricao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ufersa.pw.oncotimize.domain.exception.PrescricaoInvalidaException;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPaciente;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPaciente.MedicamentoNovo;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPacienteRepository;
import java.time.LocalDate;
import java.util.List;
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
    private ProtocoloPacienteRepository protocoloRepository;
    @Autowired
    private PrescricaoRepository prescricaoRepository;

    private ProtocoloPaciente protocolo(String codigo, Medicamento... medicamentos) {
        List<MedicamentoNovo> itens = List.of(medicamentos).stream()
                .map(m -> new MedicamentoNovo(m, 100.0, "IV", "D1"))
                .toList();
        return protocoloRepository.save(ProtocoloPaciente.novo(codigo, "Teste", null, DIA, 2, 21, 19, 0, 0, itens));
    }

    @Test
    void agrupaPelaDataAgendadaDaAplicacaoOuPelaDataDaPrescricaoNoLegado() {
        Medicamento med = medicamentoRepository.save(Medicamento.builder("Teste agrupamento").build());
        ProtocoloPaciente protocolo = protocolo("PTC9999-000001", med);
        Aplicacao primeira = protocolo.getAplicacoes().get(0);  // agendada 01/01/2030
        Aplicacao segunda = protocolo.getAplicacoes().get(1);   // agendada 22/01/2030

        salvar("PRE9999-000001", med, StatusPrescricao.APTO, DIA.minusDays(5), primeira); // entra: agendada no dia
        salvar("PRE9999-000002", med, StatusPrescricao.APTO, DIA, null);                  // entra: legado no dia
        salvar("PRE9999-000003", med, StatusPrescricao.NAO_APTO, DIA, null);              // fora: NAO_APTO
        salvar("PRE9999-000004", med, StatusPrescricao.APTO, DIA, segunda);               // fora: agendada em 22/01

        assertThat(prescricaoRepository.buscarDoDia(med.getId(), DIA, StatusPrescricao.APTO))
                .extracting(Prescricao::getCodigoPrescricao)
                .containsExactly("PRE9999-000001", "PRE9999-000002");
        assertThat(prescricaoRepository.buscarDoDia(med.getId(), DIA.minusDays(5), StatusPrescricao.APTO)).isEmpty();
        assertThat(prescricaoRepository.existsByAplicacaoProtocoloId(protocolo.getId())).isTrue();
    }

    /** Como no AC: o mesmo código e a mesma aplicação, uma linha por quimioterápico. */
    @Test
    void mesmoCodigoEAplicacaoComUmaLinhaPorMedicamento() {
        Medicamento a = medicamentoRepository.save(Medicamento.builder("Teste linha A").build());
        Medicamento b = medicamentoRepository.save(Medicamento.builder("Teste linha B").build());
        Aplicacao primeira = protocolo("PTC9999-000003", a, b).getAplicacoes().get(0);

        salvar("PRE9999-000020", a, StatusPrescricao.APTO, DIA, primeira);
        salvar("PRE9999-000020", b, StatusPrescricao.APTO, DIA, primeira);
        prescricaoRepository.flush();

        assertThat(prescricaoRepository.buscarDoDia(a.getId(), DIA, StatusPrescricao.APTO)).hasSize(1);
        assertThat(prescricaoRepository.buscarDoDia(b.getId(), DIA, StatusPrescricao.APTO)).hasSize(1);
        assertThat(prescricaoRepository.existsByCodigoPrescricaoAndMedicamentoId("PRE9999-000020", a.getId())).isTrue();
        assertThat(prescricaoRepository.existsByAplicacaoIdAndMedicamentoIdAndDataPrescricao(primeira.getId(), b.getId(), DIA)).isTrue();
    }

    @Test
    void recusaAplicacaoDeOutroMedicamentoOuJaRealizada() {
        Medicamento med = medicamentoRepository.save(Medicamento.builder("Teste vínculo A").build());
        Medicamento outro = medicamentoRepository.save(Medicamento.builder("Teste vínculo B").build());
        Aplicacao primeira = protocolo("PTC9999-000002", med).getAplicacoes().get(0);

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
