package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CicloRepository extends JpaRepository<Ciclo, UUID> {

    boolean existsByCodigoCiclo(String codigoCiclo);

    boolean existsByMedicamentoId(UUID medicamentoId);

    Page<Ciclo> findByMedicamentoId(UUID medicamentoId, Pageable pageable);

    /** Ciclos do medicamento com alguma aplicação agendada no período (as demais vêm junto como vizinhas). */
    @Query("""
            select distinct c from Ciclo c join c.aplicacoes a
            where c.medicamento.id = :medicamentoId and a.dataAgendada between :inicio and :fim""")
    List<Ciclo> buscarComAplicacaoNoPeriodo(@Param("medicamentoId") UUID medicamentoId,
                                            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
