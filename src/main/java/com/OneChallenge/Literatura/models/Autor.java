package com.OneChallenge.Literatura.models;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "autores", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long Id;
    private  String nombre;
    private Integer nacimiento;
    private Integer fallecimiento;
    @ManyToMany(mappedBy = "autores")
    private List<Libro> libros;

    public Autor(){}

    public Autor(DatosAutor d) {
        this.nombre = d.nombre();
        this.nacimiento = d.nacimiento();
        this.fallecimiento = d.fallecimiento();
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(Integer nacimiento) {
        this.nacimiento = nacimiento;
    }

    public Integer getFallecimiento() {
        return fallecimiento;
    }

    public void setFallecimiento(Integer fallecimiento) {
        this.fallecimiento = fallecimiento;
    }

    @Override
    public String toString() {

        String añoNacimiento = (nacimiento != null) ? nacimiento.toString() : "Fecha de Naciento no registrado";
        String añoFallecimiento = (fallecimiento != null) ? fallecimiento.toString() : "No existe fecha de fallecimiento";

        return nombre + " (" + añoNacimiento + " fallecio:  " + añoFallecimiento + ")";
    }
}




