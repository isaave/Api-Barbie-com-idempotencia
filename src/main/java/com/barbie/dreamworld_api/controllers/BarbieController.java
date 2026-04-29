package com.barbie.dreamworld_api.controllers;

import com.barbie.dreamworld_api.assemblers.BarbieModelAssembler;
import com.barbie.dreamworld_api.dto.request.BarbieRequest;
import com.barbie.dreamworld_api.dto.response.BarbieResponse;
import com.barbie.dreamworld_api.exceptions.BusinessException;
import com.barbie.dreamworld_api.exceptions.ConflictException;
import com.barbie.dreamworld_api.exceptions.ResourceNotFoundException;
import com.barbie.dreamworld_api.model.Barbie;
import com.barbie.dreamworld_api.model.Dreamhouse;
import com.barbie.dreamworld_api.model.Profissao;
import com.barbie.dreamworld_api.repository.BarbieRepository;
import com.barbie.dreamworld_api.repository.DreamhouseRepository;
import com.barbie.dreamworld_api.repository.ProfissaoRepository;
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

@Tag(name = "Barbies", description = "Gerenciamento da coleção de Barbies")
@RestController
@RequestMapping("/api/barbies")
@RequiredArgsConstructor
public class BarbieController {

    private final BarbieRepository barbieRepository;
    private final ProfissaoRepository profissaoRepository;
    private final DreamhouseRepository dreamhouseRepository;
    private final BarbieModelAssembler assembler;

    private record BarbieFingerprint(String nome, String colecao, Long profissaoId, Long dreamhouseId) {}
    private record IdempotentBarbieResponse(BarbieFingerprint fingerprint, BarbieResponse body, URI location) {}
    private final ConcurrentHashMap<String, IdempotentBarbieResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todas as Barbies", description = "Paginação e ordenação suportadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping
    public ResponseEntity<PagedModel<BarbieResponse>> listAll(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<BarbieResponse> pagedAssembler) {

        Page<BarbieResponse> page = barbieRepository.findAll(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar Barbie por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Barbie encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Barbie não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BarbieResponse> getById(@PathVariable Long id) {
        Barbie b = findOrThrow(id);
        return ResponseEntity.ok(assembler.toModel(toResponse(b)));
    }

    @Operation(summary = "Buscar Barbies por nome (parcial, case-insensitive)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados da busca"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<BarbieResponse>> search(
            @Parameter(description = "Trecho do nome", required = true) @RequestParam String nome,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<BarbieResponse> pagedAssembler) {

        Page<BarbieResponse> page = barbieRepository.searchByNome(nome, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar Barbies por coleção")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/colecao/{colecao}")
    public ResponseEntity<PagedModel<BarbieResponse>> listByColecao(
            @PathVariable String colecao,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<BarbieResponse> pagedAssembler) {

        Page<BarbieResponse> page = barbieRepository.findByColecaoIgnoreCase(colecao, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar Barbies sem Dreamhouse atribuída")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/sem-dreamhouse")
    public ResponseEntity<PagedModel<BarbieResponse>> listSemDreamhouse(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<BarbieResponse> pagedAssembler) {

        Page<BarbieResponse> page = barbieRepository.findSemDreamhouse(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar Barbies de uma Profissão")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Profissão não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/profissao/{profissaoId}")
    public ResponseEntity<PagedModel<BarbieResponse>> listByProfissao(
            @PathVariable Long profissaoId,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<BarbieResponse> pagedAssembler) {

        if (!profissaoRepository.existsById(profissaoId))
            throw new ResourceNotFoundException("Profissao", profissaoId);

        Page<BarbieResponse> page = barbieRepository.findByProfissaoId(profissaoId, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Cadastrar nova Barbie")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Barbie criada com sucesso",
                    headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Profissão ou Dreamhouse não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Já existe uma Barbie com esse nome",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Dreamhouse já possui proprietária",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PostMapping
    public ResponseEntity<BarbieResponse> create(
            @Parameter(description = "Chave única para garantir idempotência da criação", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody BarbieRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        var fingerprint = new BarbieFingerprint(req.getNome(), req.getColecao(), req.getProfissaoId(), req.getDreamhouseId());

        synchronized (idempotencyLock) {
            IdempotentBarbieResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                return ResponseEntity.created(cached.location()).body(cached.body());
            }

            if (barbieRepository.existsByNomeIgnoreCase(req.getNome()))
                throw new ConflictException("Já existe uma Barbie com o nome: " + req.getNome());

            Barbie barbie = new Barbie();
            barbie.setNome(req.getNome());
            barbie.setColecao(req.getColecao());
            applyRelationships(barbie, req);

            Barbie saved = barbieRepository.save(barbie);
            BarbieResponse response = assembler.toModel(toResponse(saved));
            URI location = linkTo(methodOn(BarbieController.class).getById(saved.getId())).toUri();
            idempotencyCache.put(idempotencyKey, new IdempotentBarbieResponse(fingerprint, response, location));
            return ResponseEntity.created(location).body(response);
        }
    }

    @Operation(summary = "Atualizar Barbie (idempotente)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Barbie atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Barbie, profissão ou Dreamhouse não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Já existe uma Barbie com esse nome",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Dreamhouse já possui proprietária",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BarbieResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody BarbieRequest req) {
        Barbie barbie = findOrThrow(id);

        if (!barbie.getNome().equalsIgnoreCase(req.getNome())
                && barbieRepository.existsByNomeIgnoreCase(req.getNome())) {
            throw new ConflictException("Já existe uma Barbie com o nome: " + req.getNome());
        }

        barbie.setNome(req.getNome());
        barbie.setColecao(req.getColecao());
        applyRelationships(barbie, req);

        return ResponseEntity.ok(assembler.toModel(toResponse(barbieRepository.save(barbie))));
    }

    @Operation(summary = "Remover Barbie")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Barbie não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Barbie possui acessórios",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Barbie barbie = findOrThrow(id);
        if (!barbie.getAcessorios().isEmpty())
            throw new BusinessException("Não é possível remover uma Barbie com acessórios. Remova os acessórios primeiro.");
        barbieRepository.delete(barbie);
        return ResponseEntity.noContent().build();
    }

    private Barbie findOrThrow(Long id) {
        return barbieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbie", id));
    }

    private void applyRelationships(Barbie barbie, BarbieRequest req) {
        if (req.getProfissaoId() != null) {
            Profissao p = profissaoRepository.findById(req.getProfissaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Profissao", req.getProfissaoId()));
            barbie.setProfissao(p);
        } else {
            barbie.setProfissao(null);
        }

        if (req.getDreamhouseId() != null) {
            Dreamhouse d = dreamhouseRepository.findById(req.getDreamhouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dreamhouse", req.getDreamhouseId()));

            if (d.getBarbie() != null && !d.getBarbie().getId().equals(barbie.getId())) {
                throw new BusinessException(
                        "A Dreamhouse '" + d.getNomeCasa() + "' já pertence à Barbie '"
                                + d.getBarbie().getNome() + "'. "
                                + "Consulte GET /api/dreamhouses/sem-proprietaria para ver as disponíveis."
                );
            }

            barbie.setDreamhouse(d);
        } else {
            barbie.setDreamhouse(null);
        }
    }

    BarbieResponse toResponse(Barbie b) {
        return new BarbieResponse(
                b.getId(),
                b.getNome(),
                b.getColecao(),
                b.getProfissao() != null ? b.getProfissao().getNomeProfissao() : null,
                b.getProfissao() != null ? b.getProfissao().getAreaAtuacao() : null,
                b.getDreamhouse() != null ? b.getDreamhouse().getNomeCasa() : null,
                b.getAcessorios().size(),
                b.getEventos().size()
        );
    }
}