package com.OneChallenge.Literatura.repository;

import com.OneChallenge.Literatura.models.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Optional<Libro> findByTituloContainsIgnoreCase(String nombreTitulo);

    @Query("SELECT l FROM Libro l JOIN l.idiomas i WHERE i = :idioma")
    List<Libro> findByContainingIdioma(@Param("idioma") String idioma);

    @Query(value = "SELECT * FROM libros ORDER BY numero_de_descarga DESC LIMIT 10", nativeQuery = true)
    List<Libro> topLibrosMasDescargados();

}
