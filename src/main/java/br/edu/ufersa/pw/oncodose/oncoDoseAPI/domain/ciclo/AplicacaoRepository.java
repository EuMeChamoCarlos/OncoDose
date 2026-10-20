package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AplicacaoRepository extends JpaRepository<Aplicacao, UUID> {
}
