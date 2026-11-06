package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescricaoRepository
        extends JpaRepository<Prescricao, UUID>, JpaSpecificationExecutor<Prescricao> {

    boolean existsByCodigoPrescricaoAndMedicamentoId(String codigoPrescricao, UUID medicamentoId);

    boolean existsByMedicamentoId(UUID medicamentoId);

    boolean existsByAplicacaoIdAndMedicamentoId(UUID aplicacaoId, UUID medicamentoId);

    boolean existsByAplicacaoProtocoloId(UUID protocoloId);

    /**
     * Pacientes i de uma instância do modelo: prescrições do medicamento com o status pedido cujo
     * dia de preparo é {@code data} — a data agendada da aplicação ou, no legado sem aplicação,
     * a data da prescrição. Ordem por código: o plano sai igual a cada execução.
     */
    @Query("""
            select p from Prescricao p left join p.aplicacao a
            where p.medicamento.id = :medicamentoId and p.status = :status
              and ((a is not null and a.dataAgendada = :data) or (a is null and p.dataPrescricao = :data))
            order by p.codigoPrescricao""")
    List<Prescricao> buscarDoDia(@Param("medicamentoId") UUID medicamentoId, @Param("data") LocalDate data,
                                 @Param("status") StatusPrescricao status);

    List<Prescricao> findByAplicacaoIdInAndMedicamentoId(Collection<UUID> aplicacaoIds, UUID medicamentoId);

    /** Legado (sem aplicação) no período: demanda fixa do dia para o planejador da agenda. */
    @Query("""
            select p from Prescricao p
            where p.medicamento.id = :medicamentoId and p.status = :status and p.aplicacao is null
              and p.dataPrescricao between :inicio and :fim""")
    List<Prescricao> buscarLegadoNoPeriodo(@Param("medicamentoId") UUID medicamentoId, @Param("inicio") LocalDate inicio,
                                           @Param("fim") LocalDate fim, @Param("status") StatusPrescricao status);

    /**
     * Filtros opcionais: null ignora o critério. Specification em vez de
     * "(:p is null or ...)", que o Postgres rejeita (tipo do parâmetro indeterminado).
     */
    default Page<Prescricao> buscar(LocalDate data, UUID medicamentoId, Pageable pageable) {
        Specification<Prescricao> filtro = (root, query, cb) -> cb.and(
                data == null ? cb.conjunction() : cb.equal(root.get("dataPrescricao"), data),
                medicamentoId == null ? cb.conjunction() : cb.equal(root.get("medicamento").get("id"), medicamentoId));
        return findAll(filtro, pageable);
    }
}
