package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultas de frasco (somente leitura).
 */
@Service
public class BuscarFrascoUseCase {

    private final FrascosRepository frascosRepository;

    public BuscarFrascoUseCase(FrascosRepository frascosRepository) {
        this.frascosRepository = frascosRepository;
    }

    @Transactional(readOnly = true)
    public Page<Frascos> listar(Pageable pageable) {
        return frascosRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Frascos porId(UUID frascoId) {
        return frascosRepository.findById(frascoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Frasco", frascoId));
    }
}
