package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.agenda;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SugestaoRemarcacaoRepository extends JpaRepository<SugestaoRemarcacao, UUID> {

    List<SugestaoRemarcacao> findByAplicacaoIdAndStatus(UUID aplicacaoId, SugestaoRemarcacao.Status status);

    Page<SugestaoRemarcacao> findByStatus(SugestaoRemarcacao.Status status, Pageable pageable);

    /** Uma sugestão nova ou aceita torna obsoletas as outras pendentes da mesma aplicação. */
    default void expirarPendentes(UUID aplicacaoId, UUID exceto) {
        findByAplicacaoIdAndStatus(aplicacaoId, SugestaoRemarcacao.Status.SUGERIDA).stream()
                .filter(s -> !s.getId().equals(exceto))
                .forEach(SugestaoRemarcacao::expirar);
    }
}
