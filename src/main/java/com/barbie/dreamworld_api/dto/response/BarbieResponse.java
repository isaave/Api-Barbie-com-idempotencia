package com.barbie.dreamworld_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * Estende RepresentationModel para suportar links HATEOAS.
 * Links adicionados pelo BarbieModelAssembler.
 */
@Getter
@AllArgsConstructor
@Schema(description = "Objeto de resposta que representa uma Barbie com seus dados e relacionamentos")
public class BarbieResponse extends RepresentationModel<BarbieResponse> {

    @Schema(description = "ID da Barbie", example = "1")
    private Long id;

    @Schema(description = "Nome da Barbie", example = "Barbie Cientista")
    private String nome;

    @Schema(description = "Coleção da Barbie", example = "Barbie Careers")
    private String colecao;

    @Schema(description = "Nome da profissão da Barbie", example = "Engenheira de Software")
    private String profissao;

    @Schema(description = "Área de atuação da profissão", example = "Tecnologia")
    private String areaAtuacao;

    @Schema(description = "Nome da Dreamhouse da Barbie", example = "Dreamhouse Malibu")
    private String dreamhouse;

    @Schema(description = "Quantidade total de acessórios da Barbie", example = "5")
    private int totalAcessorios;

    @Schema(description = "Quantidade total de eventos da Barbie", example = "3")
    private int totalEventos;
}