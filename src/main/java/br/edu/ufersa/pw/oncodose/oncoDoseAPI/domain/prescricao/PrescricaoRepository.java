package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao;

import java.time.LocalDate;
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

    boolean existsByCodigoPrescricao(String codigoPrescricao);

    boolean existsByMedicamentoId(UUID medicamentoId);

    boolean existsByAplicacaoId(UUID aplicacaoId);

    boolean existsByAplicacaoCicloId(UUID cicloId);

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
