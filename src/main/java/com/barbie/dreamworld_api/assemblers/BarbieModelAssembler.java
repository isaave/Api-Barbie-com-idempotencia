package com.barbie.dreamworld_api.assemblers;

import com.barbie.dreamworld_api.controllers.*;
import com.barbie.dreamworld_api.dto.response.BarbieResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class BarbieModelAssembler
        extends RepresentationModelAssemblerSupport<BarbieResponse, BarbieResponse> {

    public BarbieModelAssembler() {
        super(BarbieController.class, BarbieResponse.class);
    }

    @Override
    public BarbieResponse toModel(BarbieResponse r) {

        // ── Links fixos ───────────────────────────────────────────────────────
        r.add(
                linkTo(methodOn(BarbieController.class).getById(r.getId())).withSelfRel(),

                linkTo(methodOn(BarbieController.class).update(r.getId(), null)).withRel("update"),

                linkTo(methodOn(BarbieController.class).delete(r.getId())).withRel("delete"),

                linkTo(methodOn(BarbieController.class).listAll(null, null)).withRel("collection"),

                // CORREÇÃO: Passando o ID e os dois 'null' para a paginação
                linkTo(methodOn(AcessorioController.class).listByBarbie(r.getId(), null, null)).withRel("acessorios"),

                // CORREÇÃO: Passando o ID e os dois 'null' para a paginação
                linkTo(methodOn(EventoController.class).listByBarbie(r.getId(), null, null)).withRel("eventos")
        );

        // ── Links condicionais — estado da Dreamhouse ─────────────────────────
        if (r.getDreamhouse() != null) {
            r.add(linkTo(methodOn(DreamhouseController.class)
                    .getByBarbie(r.getId())).withRel("dreamhouse"));
        } else {
            // CORREÇÃO: Passando os dois 'null' exigidos pela paginação
            r.add(linkTo(methodOn(DreamhouseController.class)
                    .listSemProprietaria(null, null)).withRel("associar-dreamhouse"));
        }

        // ── Links condicionais — estado da Profissão ──────────────────────────
        if (r.getProfissao() != null) {
            r.add(linkTo(methodOn(BarbieController.class)
                    .listByProfissao(null, null, null)).withRel("colegas-de-profissao"));
        } else {
            r.add(linkTo(methodOn(ProfissaoController.class)
                    .listAll(null, null)).withRel("associar-profissao"));
        }

        return r;
    }
}