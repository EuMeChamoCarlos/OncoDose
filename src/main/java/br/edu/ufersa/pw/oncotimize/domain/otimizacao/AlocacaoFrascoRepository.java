package br.edu.ufersa.pw.oncotimize.domain.otimizacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlocacaoFrascoRepository extends JpaRepository<AlocacaoFrasco, UUID> {

    List<AlocacaoFrasco> findByOtimizacaoId(UUID otimizacaoId);

    boolean existsByPrescricaoId(UUID prescricaoId);

    /** A linha da prescrição já teve o preparo confirmado (otimização CONFIRMADA). */
    boolean existsByPrescricaoIdAndOtimizacaoStatus(UUID prescricaoId, StatusOtimizacao status);

    boolean existsByApresentacaoId(UUID apresentacaoId);
}
