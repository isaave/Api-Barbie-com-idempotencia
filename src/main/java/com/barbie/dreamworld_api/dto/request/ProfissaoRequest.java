package com.barbie.dreamworld_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Objeto utilizado para cadastrar ou atualizar uma profissão")
public class ProfissaoRequest {

    @Schema(description = "Nome da profissão", example = "Engenheira de Software")
    @NotBlank(message = "O nome da profissão é obrigatório")
    @Size(min = 2, max = 80)
    private String nomeProfissao;

    @Schema(description = "Descrição da profissão", example = "Responsável pelo desenvolvimento de sistemas e aplicações")
    @Size(max = 255)
    private String descricao;

    @Schema(description = "Área de atuação da profissão", example = "Tecnologia")
    @Size(max = 50)
    private String areaAtuacao;
}