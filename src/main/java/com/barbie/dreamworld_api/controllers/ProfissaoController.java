package com.barbie.dreamworld_api.controllers;

import com.barbie.dreamworld_api.assemblers.ProfissaoModelAssembler;
import com.barbie.dreamworld_api.dto.request.ProfissaoRequest;
import com.barbie.dreamworld_api.dto.response.ProfissaoResponse;
import com.barbie.dreamworld_api.exceptions.BusinessException;
import com.barbie.dreamworld_api.exceptions.ConflictException;
import com.barbie.dreamworld_api.exceptions.ResourceNotFoundException;
import com.barbie.dreamworld_api.model.Profissao;
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

@Tag(name = "Profissões", description = "Gerenciamento das carreiras das Barbies")
@RestController
@RequestMapping("/api/profissoes")
@RequiredArgsConstructor
public class ProfissaoController {

    private final ProfissaoRepository profissaoRepository;
    private final ProfissaoModelAssembler assembler;

    private record ProfissaoFingerprint(String nomeProfissao, String areaAtuacao) {}
    private record IdempotentProfissaoResponse(ProfissaoFingerprint fingerprint, ProfissaoResponse body, URI location) {}
    private final ConcurrentHashMap<String, IdempotentProfissaoResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todas as profissões")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping
    public ResponseEntity<PagedModel<ProfissaoResponse>> listAll(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<ProfissaoResponse> pagedAssembler) {

        Page<ProfissaoResponse> page = profissaoRepository.findAll(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar profissão por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Não encontrada"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProfissaoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(toResponse(findOrThrow(id))));
    }

    @Operation(summary = "Buscar profissões por nome (parcial)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados da busca"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<ProfissaoResponse>> search(
            @RequestParam String nome,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<ProfissaoResponse> pagedAssembler) {

        Page<ProfissaoResponse> page = profissaoRepository.searchByNome(nome, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar profissões por área de atuação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/area/{area}")
    public ResponseEntity<PagedModel<ProfissaoResponse>> listByArea(
            @PathVariable String area,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<ProfissaoResponse> pagedAssembler) {

        Page<ProfissaoResponse> page = profissaoRepository.findByAreaAtuacaoIgnoreCase(area, pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar profissões mais populares")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/mais-populares")
    public ResponseEntity<PagedModel<ProfissaoResponse>> listMaisPopulares(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<ProfissaoResponse> pagedAssembler) {

        Page<ProfissaoResponse> page = profissaoRepository.findMaisPopulares(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Cadastrar nova profissão")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criada com sucesso",
                    headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Nome já existe"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PostMapping
    public ResponseEntity<ProfissaoResponse> create(
            @Parameter(description = "Chave única para garantir idempotência da criação", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ProfissaoRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        var fingerprint = new ProfissaoFingerprint(req.getNomeProfissao(), req.getAreaAtuacao());

        synchronized (idempotencyLock) {
            IdempotentProfissaoResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                return ResponseEntity.created(cached.location()).body(cached.body());
            }

            if (profissaoRepository.existsByNomeProfissaoIgnoreCase(req.getNomeProfissao()))
                throw new ConflictException("Já existe uma profissão com o nome: " + req.getNomeProfissao());

            Profissao p = new Profissao();
            p.setNomeProfissao(req.getNomeProfissao());
            p.setDescricao(req.getDescricao());
            p.setAreaAtuacao(req.getAreaAtuacao());

            Profissao saved = profissaoRepository.save(p);
            URI location = linkTo(methodOn(ProfissaoController.class).getById(saved.getId())).toUri();
            ProfissaoResponse body = assembler.toModel(toResponse(saved));
            idempotencyCache.put(idempotencyKey, new IdempotentProfissaoResponse(fingerprint, body, location));
            return ResponseEntity.created(location).body(body);
        }
    }

    @Operation(summary = "Atualizar profissão")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Profissão não encontrada"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProfissaoResponse> update(
            @PathVariable Long id, @Valid @RequestBody ProfissaoRequest req) {

        Profissao p = findOrThrow(id);
        p.setNomeProfissao(req.getNomeProfissao());
        p.setDescricao(req.getDescricao());
        p.setAreaAtuacao(req.getAreaAtuacao());

        return ResponseEntity.ok(assembler.toModel(toResponse(profissaoRepository.save(p))));
    }

    @Operation(summary = "Remover profissão")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "422", description = "Profissão possui Barbies vinculadas"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        Profissao p = findOrThrow(id);

        if (!p.getBarbies().isEmpty())
            throw new BusinessException("Não é possível remover uma profissão com Barbies vinculadas.");

        profissaoRepository.delete(p);
        return ResponseEntity.noContent().build();
    }

    private Profissao findOrThrow(Long id) {
        return profissaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissao", id));
    }

    ProfissaoResponse toResponse(Profissao p) {
        return new ProfissaoResponse(
                p.getId(),
                p.getNomeProfissao(),
                p.getDescricao(),
                p.getAreaAtuacao(),
                p.getBarbies().size()
        );
    }
}