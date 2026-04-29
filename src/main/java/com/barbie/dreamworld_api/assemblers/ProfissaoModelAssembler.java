package com.barbie.dreamworld_api.assemblers;

import com.barbie.dreamworld_api.controllers.BarbieController;
import com.barbie.dreamworld_api.controllers.ProfissaoController;
import com.barbie.dreamworld_api.dto.response.ProfissaoResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Links fixos: self, update, delete, collection, mais-populares.
 * Condicional: se tem Barbies → link para listá-las;
 *              link para mesma área de atuação se área definida.
 */
@Component
public class ProfissaoModelAssembler
        extends RepresentationModelAssemblerSupport<ProfissaoResponse, ProfissaoResponse> {

    public ProfissaoModelAssembler() {
        super(ProfissaoController.class, ProfissaoResponse.class);
    }

    @Override
    public ProfissaoResponse toModel(ProfissaoResponse r) {
        r.add(
            linkTo(methodOn(ProfissaoController.class).getById(r.getId())).withSelfRel(),
            linkTo(methodOn(ProfissaoController.class).update(r.getId(), null)).withRel("update"),
            linkTo(methodOn(ProfissaoController.class).delete(r.getId())).withRel("delete"),
            linkTo(methodOn(ProfissaoController.class).listAll(null, null)).withRel("collection"),
            linkTo(methodOn(ProfissaoController.class).listMaisPopulares(null, null)).withRel("mais-populares")
        );

        if (r.getTotalBarbies() > 0) {
            r.add(linkTo(methodOn(BarbieController.class)
                    .listByProfissao(r.getId(), null, null)).withRel("barbies"));
        }

        if (r.getAreaAtuacao() != null) {
            r.add(linkTo(methodOn(ProfissaoController.class)
                    .listByArea(r.getAreaAtuacao(), null, null)).withRel("mesma-area"));
        }

        return r;
    }
}
