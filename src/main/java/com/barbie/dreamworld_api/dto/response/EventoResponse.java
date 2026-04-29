package com.barbie.dreamworld_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Schema(description = "Objeto de resposta que representa um evento")
public class EventoResponse extends RepresentationModel<EventoResponse> {

    @Schema(description = "ID do evento", example = "1")
    private Long id;

    @Schema(description = "Nome do evento", example = "Baile Rosa")
    private String nomeEvento;

    @Schema(description = "Descrição do evento", example = "Evento de gala no mundo Barbie")
    private String descricao;

    @Schema(description = "Data do evento (formato YYYY-MM-DD)", example = "2026-05-10")
    private LocalDate dataEvento;

    @Schema(description = "Local onde o evento será realizado", example = "Dream House")
    private String local;

    @Schema(description = "Quantidade total de participantes no evento", example = "10")
    private int totalParticipantes;
}