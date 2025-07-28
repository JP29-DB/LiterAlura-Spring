package com.literalura.challenge_literalura.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Clase que representa un autor en la base de datos.
 * Esta clase se mapea a la tabla "autores" y contiene información sobre los autores, incluyendo sus libros asociados.
 */
@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private int anoNacimiento;

    private Integer anoFallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Libro> libros;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getAnoNacimiento() {
        return anoNacimiento;
    }

    public void setAnoNacimiento(int anoNacimiento) {
        this.anoNacimiento = anoNacimiento;
    }

    public Integer getAnoFallecimiento() {
        return anoFallecimiento;
    }

    public void setAnoFallecimiento(Integer anoFallecimiento) {
        this.anoFallecimiento = anoFallecimiento;
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    @Override
    public String toString() {
        return "Autor: " + nombre + "\n" +
                "Fecha de nacimiento: " + anoNacimiento + "\n" +
                "Fecha de fallecimiento: " + (anoFallecimiento != null ? anoFallecimiento : "Desconocido") + "\n" +
                "Libros: " + (libros != null ? libros.size() : "Sin libros registrados");
    }
}