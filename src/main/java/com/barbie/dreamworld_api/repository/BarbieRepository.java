package com.barbie.dreamworld_api.repository;

import com.barbie.dreamworld_api.model.Barbie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarbieRepository extends JpaRepository<Barbie, Long> {

    Page<Barbie> findAll(Pageable pageable);

    Page<Barbie> findByColecaoIgnoreCase(String colecao, Pageable pageable);

    @Query("""
        SELECT b FROM Barbie b
        WHERE LOWER(b.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
        ORDER BY b.nome ASC
    """)
    Page<Barbie> searchByNome(@Param("nome") String nome, Pageable pageable);

    Page<Barbie> findByProfissaoId(Long profissaoId, Pageable pageable);

    @Query("SELECT b FROM Barbie b WHERE b.dreamhouse IS NULL")
    Page<Barbie> findSemDreamhouse(Pageable pageable);

    @Query("SELECT b FROM Barbie b JOIN b.eventos e WHERE e.id = :eventoId")
    List<Barbie> findByEventoId(@Param("eventoId") Long eventoId);

    boolean existsByNomeIgnoreCase(String nomeCasa);
}
