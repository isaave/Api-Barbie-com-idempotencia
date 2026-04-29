package com.barbie.dreamworld_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade central do sistema.
 */
@Entity
@Table(name = "tb_barbies")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Barbie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome da Barbie é obrigatório")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "A coleção é obrigatória")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String colecao;

    // ── Relacionamentos ───────────────────────────────────────────────────────

    /** * 1:1 — Barbie é dona da FK
     * REMOVIDO: cascade = CascadeType.ALL
     * MOTIVO: Evitar erro 'Detached entity passed to persist' ao usar casas já salvas no LoadDatabase.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dreamhouse_id")
    private Dreamhouse dreamhouse;

    /** N:1 — Várias Barbies podem ter a mesma Profissão */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissao_id")
    private Profissao profissao;

    /** * 1:N — Acessórios pertencem a esta Barbie
     * Aqui o cascade ALL faz sentido, pois o acessório não existe sem a Barbie.
     */
    @OneToMany(mappedBy = "barbie", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Acessorio> acessorios = new ArrayList<>();

    /** N:N — Barbie é dona da tabela de junção */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_barbies_eventos",
            joinColumns        = @JoinColumn(name = "barbie_id"),
            inverseJoinColumns = @JoinColumn(name = "evento_id")
    )
    private List<Evento> eventos = new ArrayList<>();
}