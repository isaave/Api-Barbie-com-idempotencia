package com.barbie.dreamworld_api.controllers;

import com.barbie.dreamworld_api.assemblers.DreamhouseModelAssembler;
import com.barbie.dreamworld_api.dto.request.DreamhouseRequest;
import com.barbie.dreamworld_api.dto.response.DreamhouseResponse;
import com.barbie.dreamworld_api.exceptions.BusinessException;
import com.barbie.dreamworld_api.exceptions.ConflictException;
import com.barbie.dreamworld_api.exceptions.ResourceNotFoundException;
import com.barbie.dreamworld_api.model.Dreamhouse;
import com.barbie.dreamworld_api.repository.DreamhouseRepository;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Dreamhouses", description = "Gerenciamento das residências das Barbies")
@RestController
@RequestMapping("/api/dreamhouses")
@RequiredArgsConstructor
public class DreamhouseController {

    private final DreamhouseRepository dreamhouseRepository;
    private final DreamhouseModelAssembler assembler;

    private record DreamhouseFingerprint(String nomeCasa, String endereco, String cor, Integer numeroQuartos) {}
    private record IdempotentDreamhouseResponse(DreamhouseFingerprint fingerprint, DreamhouseResponse body, URI location) {}
    private final ConcurrentHashMap<String, IdempotentDreamhouseResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todas as Dreamhouses")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping
    public ResponseEntity<PagedModel<DreamhouseResponse>> listAll(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<DreamhouseResponse> pagedAssembler) {
        Page<DreamhouseResponse> page = dreamhouseRepository.findAll(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar Dreamhouse por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dreamhouse encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Não encontrada"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DreamhouseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(toResponse(findOrThrow(id))));
    }

    @Operation(summary = "Buscar Dreamhouse por nome (parcial)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados da busca"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<DreamhouseResponse>> search(
            @RequestParam String nome,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<DreamhouseResponse> pagedAssembler) {
        Page<DreamhouseResponse> page = dreamhouseRepository.searchByNome(nome, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar Dreamhouses sem proprietária (disponíveis)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/sem-proprietaria")
    public ResponseEntity<PagedModel<DreamhouseResponse>> listSemProprietaria(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<DreamhouseResponse> pagedAssembler) {
        Page<DreamhouseResponse> page = dreamhouseRepository.findSemProprietaria(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar Dreamhouse da Barbie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dreamhouse encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Barbie não possui Dreamhouse"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/barbie/{barbieId}")
    public ResponseEntity<DreamhouseResponse> getByBarbie(@PathVariable Long barbieId) {
        Dreamhouse d = dreamhouseRepository.findByBarbieId(barbieId)
                .orElseThrow(() -> new ResourceNotFoundException("Dreamhouse da Barbie " + barbieId + " não encontrada"));
        return ResponseEntity.ok(assembler.toModel(toResponse(d)));
    }

    @Operation(summary = "Cadastrar nova Dreamhouse")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criada com sucesso",
                    headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Nome já existe"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PostMapping
    public ResponseEntity<DreamhouseResponse> create(
            @Parameter(description = "Chave única para garantir idempotência da criação", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DreamhouseRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        var fingerprint = new DreamhouseFingerprint(req.getNomeCasa(), req.getEndereco(), req.getCor(), req.getNumeroQuartos());

        synchronized (idempotencyLock) {
            IdempotentDreamhouseResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                return ResponseEntity.created(cached.location()).body(cached.body());
            }

            if (dreamhouseRepository.existsByNomeCasaIgnoreCase(req.getNomeCasa()))
                throw new ConflictException("Já existe uma Dreamhouse com o nome: " + req.getNomeCasa());

            Dreamhouse d = new Dreamhouse();
            d.setNomeCasa(req.getNomeCasa());
            d.setEndereco(req.getEndereco());
            d.setCor(req.getCor());
            d.setNumeroQuartos(req.getNumeroQuartos());

            Dreamhouse saved = dreamhouseRepository.save(d);
            URI location = linkTo(methodOn(DreamhouseController.class).getById(saved.getId())).toUri();
            DreamhouseResponse body = assembler.toModel(toResponse(saved));
            idempotencyCache.put(idempotencyKey, new IdempotentDreamhouseResponse(fingerprint, body, location));
            return ResponseEntity.created(location).body(body);
        }
    }

    @Operation(summary = "Atualizar Dreamhouse")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Dreamhouse não encontrada"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DreamhouseResponse> update(
            @PathVariable Long id, @Valid @RequestBody DreamhouseRequest req) {
        Dreamhouse d = findOrThrow(id);
        d.setNomeCasa(req.getNomeCasa());
        d.setEndereco(req.getEndereco());
        d.setCor(req.getCor());
        d.setNumeroQuartos(req.getNumeroQuartos());
        return ResponseEntity.ok(assembler.toModel(toResponse(dreamhouseRepository.save(d))));
    }

    @Operation(summary = "Remover Dreamhouse")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Não encontrada"),
            @ApiResponse(responseCode = "422", description = "Possui proprietária"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Dreamhouse d = findOrThrow(id);

        if (d.getBarbie() != null) {
            throw new BusinessException(
                    "Não é possível remover a Dreamhouse '" + d.getNomeCasa() +
                            "' pois ela pertence à Barbie '" + d.getBarbie().getNome()
            );
        }

        dreamhouseRepository.delete(d);
        return ResponseEntity.noContent().build();
    }

    private Dreamhouse findOrThrow(Long id) {
        return dreamhouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dreamhouse", id));
    }

    DreamhouseResponse toResponse(Dreamhouse d) {
        return new DreamhouseResponse(
                d.getId(),
                d.getNomeCasa(),
                d.getEndereco(),
                d.getCor(),
                d.getNumeroQuartos(),
                d.getBarbie() != null ? d.getBarbie().getNome() : null
        );
    }
}