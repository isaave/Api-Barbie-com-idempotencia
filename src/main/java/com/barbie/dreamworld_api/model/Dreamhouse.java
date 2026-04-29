package com.barbie.dreamworld_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Residência oficial de uma Barbie.
 * Relação 1:1 com Barbie — a FK dreamhouse_id fica na tabela tb_barbies.
 *
 * Tabela: tb_dreamhouses
 */
@Entity
@Table(name = "tb_dreamhouses")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Dreamhouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome da casa é obrigatório")
    @Size(min = 2, max = 100)
    @Column(name = "nome_casa", nullable = false, length = 100)
    private String nomeCasa;

    @Size(max = 150)
    @Column(length = 150)
    private String endereco;

    @Size(max = 30)
    @Column(length = 30)
    private String cor;

    @Min(value = 1, message = "A casa deve ter ao menos 1 quarto")
    @Column(name = "numero_quartos")
    private Integer numeroQuartos;

    /**
     * Lado inverso — FK está em Barbie.
     * JsonIgnore evita loop Dreamhouse → Barbie → Dreamhouse.
     */
    @OneToOne(mappedBy = "dreamhouse", fetch = FetchType.LAZY)
    @JsonIgnore
    private Barbie barbie;
}
