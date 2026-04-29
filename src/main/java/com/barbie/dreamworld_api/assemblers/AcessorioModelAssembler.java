package com.barbie.dreamworld_api.assemblers;

import com.barbie.dreamworld_api.controllers.AcessorioController;
import com.barbie.dreamworld_api.controllers.BarbieController;
import com.barbie.dreamworld_api.dto.response.AcessorioResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Links fixos: self, update, delete, collection, proprietaria.
 * Condicional: raridade e categoria → links para outros do mesmo tipo.
 */
@Component
public class AcessorioModelAssembler
        extends RepresentationModelAssemblerSupport<AcessorioResponse, AcessorioResponse> {

    public AcessorioModelAssembler() {
        super(AcessorioController.class, AcessorioResponse.class);
    }

    @Override
    public AcessorioResponse toModel(AcessorioResponse r) {
        r.add(
            linkTo(methodOn(AcessorioController.class).getById(r.getId())).withSelfRel(),
            linkTo(methodOn(AcessorioController.class).update(r.getId(), null)).withRel("update"),
            linkTo(methodOn(AcessorioController.class).delete(r.getId())).withRel("delete"),
            linkTo(methodOn(AcessorioController.class).listAll(null, null)).withRel("collection"),
            linkTo(methodOn(BarbieController.class).getById(r.getBarbieId())).withRel("proprietaria")
        );

        if (r.getRaridade() != null) {
            r.add(linkTo(methodOn(AcessorioController.class)
                    .listByRaridade(r.getRaridade(), null, null)).withRel("mesma-raridade"));
        }

        if (r.getCategoria() != null) {
            r.add(linkTo(methodOn(AcessorioController.class)
                    .listByCategoria(r.getCategoria(), null, null)).withRel("mesma-categoria"));
        }

        return r;
    }
}
