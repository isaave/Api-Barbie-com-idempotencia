package com.barbie.dreamworld_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Representa uma carreira/profissão que uma Barbie pode exercer.
 * Relação 1:N com Barbie — várias Barbies podem compartilhar a mesma profissão.
 *
 * Tabela: tb_profissoes
 */
@Entity
@Table(name = "tb_profissoes")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Profissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome da profissão é obrigatório")
    @Size(min = 2, max = 80)
    @Column(name = "nome_profissao", nullable = false, length = 80)
    private String nomeProfissao;

    @Size(max = 255)
    @Column(length = 255)
    private String descricao;

    @Size(max = 50)
    @Column(name = "area_atuacao", length = 50)
    private String areaAtuacao;

    /**
     * Lado inverso — FK está em Barbie.
     * JsonIgnore evita loop Profissao → Barbie → Profissao na serialização.
     */
    @OneToMany(mappedBy = "profissao", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Barbie> barbies = new ArrayList<>();
}
