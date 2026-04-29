package com.barbie.dreamworld_api.controllers;

import com.barbie.dreamworld_api.assemblers.EventoModelAssembler;
import com.barbie.dreamworld_api.dto.request.EventoRequest;
import com.barbie.dreamworld_api.dto.response.EventoResponse;
import com.barbie.dreamworld_api.exceptions.BusinessException;
import com.barbie.dreamworld_api.exceptions.ConflictException;
import com.barbie.dreamworld_api.exceptions.ResourceNotFoundException;
import com.barbie.dreamworld_api.model.Evento;
import com.barbie.dreamworld_api.repository.EventoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Eventos", description = "Gerenciamento dos eventos de gala do multiverso Barbie")
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoRepository eventoRepository;
    private final EventoModelAssembler assembler;

    private record EventoFingerprint(String nomeEvento, String local, String dataEvento) {}
    private record IdempotentEventoResponse(EventoFingerprint fingerprint, EventoResponse body, URI location) {}
    private final ConcurrentHashMap<String, IdempotentEventoResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todos os eventos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping
    public ResponseEntity<PagedModel<EventoResponse>> listAll(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<EventoResponse> pagedAssembler) {

        Page<EventoResponse> page = eventoRepository.findAll(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar evento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(toResponse(findOrThrow(id))));
    }

    @Operation(summary = "Buscar eventos por nome (parcial)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados da busca"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EventoResponse>> search(
            @RequestParam String nome,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<EventoResponse> pagedAssembler) {

        Page<EventoResponse> page = eventoRepository.searchByNome(nome, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar eventos futuros (data > hoje)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/futuros")
    public ResponseEntity<PagedModel<EventoResponse>> listFuturos(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<EventoResponse> pagedAssembler) {

        Page<EventoResponse> page = eventoRepository.findByDataEventoAfter(LocalDate.now(), pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar eventos de uma Barbie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/barbie/{barbieId}")
    public ResponseEntity<PagedModel<EventoResponse>> listByBarbie(
            @PathVariable Long barbieId,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<EventoResponse> pagedAssembler) {

        Page<EventoResponse> page = eventoRepository.findByBarbieId(barbieId, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar eventos por número de participantes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/mais-populares")
    public ResponseEntity<PagedModel<EventoResponse>> listMaisPopulares(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<EventoResponse> pagedAssembler) {

        Page<EventoResponse> page = eventoRepository.findMaisPopulares(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Cadastrar novo evento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                    headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Nome já existe",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PostMapping
    public ResponseEntity<EventoResponse> create(
            @Parameter(description = "Chave única para garantir idempotência da criação", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody EventoRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        var fingerprint = new EventoFingerprint(
                req.getNomeEvento(),
                req.getLocal(),
                req.getDataEvento() != null ? req.getDataEvento().toString() : null
        );

        synchronized (idempotencyLock) {
            IdempotentEventoResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                return ResponseEntity.created(cached.location()).body(cached.body());
            }

            if (eventoRepository.existsByNomeEventoIgnoreCase(req.getNomeEvento()))
                throw new ConflictException("Já existe um evento com o nome: " + req.getNomeEvento());

            Evento e = new Evento();
            e.setNomeEvento(req.getNomeEvento());
            e.setDescricao(req.getDescricao());
            e.setDataEvento(req.getDataEvento());
            e.setLocal(req.getLocal());

            Evento saved = eventoRepository.save(e);
            URI location = linkTo(methodOn(EventoController.class).getById(saved.getId())).toUri();
            EventoResponse body = assembler.toModel(toResponse(saved));
            idempotencyCache.put(idempotencyKey, new IdempotentEventoResponse(fingerprint, body, location));
            return ResponseEntity.created(location).body(body);
        }
    }

    @Operation(summary = "Atualizar evento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequest req) {

        Evento e = findOrThrow(id);
        e.setNomeEvento(req.getNomeEvento());
        e.setDescricao(req.getDescricao());
        e.setDataEvento(req.getDataEvento());
        e.setLocal(req.getLocal());

        return ResponseEntity.ok(assembler.toModel(toResponse(eventoRepository.save(e))));
    }

    @Operation(summary = "Remover evento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Evento possui Barbies inscritas",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        Evento e = findOrThrow(id);

        if (!e.getBarbies().isEmpty()) {
            throw new BusinessException(
                    "Não é possível remover o evento '" + e.getNomeEvento() +
                            "' pois ele possui " + e.getBarbies().size() + " Barbie(s) inscrita(s)."
            );
        }

        eventoRepository.delete(e);
        return ResponseEntity.noContent().build();
    }

    private Evento findOrThrow(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
    }

    EventoResponse toResponse(Evento e) {
        return new EventoResponse(
                e.getId(),
                e.getNomeEvento(),
                e.getDescricao(),
                e.getDataEvento(),
                e.getLocal(),
                e.getBarbies().size()
        );
    }
}