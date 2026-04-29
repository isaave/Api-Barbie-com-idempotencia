package com.barbie.dreamworld_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Objeto utilizado para cadastrar ou atualizar uma Barbie")
public class BarbieRequest {

    @Schema(description = "Nome da Barbie", example = "Barbie Cientista")
    @NotBlank(message = "O nome da Barbie é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;

    @Schema(description = "Coleção à qual a Barbie pertence", example = "Barbie Careers")
    @NotBlank(message = "A coleção é obrigatória")
    @Size(min = 2, max = 100)
    private String colecao;

    @Schema(description = "ID da profissão associada à Barbie", example = "1")
    private Long profissaoId;

    @Schema(description = "ID da Dreamhouse associada à Barbie", example = "2")
    private Long dreamhouseId;
}