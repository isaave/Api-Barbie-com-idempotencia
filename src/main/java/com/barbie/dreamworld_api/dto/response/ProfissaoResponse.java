package com.barbie.dreamworld_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@AllArgsConstructor
@Schema(description = "Objeto de resposta que representa uma profissão")
public class ProfissaoResponse extends RepresentationModel<ProfissaoResponse> {

    @Schema(description = "ID da profissão", example = "1")
    private Long id;

    @Schema(description = "Nome da profissão", example = "Engenheira de Software")
    private String nomeProfissao;

    @Schema(description = "Descrição da profissão", example = "Responsável pelo desenvolvimento de sistemas e aplicações")
    private String descricao;

    @Schema(description = "Área de atuação da profissão", example = "Tecnologia")
    private String areaAtuacao;

    @Schema(description = "Quantidade total de Barbies associadas a essa profissão", example = "4")
    private int totalBarbies;
}