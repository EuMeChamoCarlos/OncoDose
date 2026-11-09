package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtimizacaoRepository extends JpaRepository<Otimizacao, UUID> {

    List<Otimizacao> findByMedicamentoIdAndDataReferenciaOrderByCreatedAtDesc(UUID medicamentoId, LocalDate dataReferencia);

    /** Trava a linha (SELECT ... FOR UPDATE) até o fim da transação: serializa confirmações. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Otimizacao> findWithLockById(UUID id);

    boolean existsByMedicamentoIdAndDataReferenciaAndStatus(
            UUID medicamentoId, LocalDate dataReferencia, StatusOtimizacao status);

    /** Dias do período com preparo já feito (status CONFIRMADA): o planejador não manda ninguém para eles. */
    @Query("""
            select o.dataReferencia from Otimizacao o
            where o.medicamento.id = :medicamentoId and o.status = :status
              and o.dataReferencia between :inicio and :fim""")
    List<LocalDate> buscarDiasNoPeriodo(@Param("medicamentoId") UUID medicamentoId, @Param("inicio") LocalDate inicio,
                                        @Param("fim") LocalDate fim, @Param("status") StatusOtimizacao status);
}
