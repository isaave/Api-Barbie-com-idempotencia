package com.barbie.dreamworld_api.dto.response;

import com.barbie.dreamworld_api.model.Raridade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@AllArgsConstructor
@Schema(description = "Objeto de resposta que representa um acessório")
public class AcessorioResponse extends RepresentationModel<AcessorioResponse> {

    @Schema(description = "ID do acessório", example = "1")
    private Long id;

    @Schema(description = "Nome do acessório", example = "Bolsa Rosa")
    private String nomeItem;

    @Schema(description = "Nível de raridade do acessório", example = "RARO")
    private Raridade raridade;

    @Schema(description = "Preço do acessório", example = "99.90")
    private Double preco;

    @Schema(description = "Categoria do acessório", example = "Moda")
    private String categoria;

    @Schema(description = "Nome da Barbie proprietária do acessório", example = "Barbie Fashionista")
    private String proprietaria;

    @Schema(description = "ID da Barbie associada ao acessório", example = "1")
    private Long barbieId;
}