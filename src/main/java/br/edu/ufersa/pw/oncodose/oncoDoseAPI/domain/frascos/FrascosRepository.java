package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrascosRepository extends JpaRepository<Frascos, UUID> {

    Page<Frascos> findByMedicamentoId(UUID medicamentoId, Pageable pageable);

    Optional<Frascos> findByIdAndMedicamentoId(UUID id, UUID medicamentoId);
}
