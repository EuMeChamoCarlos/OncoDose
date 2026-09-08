package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface OtimizacaoRepository extends JpaRepository<Otimizacao, UUID> {

    List<Otimizacao> findByMedicamentoIdAndDataReferenciaOrderByCreatedAtDesc(UUID medicamentoId, LocalDate dataReferencia);
}
