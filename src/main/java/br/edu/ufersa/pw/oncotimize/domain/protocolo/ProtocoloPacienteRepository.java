package br.edu.ufersa.pw.oncotimize.domain.protocolo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProtocoloPacienteRepository extends JpaRepository<ProtocoloPaciente, UUID> {

    boolean existsByCodigoProtocolo(String codigoProtocolo);

    boolean existsByMedicamentosMedicamentoId(UUID medicamentoId);

    Page<ProtocoloPaciente> findDistinctByMedicamentosMedicamentoId(UUID medicamentoId, Pageable pageable);

    /** Protocolos com o medicamento e alguma aplicação agendada no período (as demais vêm junto como vizinhas). */
    @Query("""
            select distinct p from ProtocoloPaciente p join p.medicamentos m join p.aplicacoes a
            where m.medicamento.id = :medicamentoId and a.dataAgendada between :inicio and :fim""")
    List<ProtocoloPaciente> buscarComAplicacaoNoPeriodo(@Param("medicamentoId") UUID medicamentoId,
                                                        @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
