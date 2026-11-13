package br.edu.ufersa.pw.oncotimize.application.usecases.otimizacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ufersa.pw.oncotimize.application.api.prescricao.dto.AtualizarPrescricaoRequest;
import br.edu.ufersa.pw.oncotimize.application.api.prescricao.dto.PrescricaoRequest;
import br.edu.ufersa.pw.oncotimize.application.usecases.prescricao.AtualizarPrescricaoUseCase;
import br.edu.ufersa.pw.oncotimize.application.usecases.prescricao.CriarPrescricaoUseCase;
import br.edu.ufersa.pw.oncotimize.domain.exception.PrescricaoInvalidaException;
import br.edu.ufersa.pw.oncotimize.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncotimize.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.PrescricaoRepository;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.StatusPrescricao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPaciente;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPaciente.MedicamentoNovo;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPacienteRepository;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.StatusAplicacao;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integração com o Postgres local: um protocolo como o AC, com dois quimioterápicos no mesmo dia.
 * @Transactional desfaz tudo ao fim; datas em 2031 e códigos 9999 para não cruzar com o seed.
 */
@SpringBootTest
@Transactional
class ConfirmacaoPorLinhasTest {

    private static final LocalDate DIA = LocalDate.of(2031, 1, 6);

    @Autowired private MedicamentoRepository medicamentoRepository;
    @Autowired private FrascosRepository frascosRepository;
    @Autowired private ProtocoloPacienteRepository protocoloRepository;
    @Autowired private PrescricaoRepository prescricaoRepository;
    @Autowired private CriarPrescricaoUseCase criarPrescricao;
    @Autowired private AtualizarPrescricaoUseCase atualizarPrescricao;
    @Autowired private OtimizarDiaUseCase otimizarDia;
    @Autowired private ConfirmarOtimizacaoUseCase confirmarOtimizacao;

    private Medicamento doxo;
    private Medicamento ciclo;
    private Aplicacao primeira;

    @BeforeEach
    void cenario() {
        doxo = medicamentoRepository.save(Medicamento.builder("Teste linhas doxo").build());
        ciclo = medicamentoRepository.save(Medicamento.builder("Teste linhas ciclo").build());
        frasco(doxo, 50, 120);
        frasco(ciclo, 1000, 30);
        ProtocoloPaciente ac = protocoloRepository.save(ProtocoloPaciente.novo("PTC9999-000020", "AC", null, DIA,
                4, 21, 19, 0, 0, List.of(
                        new MedicamentoNovo(doxo, 100.0, "IV", "D1"),
                        new MedicamentoNovo(ciclo, 1000.0, "IV", "D1"))));
        primeira = ac.getAplicacoes().get(0);
    }

    @Test
    void aplicacaoSoFicaRealizadaComTodosOsMedicamentosConfirmados() {
        linha(doxo, 100.0, StatusPrescricao.APTO);
        linha(ciclo, 1000.0, StatusPrescricao.APTO);

        confirmarOtimizacao.executar(otimizarDia.executar(doxo.getId(), DIA).getId());
        assertThat(primeira.getStatus()).isEqualTo(StatusAplicacao.AGENDADA);
        assertThat(primeira.getDataReal()).isNull();

        confirmarOtimizacao.executar(otimizarDia.executar(ciclo.getId(), DIA).getId());
        assertThat(primeira.getStatus()).isEqualTo(StatusAplicacao.REALIZADA);
        assertThat(primeira.getDataReal()).isEqualTo(DIA);
    }

    @Test
    void linhasDaMesmaPrescricaoTemOMesmoStatus() {
        linha(doxo, 100.0, StatusPrescricao.APTO);

        assertThatThrownBy(() -> linha(ciclo, 1000.0, StatusPrescricao.NAO_APTO))
                .isInstanceOf(PrescricaoInvalidaException.class).hasMessageContaining("mesma aplicação, status e data");
    }

    @Test
    void mudarOStatusDeUmaLinhaMudaAsOutras() {
        Prescricao linhaDoxo = linha(doxo, 100.0, StatusPrescricao.APTO);
        Prescricao linhaCiclo = linha(ciclo, 1000.0, StatusPrescricao.APTO);

        atualizarPrescricao.executar(linhaDoxo.getId(), new AtualizarPrescricaoRequest(95.0, StatusPrescricao.NAO_APTO, DIA));

        assertThat(linhaCiclo.getStatus()).isEqualTo(StatusPrescricao.NAO_APTO);
        assertThat(linhaCiclo.getDoseMg()).isEqualTo(1000.0);
        assertThat(linhaDoxo.getDoseMg()).isEqualTo(95.0);
    }

    private Prescricao linha(Medicamento medicamento, double dose, StatusPrescricao status) {
        return criarPrescricao.executar(new PrescricaoRequest("PRE9999-000030", medicamento.getId(), dose, status, DIA,
                primeira.getId()));
    }

    private void frasco(Medicamento medicamento, double volume, double custo) {
        Frascos frasco = new Frascos();
        frasco.setMedicamento(medicamento);
        frasco.setVolumeMg(volume);
        frasco.setCusto(custo);
        frasco.setQuantidadeEstoque(20);
        frascosRepository.save(frasco);
    }
}
