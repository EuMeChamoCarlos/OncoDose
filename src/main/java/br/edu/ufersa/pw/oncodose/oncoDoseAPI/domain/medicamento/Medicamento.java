package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.MedicamentoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade rica: sem setters públicos, nascimento blindado via Builder + invariantes,
 * mudanças só por métodos de comportamento.
 */
@Getter
@Entity
@Table(name = "medicamento")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private Medicamento(Builder builder) {
        this.nome = builder.nome;
        this.codigoInterno = builder.codigoInterno;
    }

    /** Intenção de negócio: renomear, nunca set genérico. */
    public void renomear(String novoNome) {
        if (novoNome == null || novoNome.isBlank()) {
            throw new MedicamentoInvalidoException("nome", "obrigatório");
        }
        this.nome = novoNome.trim();
    }

    /** Intenção de negócio: trocar código interno (branco vira null p/ não quebrar unicidade). */
    public void trocarCodigo(String novoCodigo) {
        if (novoCodigo == null || novoCodigo.isBlank()) {
            this.codigoInterno = null;
            return;
        }
        this.codigoInterno = novoCodigo.trim();
    }

    public static Builder builder(String nome) {
        return new Builder(nome);
    }

    public static class Builder {
        private String nome;
        private String codigoInterno;

        public Builder(String nome) {
            this.nome = nome;
        }

        public Builder codigoInterno(String codigoInterno) {
            this.codigoInterno = codigoInterno;
            return this;
        }

        public Medicamento build() {
            validarInvariantes();
            return new Medicamento(this);
        }

        private void validarInvariantes() {
            if (nome == null || nome.isBlank()) {
                throw new MedicamentoInvalidoException("nome", "obrigatório");
            }
            nome = nome.trim();
            if (codigoInterno != null && codigoInterno.isBlank()) {
                codigoInterno = null;
            } else if (codigoInterno != null) {
                codigoInterno = codigoInterno.trim();
            }
        }
    }
}
