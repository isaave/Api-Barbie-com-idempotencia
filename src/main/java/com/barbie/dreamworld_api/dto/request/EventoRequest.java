package com.barbie.dreamworld_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@Schema(description = "Objeto utilizado para cadastrar ou atualizar um evento")
public class EventoRequest {

    @Schema(description = "Nome do evento", example = "Baile Rosa")
    @NotBlank(message = "O nome do evento é obrigatório")
    @Size(min = 2, max = 100)
    private String nomeEvento;

    @Schema(description = "Descrição do evento", example = "Evento de gala no mundo Barbie")
    @Size(max = 255)
    private String descricao;

    @Schema(description = "Data do evento (formato YYYY-MM-DD)", example = "2026-05-10")
    @NotNull(message = "A data do evento é obrigatória")
    private LocalDate dataEvento;

    @Schema(description = "Local onde o evento será realizado", example = "Dream House")
    @Size(max = 100)
    private String local;
}