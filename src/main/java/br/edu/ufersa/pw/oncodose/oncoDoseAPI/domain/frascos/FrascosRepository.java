package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FrascosRepository extends JpaRepository<Frascos, UUID> {

    Page<Frascos> findByMedicamentoId(UUID medicamentoId, Pageable pageable);

    /** Tipos j de uma instância do modelo: todas as apresentações do medicamento. */
    List<Frascos> findByMedicamentoId(UUID medicamentoId);

    /**
     * Baixa atômica no banco: só desconta se houver estoque. Retorna 0 se não havia
     * (nada muda). Um UPDATE único evita perder baixas de confirmações simultâneas.
     */
    @Modifying
    @Query("update Frascos f set f.quantidadeEstoque = f.quantidadeEstoque - :quantidade "
            + "where f.id = :id and f.quantidadeEstoque >= :quantidade")
    int baixarEstoque(@Param("id") UUID id, @Param("quantidade") int quantidade);

    Optional<Frascos> findByIdAndMedicamentoId(UUID id, UUID medicamentoId);
}
