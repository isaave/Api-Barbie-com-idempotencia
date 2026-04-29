package com.barbie.dreamworld_api.dto.request;

import com.barbie.dreamworld_api.model.Raridade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Objeto utilizado para cadastrar ou atualizar um acessório")
public class AcessorioRequest {

    @Schema(description = "Nome do acessório", example = "Bolsa Rosa")
    @NotBlank(message = "O nome do item é obrigatório")
    @Size(min = 2, max = 100)
    private String nomeItem;

    @Schema(
            description = "Nível de raridade do acessório",
            example = "RARO"
    )
    private Raridade raridade;

    @Schema(description = "Preço do acessório", example = "99.90")
    @Positive(message = "O preço deve ser positivo")
    private Double preco;

    @Schema(description = "Categoria do acessório", example = "Moda")
    @Size(max = 50)
    private String categoria;

    @Schema(description = "ID da Barbie associada ao acessório", example = "1")
    @NotNull(message = "O ID da Barbie é obrigatório")
    private Long barbieId;
}