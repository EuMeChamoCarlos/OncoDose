package br.edu.ufersa.pw.oncotimize.application.usecases.frascos;

import br.edu.ufersa.pw.oncotimize.application.api.frascos.dto.FrascoRequest;
import br.edu.ufersa.pw.oncotimize.domain.exception.FrascoIndisponivelException;
import br.edu.ufersa.pw.oncotimize.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncotimize.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncotimize.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncotimize.domain.otimizacao.AlocacaoFrascoRepository;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: editar frasco. Depois de usado numa otimização, volume e custo ficam
 * congelados (o histórico do TCC depende deles); só o estoque pode mudar.
 */
@Service
public class AtualizarFrascoUseCase {

    private final FrascosRepository frascosRepository;
    private final AlocacaoFrascoRepository alocacaoFrascoRepository;

    public AtualizarFrascoUseCase(
            FrascosRepository frascosRepository,
            AlocacaoFrascoRepository alocacaoFrascoRepository) {
        this.frascosRepository = frascosRepository;
        this.alocacaoFrascoRepository = alocacaoFrascoRepository;
    }

    @Transactional
    public Frascos executar(UUID medicamentoId, UUID frascoId, FrascoRequest request) {
        Frascos frasco = frascosRepository.findByIdAndMedicamentoId(frascoId, medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Frasco", frascoId));

        boolean mudaModelo = !Objects.equals(request.getVolumeMg(), frasco.getVolumeMg())
                || !Objects.equals(request.getCusto(), frasco.getCusto());
        if (mudaModelo && alocacaoFrascoRepository.existsByApresentacaoId(frascoId)) {
            throw new FrascoIndisponivelException(frascoId,
                    "já usado em otimização; só o estoque pode mudar (cadastre outro frasco para novo volume/custo)");
        }

        frasco.setVolumeMg(request.getVolumeMg());
        frasco.setCusto(request.getCusto());
        frasco.setQuantidadeEstoque(request.getQuantidadeEstoque());
        frasco.setDescricaoTasy(request.getDescricaoTasy());
        return frasco;
    }
}
