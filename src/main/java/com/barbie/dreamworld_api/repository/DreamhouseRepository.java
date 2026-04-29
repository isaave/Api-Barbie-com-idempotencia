package com.barbie.dreamworld_api.repository;

import com.barbie.dreamworld_api.model.Dreamhouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DreamhouseRepository extends JpaRepository<Dreamhouse, Long> {

    Page<Dreamhouse> findAll(Pageable pageable);

    @Query("""
        SELECT d FROM Dreamhouse d
        WHERE LOWER(d.nomeCasa) LIKE LOWER(CONCAT('%', :nome, '%'))
        ORDER BY d.nomeCasa ASC
    """)
    Page<Dreamhouse> searchByNome(@Param("nome") String nome, Pageable pageable);

    Page<Dreamhouse> findByCor(String cor, Pageable pageable);

    @Query("SELECT d FROM Dreamhouse d WHERE d.barbie IS NULL")
    Page<Dreamhouse> findSemProprietaria(Pageable pageable);

    @Query("SELECT d FROM Dreamhouse d WHERE d.barbie.id = :barbieId")
    Optional<Dreamhouse> findByBarbieId(@Param("barbieId") Long barbieId);

    boolean existsByNomeCasaIgnoreCase(String nomeCasa);
}
