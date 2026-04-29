package com.barbie.dreamworld_api.assemblers;

import com.barbie.dreamworld_api.controllers.EventoController;
import com.barbie.dreamworld_api.dto.response.EventoResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Links fixos: self, update, delete, collection.
 * Condicional: se futuro → link para eventos futuros;
 *              se tem participantes → link para mais populares.
 */
@Component
public class EventoModelAssembler
        extends RepresentationModelAssemblerSupport<EventoResponse, EventoResponse> {

    public EventoModelAssembler() {
        super(EventoController.class, EventoResponse.class);
    }

    @Override
    public EventoResponse toModel(EventoResponse r) {
        r.add(
            linkTo(methodOn(EventoController.class).getById(r.getId())).withSelfRel(),
            linkTo(methodOn(EventoController.class).update(r.getId(), null)).withRel("update"),
            linkTo(methodOn(EventoController.class).delete(r.getId())).withRel("delete"),
            linkTo(methodOn(EventoController.class).listAll(null, null)).withRel("collection"),
            linkTo(methodOn(EventoController.class).listMaisPopulares(null, null)).withRel("mais-populares")
        );

        if (r.getDataEvento() != null && r.getDataEvento().isAfter(LocalDate.now())) {
            r.add(linkTo(methodOn(EventoController.class)
                    .listFuturos(null, null)).withRel("outros-futuros"));
        }

        return r;
    }
}
