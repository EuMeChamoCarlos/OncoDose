package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import lombok.Data;
import jakarta.persistence.*;
import java.util.UUID;
import java.util.List;

@Data
@Entity
@Table(name = "medicamento")
public class Medicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(name = "codigo_interno", unique = true)
    private String codigoInterno;

    @OneToMany(mappedBy = "medicamento", cascade = CascadeType.ALL)
    private List<Frascos> apresentacoes;

    @OneToMany(mappedBy = "medicamento", cascade = CascadeType.ALL)
    private List<Prescricao> prescricoes;


}
