package com.barbie.dreamworld_api.repository;

import com.barbie.dreamworld_api.model.Acessorio;
import com.barbie.dreamworld_api.model.Raridade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AcessorioRepository extends JpaRepository<Acessorio, Long> {

    Page<Acessorio> findAll(Pageable pageable);

    Page<Acessorio> findByBarbieId(Long barbieId, Pageable pageable);

    Page<Acessorio> findByRaridade(Raridade raridade, Pageable pageable);

    Page<Acessorio> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    @Query("""
        SELECT a FROM Acessorio a
        WHERE LOWER(a.nomeItem) LIKE LOWER(CONCAT('%', :nome, '%'))
        ORDER BY a.nomeItem ASC
    """)
    Page<Acessorio> searchByNome(@Param("nome") String nome, Pageable pageable);
}
