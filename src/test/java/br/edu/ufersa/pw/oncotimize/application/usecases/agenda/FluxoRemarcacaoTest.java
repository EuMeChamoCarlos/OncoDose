package br.edu.ufersa.pw.oncotimize.application.usecases.agenda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import br.edu.ufersa.pw.oncotimize.application.api.prescricao.dto.PrescricaoRequest;
import br.edu.ufersa.pw.oncotimize.application.usecases.otimizacao.ConfirmarOtimizacaoUseCase;
import br.edu.ufersa.pw.oncotimize.application.usecases.otimizacao.OtimizarDiaUseCase;
import br.edu.ufersa.pw.oncotimize.application.usecases.otimizacao.RecusarOtimizacaoUseCase;
import br.edu.ufersa.pw.oncotimize.application.usecases.prescricao.CriarPrescricaoUseCase;
import br.edu.ufersa.pw.oncotimize.domain.agenda.SugestaoRemarcacao;
import br.edu.ufersa.pw.oncotimize.domain.exception.EstadoInvalidoException;
import br.edu.ufersa.pw.oncotimize.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncotimize.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncotimize.domain.otimizacao.Otimizacao;
import br.edu.ufersa.pw.oncotimize.domain.otimizacao.StatusOtimizacao;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.StatusPrescricao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPaciente;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPaciente.MedicamentoNovo;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPacienteRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integração com o Postgres local. Paciente não apto em 03/03/2031 (Paclitaxel 175 mg) pode voltar a partir de
 * 10/03; em 12/03 há outro paciente de 260 mg. Frascos 300 mg US$ 190 e 100 mg US$ 70, 30 mg US$ 25.
 * Sozinho, 175 mg custa US$ 140 (2×100); junto com 260 mg, o dia passa de 190 para 310 (300+100+30+30):
 * custo marginal 120, economia 20. @Transactional desfaz tudo; datas em 2031 e códigos 9999.
 */
@SpringBootTest
@Transactional
class FluxoRemarcacaoTest {

    private static final LocalDate ORIGEM = LocalDate.of(2031, 3, 3);
    private static final LocalDate MINIMA = LocalDate.of(2031, 3, 10);
    private static final LocalDate GRUPO = LocalDate.of(2031, 3, 12);

    @Autowired private MedicamentoRepository medicamentoRepository;
    @Autowired private FrascosRepository frascosRepository;
    @Autowired private ProtocoloPacienteRepository protocoloRepository;
    @Autowired private SugerirRemarcacaoNaoAptoUseCase sugerirRemarcacao;
    @Autowired private AceitarSugestaoUseCase aceitarSugestao;
    @Autowired private RecusarSugestaoUseCase recusarSugestao;
    @Autowired private CriarPrescricaoUseCase criarPrescricao;
    @Autowired private OtimizarDiaUseCase otimizarDia;
    @Autowired private RecusarOtimizacaoUseCase recusarOtimizacao;
    @Autowired private ConfirmarOtimizacaoUseCase confirmarOtimizacao;

    private Medicamento pac;
    private Aplicacao naoApto;
    private Aplicacao doGrupo;

    @BeforeEach
    void cenario() {
        pac = medicamentoRepository.save(Medicamento.builder("Teste remarcação pac").build());
        frasco(300, 190);
        frasco(100, 70);
        frasco(30, 25);
        naoApto = protocolo("PTC9999-000040", ORIGEM, 175.0).getAplicacoes().get(0);
        doGrupo = protocolo("PTC9999-000041", GRUPO, 260.0).getAplicacoes().get(0);
    }

    @Test
    void naoAptoVoltaNoDiaQueDivideFrascoEAceiteRemarca() {
        SugestaoRemarcacao s = sugerirRemarcacao.executar(naoApto.getId(), MINIMA, "imunidade baixa");

        assertThat(s.getTipo()).isEqualTo(SugestaoRemarcacao.Tipo.NAO_APTO);
        assertThat(s.getDataOrigem()).isEqualTo(ORIGEM);
        assertThat(s.getDataDestino()).isEqualTo(GRUPO);
        assertThat(s.getEconomiaEstimada().doubleValue()).isCloseTo(20, within(1e-6));
        assertThat(naoApto.getDataAgendada()).isEqualTo(ORIGEM);

        aceitarSugestao.executar(s.getId(), "enfermeira@teste", null);

        assertThat(s.getStatus()).isEqualTo(SugestaoRemarcacao.Status.ACEITA);
        assertThat(s.getDecididaPor()).isEqualTo("enfermeira@teste");
        assertThat(naoApto.getDataAgendada()).isEqualTo(GRUPO);
        assertThat(naoApto.getDataPrevista()).isEqualTo(ORIGEM);
        assertThat(naoApto.getMotivoAlteracao()).contains("imunidade baixa");
        assertThatThrownBy(() -> recusarSugestao.executar(s.getId(), "enfermeira@teste", null))
                .isInstanceOf(EstadoInvalidoException.class).hasMessageContaining("ACEITA");
    }

    @Test
    void recusaGuardaQuemEMotivoENaoMudaAData() {
        SugestaoRemarcacao s = sugerirRemarcacao.executar(naoApto.getId(), MINIMA, "imunidade baixa");

        recusarSugestao.executar(s.getId(), "enfermeira@teste", "paciente pediu outro dia");

        assertThat(s.getStatus()).isEqualTo(SugestaoRemarcacao.Status.RECUSADA);
        assertThat(s.getMotivoDecisao()).isEqualTo("paciente pediu outro dia");
        assertThat(naoApto.getDataAgendada()).isEqualTo(ORIGEM);
    }

    @Test
    void novaSugestaoExpiraAPendenteAnterior() {
        SugestaoRemarcacao primeira = sugerirRemarcacao.executar(naoApto.getId(), MINIMA, "imunidade baixa");
        sugerirRemarcacao.executar(naoApto.getId(), MINIMA.plusDays(1), "imunidade baixa");

        assertThat(primeira.getStatus()).isEqualTo(SugestaoRemarcacao.Status.EXPIRADA);
    }

    @Test
    void planoRecusadoNaoPodeSerConfirmado() {
        criarPrescricao.executar(new PrescricaoRequest("PRE9999-000041", pac.getId(), 260.0, StatusPrescricao.APTO, GRUPO,
                doGrupo.getId()));
        Otimizacao plano = otimizarDia.executar(pac.getId(), GRUPO);

        recusarOtimizacao.executar(plano.getId(), "farmaceutico@teste", "frasco com lote vencendo");

        assertThat(plano.getStatus()).isEqualTo(StatusOtimizacao.RECUSADA);
        assertThat(plano.getRecusadaPor()).isEqualTo("farmaceutico@teste");
        assertThat(plano.getMotivoRecusa()).isEqualTo("frasco com lote vencendo");
        assertThatThrownBy(() -> confirmarOtimizacao.executar(plano.getId()))
                .isInstanceOf(EstadoInvalidoException.class).hasMessageContaining("recusado");
    }

    private ProtocoloPaciente protocolo(String codigo, LocalDate inicio, double dose) {
        return protocoloRepository.save(ProtocoloPaciente.novo(codigo, "TAX", null, inicio, 1, 21, 0, 0, 0,
                List.of(new MedicamentoNovo(pac, dose, "IV", "D1"))));
    }

    private void frasco(double volume, double custo) {
        Frascos frasco = new Frascos();
        frasco.setMedicamento(pac);
        frasco.setVolumeMg(volume);
        frasco.setCusto(custo);
        frasco.setQuantidadeEstoque(20);
        frascosRepository.save(frasco);
    }
}
