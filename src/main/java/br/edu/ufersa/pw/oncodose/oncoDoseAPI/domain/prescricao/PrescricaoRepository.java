package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescricaoRepository
        extends JpaRepository<Prescricao, UUID>, JpaSpecificationExecutor<Prescricao> {

    boolean existsByCodigoPrescricao(String codigoPrescricao);

    boolean existsByMedicamentoId(UUID medicamentoId);

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
