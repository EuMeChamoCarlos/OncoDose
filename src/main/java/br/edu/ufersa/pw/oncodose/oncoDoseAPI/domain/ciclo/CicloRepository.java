package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CicloRepository extends JpaRepository<Ciclo, UUID> {

    boolean existsByCodigoCiclo(String codigoCiclo);

    boolean existsByMedicamentoId(UUID medicamentoId);

    Page<Ciclo> findByMedicamentoId(UUID medicamentoId, Pageable pageable);
}
