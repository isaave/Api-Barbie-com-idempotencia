package com.barbie.dreamworld_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Evento de gala ou social do multiverso Barbie.
 * Relação N:N com Barbie — tabela de junção: tb_barbies_eventos.
 *
 * Tabela: tb_eventos
 */
@Entity
@Table(name = "tb_eventos")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome do evento é obrigatório")
    @Size(min = 2, max = 100)
    @Column(name = "nome_evento", nullable = false, length = 100)
    private String nomeEvento;

    @Size(max = 255)
    @Column(length = 255)
    private String descricao;

    @NotNull(message = "A data do evento é obrigatória")
    @Column(name = "data_evento", nullable = false)
    private LocalDate dataEvento;

    @Size(max = 100)
    @Column(length = 100)
    private String local;

    /**
     * Lado inverso — Barbie é dona da tabela de junção.
     * JsonIgnore evita loop Evento → Barbie → Evento.
     */
    @ManyToMany(mappedBy = "eventos", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Barbie> barbies = new ArrayList<>();
}
