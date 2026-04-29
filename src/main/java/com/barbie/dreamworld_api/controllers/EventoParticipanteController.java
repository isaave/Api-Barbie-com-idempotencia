package com.barbie.dreamworld_api.controllers;

import com.barbie.dreamworld_api.exceptions.BusinessException;
import com.barbie.dreamworld_api.exceptions.ResourceNotFoundException;
import com.barbie.dreamworld_api.model.Barbie;
import com.barbie.dreamworld_api.model.Evento;
import com.barbie.dreamworld_api.repository.BarbieRepository;
import com.barbie.dreamworld_api.repository.EventoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Eventos", description = "Gerenciamento dos eventos de gala do multiverso Barbie")
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoParticipanteController {

    private final EventoRepository eventoRepository;
    private final BarbieRepository barbieRepository;

    @Operation(summary = "Inscrever Barbie em um Evento (N:N)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Barbie inscrita com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento ou Barbie não encontrados",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Barbie já inscrita neste evento",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PutMapping("/{eventoId}/participantes/{barbieId}")
    public ResponseEntity<Void> inscrever(
            @PathVariable Long eventoId,
            @PathVariable Long barbieId) {

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", eventoId));

        Barbie barbie = barbieRepository.findById(barbieId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbie", barbieId));

        if (barbie.getEventos().contains(evento)) {
            throw new BusinessException("Barbie já está inscrita neste evento.");
        }

        barbie.getEventos().add(evento);
        barbieRepository.save(barbie);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remover Barbie de um Evento (N:N)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Barbie removida do evento"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento ou Barbie não encontrados",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @DeleteMapping("/{eventoId}/participantes/{barbieId}")
    public ResponseEntity<Void> remover(
            @PathVariable Long eventoId,
            @PathVariable Long barbieId) {

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", eventoId));

        Barbie barbie = barbieRepository.findById(barbieId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbie", barbieId));

        barbie.getEventos().remove(evento);
        barbieRepository.save(barbie);

        return ResponseEntity.noContent().build();
    }
}