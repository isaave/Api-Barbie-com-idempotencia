package com.barbie.dreamworld_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Objeto utilizado para cadastrar ou atualizar uma Dreamhouse")
public class DreamhouseRequest {

    @Schema(description = "Nome da casa", example = "Dreamhouse Malibu")
    @NotBlank(message = "O nome da casa é obrigatório")
    @Size(min = 2, max = 100)
    private String nomeCasa;

    @Schema(description = "Endereço da casa", example = "Rua das Bonecas, 123")
    @Size(max = 150)
    private String endereco;

    @Schema(description = "Cor predominante da casa", example = "Rosa")
    @Size(max = 30)
    private String cor;

    @Schema(description = "Número de quartos da casa", example = "3")
    @Min(value = 1, message = "Deve ter ao menos 1 quarto")
    private Integer numeroQuartos;
}