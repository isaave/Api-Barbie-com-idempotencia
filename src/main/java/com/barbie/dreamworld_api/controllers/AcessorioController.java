package com.barbie.dreamworld_api.controllers;

import com.barbie.dreamworld_api.assemblers.AcessorioModelAssembler;
import com.barbie.dreamworld_api.dto.request.AcessorioRequest;
import com.barbie.dreamworld_api.dto.response.AcessorioResponse;
import com.barbie.dreamworld_api.exceptions.BusinessException;
import com.barbie.dreamworld_api.exceptions.ConflictException;
import com.barbie.dreamworld_api.exceptions.ResourceNotFoundException;
import com.barbie.dreamworld_api.model.Acessorio;
import com.barbie.dreamworld_api.model.Barbie;
import com.barbie.dreamworld_api.model.Raridade;
import com.barbie.dreamworld_api.repository.AcessorioRepository;
import com.barbie.dreamworld_api.repository.BarbieRepository;
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
import org.springframework.web.bind.annotation.RequestHeader;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Acessórios", description = "Gerenciamento dos acessórios colecionáveis das Barbies")
@RestController
@RequestMapping("/api/acessorios")
@RequiredArgsConstructor
public class AcessorioController {

    private final AcessorioRepository acessorioRepository;
    private final BarbieRepository barbieRepository;
    private final AcessorioModelAssembler assembler;

    private record AcessorioFingerprint(String nomeItem, Long barbieId) {}
    private record IdempotentAcessorioResponse(AcessorioFingerprint fingerprint, AcessorioResponse body, URI location) {}
    private final ConcurrentHashMap<String, IdempotentAcessorioResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todos os acessórios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping
    public ResponseEntity<PagedModel<AcessorioResponse>> listAll(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<AcessorioResponse> pagedAssembler) {

        Page<AcessorioResponse> page = acessorioRepository.findAll(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar acessório por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AcessorioResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(toResponse(findOrThrow(id))));
    }

    @Operation(summary = "Listar acessórios de uma Barbie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "404", description = "Barbie não encontrada"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/barbie/{barbieId}")
    public ResponseEntity<PagedModel<AcessorioResponse>> listByBarbie(
            @PathVariable Long barbieId,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<AcessorioResponse> pagedAssembler) {

        if (!barbieRepository.existsById(barbieId))
            throw new ResourceNotFoundException("Barbie", barbieId);

        Page<AcessorioResponse> page = acessorioRepository.findByBarbieId(barbieId, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Filtrar acessórios por raridade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso"),
            @ApiResponse(responseCode = "400", description = "Raridade inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/raridade/{raridade}")
    public ResponseEntity<PagedModel<AcessorioResponse>> listByRaridade(
            @PathVariable Raridade raridade,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<AcessorioResponse> pagedAssembler) {

        Page<AcessorioResponse> page = acessorioRepository.findByRaridade(raridade, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Filtrar acessórios por categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso"),
            @ApiResponse(responseCode = "400", description = "Categoria inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<PagedModel<AcessorioResponse>> listByCategoria(
            @PathVariable String categoria,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<AcessorioResponse> pagedAssembler) {

        Page<AcessorioResponse> page = acessorioRepository.findByCategoriaIgnoreCase(categoria, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Cadastrar novo acessório")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                    headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Barbie não encontrada"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PostMapping
    public ResponseEntity<AcessorioResponse> create(
            @Parameter(description = "Chave única para garantir idempotência da criação", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AcessorioRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        var fingerprint = new AcessorioFingerprint(req.getNomeItem(), req.getBarbieId());

        synchronized (idempotencyLock) {
            IdempotentAcessorioResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                return ResponseEntity.created(cached.location()).body(cached.body());
            }

            Barbie barbie = barbieRepository.findById(req.getBarbieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barbie", req.getBarbieId()));

            Acessorio a = new Acessorio();
            a.setNomeItem(req.getNomeItem());
            a.setRaridade(req.getRaridade());
            a.setPreco(req.getPreco());
            a.setCategoria(req.getCategoria());
            a.setBarbie(barbie);

            Acessorio saved = acessorioRepository.save(a);
            URI location = linkTo(methodOn(AcessorioController.class).getById(saved.getId())).toUri();
            AcessorioResponse body = assembler.toModel(toResponse(saved));
            idempotencyCache.put(idempotencyKey, new IdempotentAcessorioResponse(fingerprint, body, location));
            return ResponseEntity.created(location).body(body);
        }
    }

    @Operation(summary = "Atualizar acessório")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AcessorioResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AcessorioRequest req) {

        Acessorio a = findOrThrow(id);

        Barbie barbie = barbieRepository.findById(req.getBarbieId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbie", req.getBarbieId()));

        a.setNomeItem(req.getNomeItem());
        a.setRaridade(req.getRaridade());
        a.setPreco(req.getPreco());
        a.setCategoria(req.getCategoria());
        a.setBarbie(barbie);

        return ResponseEntity.ok(assembler.toModel(toResponse(acessorioRepository.save(a))));
    }

    @Operation(summary = "Remover acessório")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        acessorioRepository.delete(findOrThrow(id));
        return ResponseEntity.noContent().build();
    }

    private Acessorio findOrThrow(Long id) {
        return acessorioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acessorio", id));
    }

    AcessorioResponse toResponse(Acessorio a) {
        return new AcessorioResponse(
                a.getId(),
                a.getNomeItem(),
                a.getRaridade(),
                a.getPreco(),
                a.getCategoria(),
                a.getBarbie().getNome(),
                a.getBarbie().getId()
        );
    }
}