package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrascosRepository extends JpaRepository<Frascos, UUID> {
}
