package com.barbie.dreamworld_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Acessório colecionável de uma Barbie (bolsa, sapato, chapéu, etc.).
 * Relação N:1 com Barbie — FK barbie_id em tb_acessorios.
 *
 * Tabela: tb_acessorios
 */
@Entity
@Table(name = "tb_acessorios")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Acessorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome do item é obrigatório")
    @Size(min = 2, max = 100)
    @Column(name = "nome_item", nullable = false, length = 100)
    private String nomeItem;

    @Enumerated(EnumType.STRING)
    private Raridade raridade;

    @Positive(message = "O preço deve ser positivo")
    private Double preco;

    @Size(max = 50)
    @Column(length = 50)
    private String categoria;

    /**
     * Barbie proprietária do acessório.
     * JsonIgnore evita loop Acessorio → Barbie → acessorios → Acessorio.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbie_id", nullable = false)
    @JsonIgnore
    private Barbie barbie;
}
