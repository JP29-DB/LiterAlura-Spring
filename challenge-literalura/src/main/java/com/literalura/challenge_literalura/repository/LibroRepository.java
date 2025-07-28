package com.literalura.challenge_literalura.repository;

import com.literalura.challenge_literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByTituloIgnoreCase(String titulo);

    @Query("SELECT l FROM Libro l WHERE l.idioma = :idioma")
    List<Libro> findByIdioma(@Param("idioma") String idioma);
}