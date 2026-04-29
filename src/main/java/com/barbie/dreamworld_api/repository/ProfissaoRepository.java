package com.barbie.dreamworld_api.repository;

import com.barbie.dreamworld_api.model.Profissao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfissaoRepository extends JpaRepository<Profissao, Long> {

    Page<Profissao> findAll(Pageable pageable);

    @Query("""
        SELECT p FROM Profissao p
        WHERE LOWER(p.nomeProfissao) LIKE LOWER(CONCAT('%', :nome, '%'))
        ORDER BY p.nomeProfissao ASC
    """)
    Page<Profissao> searchByNome(@Param("nome") String nome, Pageable pageable);

    Page<Profissao> findByAreaAtuacaoIgnoreCase(String area, Pageable pageable);

    @Query("SELECT p FROM Profissao p ORDER BY SIZE(p.barbies) DESC")
    Page<Profissao> findMaisPopulares(Pageable pageable);

    boolean existsByNomeProfissaoIgnoreCase(String nomeProfissao);
}
