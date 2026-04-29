package com.barbie.dreamworld_api.assemblers;

import com.barbie.dreamworld_api.controllers.BarbieController;
import com.barbie.dreamworld_api.controllers.DreamhouseController;
import com.barbie.dreamworld_api.dto.response.DreamhouseResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Links fixos: self, update, delete, collection.
 * Condicional: se tem proprietária → link para a Barbie dona;
 *              se não tem → link para Barbies sem casa.
 */
@Component
public class DreamhouseModelAssembler
        extends RepresentationModelAssemblerSupport<DreamhouseResponse, DreamhouseResponse> {

    public DreamhouseModelAssembler() {
        super(DreamhouseController.class, DreamhouseResponse.class);
    }

    @Override
    public DreamhouseResponse toModel(DreamhouseResponse r) {
        r.add(
            linkTo(methodOn(DreamhouseController.class).getById(r.getId())).withSelfRel(),
            linkTo(methodOn(DreamhouseController.class).update(r.getId(), null)).withRel("update"),
            linkTo(methodOn(DreamhouseController.class).delete(r.getId())).withRel("delete"),
            linkTo(methodOn(DreamhouseController.class).listAll(null, null)).withRel("collection")
        );

        if (r.getProprietaria() != null) {
            r.add(linkTo(methodOn(BarbieController.class)
                    .search(r.getProprietaria(), null, null)).withRel("proprietaria"));
        } else {
            r.add(linkTo(methodOn(BarbieController.class)
                    .listSemDreamhouse(null, null)).withRel("barbies-sem-casa"));
        }

        return r;
    }
}
