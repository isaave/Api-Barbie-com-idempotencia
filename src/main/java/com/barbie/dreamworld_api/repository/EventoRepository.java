package com.barbie.dreamworld_api.repository;

import com.barbie.dreamworld_api.model.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    Page<Evento> findAll(Pageable pageable);

    @Query("""
        SELECT e FROM Evento e
        WHERE LOWER(e.nomeEvento) LIKE LOWER(CONCAT('%', :nome, '%'))
        ORDER BY e.dataEvento ASC
    """)
    Page<Evento> searchByNome(@Param("nome") String nome, Pageable pageable);

    Page<Evento> findByDataEventoAfter(LocalDate data, Pageable pageable);

    @Query("SELECT e FROM Evento e JOIN e.barbies b WHERE b.id = :barbieId")
    Page<Evento> findByBarbieId(@Param("barbieId") Long barbieId, Pageable pageable);

    @Query("SELECT e FROM Evento e ORDER BY SIZE(e.barbies) DESC")
    Page<Evento> findMaisPopulares(Pageable pageable);

    boolean existsByNomeEventoIgnoreCase(String nomeEvento);
}
