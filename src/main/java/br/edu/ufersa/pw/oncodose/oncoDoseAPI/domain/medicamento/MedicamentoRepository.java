package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, UUID> {

    Optional<Medicamento> findByNome(String nome);

    boolean existsByNome(String nome);

    Optional<Medicamento> findByCodigoInterno(String codigoInterno);

    boolean existsByCodigoInterno(String codigoInterno);
}
