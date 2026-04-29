package com.barbie.dreamworld_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@AllArgsConstructor
@Schema(description = "Objeto de resposta que representa uma Dreamhouse")
public class DreamhouseResponse extends RepresentationModel<DreamhouseResponse> {

    @Schema(description = "ID da Dreamhouse", example = "1")
    private Long id;

    @Schema(description = "Nome da casa", example = "Dreamhouse Malibu")
    private String nomeCasa;

    @Schema(description = "Endereço da casa", example = "Rua das Bonecas, 123")
    private String endereco;

    @Schema(description = "Cor predominante da casa", example = "Rosa")
    private String cor;

    @Schema(description = "Número de quartos da casa", example = "3")
    private Integer numeroQuartos;

    @Schema(description = "Nome da Barbie proprietária da casa", example = "Barbie Fashionista")
    private String proprietaria;
}