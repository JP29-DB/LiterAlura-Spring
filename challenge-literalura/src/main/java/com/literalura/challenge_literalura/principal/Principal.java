package com.literalura.challenge_literalura.principal;

import com.literalura.challenge_literalura.dto.AutorDTO;
import com.literalura.challenge_literalura.dto.LibroDTO;
import com.literalura.challenge_literalura.dto.RespuestaLibrosDTO;
import com.literalura.challenge_literalura.model.Autor;
import com.literalura.challenge_literalura.model.Libro;
import com.literalura.challenge_literalura.service.AutorService;
import com.literalura.challenge_literalura.service.ConsumoAPI;
import com.literalura.challenge_literalura.service.ConvierteDatos;
import com.literalura.challenge_literalura.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class Principal {

    @Autowired
    private LibroService libroService;

    @Autowired
    private AutorService autorService;

    @Autowired
    private ConsumoAPI consumoAPI = new ConsumoAPI();

    @Autowired
    private ConvierteDatos convierteDatos = new ConvierteDatos();

    private static final String BASE_URL = "https://gutendex.com/books/";

    Scanner scanner = new Scanner(System.in);
    public void mostrarMenu() {
        int opcion;

        do {
            System.out.println("--- LITERALURA ---");
            System.out.println("1 - Buscar libro por título");
            System.out.println("2 - Listar libros registrados");
            System.out.println("3 - Listar autores registrados");
            System.out.println("4 - Listar autores vivos en un año");
            System.out.println("5 - Listar libros por idioma");
            System.out.println("0 - Salir");
            System.out.print("Seleccione una opcion: ");
            opcion = scanner.nextInt();
            scanner.nextLine();

                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        buscarAutorPorAno();
                        break;
                    case 5:
                        buscarPorIdioma();
                        break;
                    case 0:
                        System.out.println("\nSaliendo...\n");
                        break;
                    default:
                        System.out.println("\nOpción no válida. Intente de nuevo" + " \n");
                }
        } while (opcion != 0);
        scanner.close();
    }

    private void buscarLibroPorTitulo() {
        System.out.print("Ingrese el título del libro: ");
        String titulo = scanner.nextLine();
        try {
            String encodedTitulo = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
            String json = consumoAPI.obtenerDatos(BASE_URL + "?search=" + encodedTitulo);
            RespuestaLibrosDTO respuestaLibrosDTO = convierteDatos.obtenerDatos(json, RespuestaLibrosDTO.class);
            List<LibroDTO> librosDTO = respuestaLibrosDTO.getLibros();
            if (librosDTO.isEmpty()) {
                System.out.println("\nLibro no encontrado en la API\n");
            } else {
                boolean libroRegistrado = false;
                for (LibroDTO libroDTO : librosDTO) {
                    if (libroDTO.getTitulo().equalsIgnoreCase(titulo)) {
                        Optional<Libro> libroExistente = libroService.obtenerLibroPorTitulo(titulo);
                        if (libroExistente.isPresent()) {
                            System.out.println("\nDetalle: " + titulo + " ya existe");
                            System.out.println("No se puede registrar el mismo libro más de una vez\n");
                            libroRegistrado = true;
                            break;
                        } else {
                            Libro libro = new Libro();
                            libro.setTitulo(libroDTO.getTitulo());
                            libro.setIdioma(libroDTO.getIdiomas().getFirst());
                            libro.setNumeroDescargas(libroDTO.getNumeroDescargas());

                            AutorDTO primerAutorDTO = libroDTO.getAutores().getFirst();
                            Autor autor = autorService.obtenerAutorPorNombre(primerAutorDTO.getNombre())
                                    .orElseGet(() -> {
                                        Autor nuevoAutor = new Autor();
                                        nuevoAutor.setNombre(primerAutorDTO.getNombre());
                                        nuevoAutor.setAnoNacimiento(primerAutorDTO.getAnoNacimiento());
                                        nuevoAutor.setAnoFallecimiento(primerAutorDTO.getAnoFallecimiento());
                                        return autorService.crearAutor(nuevoAutor);
                                    });

                            libro.setAutor(autor);

                            libroService.crearLibro(libro);
                            System.out.println("\nLibro registrado: " + libro.getTitulo() + "\n");
                            mostrarDetallesLibro(libroDTO);
                            libroRegistrado = true;
                            break;
                        }
                    }
                }
                if (!libroRegistrado) {
                    System.out.println("\nNo se encontró un libro exactamente con el título '" + titulo + "' en la base de datos\n");
                }
            }
        } catch (Exception e) {
            System.out.println("\nError al obtener datos de la API: " + e.getMessage() + "\n");
        }
    }

    private void listarLibrosRegistrados() {
        libroService.listarLibros().forEach(libro -> {
            System.out.println("\n---LIBRO---");
            System.out.println("Titulo: " + libro.getTitulo());
            System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Número de descargas: " + libro.getNumeroDescargas() +"\n");
        });
    }

    private void listarAutoresRegistrados() {
        autorService.listarAutores().forEach(autor -> {
            System.out.println("\n-------AUTOR-------");
            System.out.println("Autor: " + autor.getNombre());
            System.out.println("Fecha de nacimiento: " + autor.getAnoNacimiento());
            System.out.println("Fecha de fallecimiento: " + (autor.getAnoFallecimiento() != null ? autor.getAnoFallecimiento() : "Desconocido"));
            String libros = autor.getLibros().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.joining(", "));
            System.out.println("Libros: [ " + libros + " ]");
            System.out.print("\n");
        });
    }

    private void buscarAutorPorAno() {
        System.out.println("Ingrese el año vivo de autor(es) que desea buscar: ");
        int ano = scanner.nextInt();
        scanner.nextLine();
        List<Autor> autoresVivos = autorService.listarAutoresVivosEnAno(ano);
        if (autoresVivos.isEmpty()) {
            System.out.println("\nNo se encontraron autores vivos en el año " + ano + "\n");
        } else {
            autoresVivos.forEach(autor -> {
                System.out.println("\n---AUTOR---");
                System.out.println("Autor: " + autor.getNombre());
                System.out.println("Fecha de nacimiento: " + autor.getAnoNacimiento());
                System.out.println("Fecha de fallecimiento: " + (autor.getAnoFallecimiento()));
                System.out.println("Libros: " + autor.getLibros().size());
                System.out.println("\n");
            });
        }
    }

    private void buscarPorIdioma() {
        System.out.println("Ingrese el idioma:");
        System.out.println("es - Español");
        System.out.println("en - Inglés");
        System.out.println("fr - Francés");
        System.out.println("pt - Portugués");
        String idioma = scanner.nextLine();
        if ("es".equalsIgnoreCase(idioma) || "en".equalsIgnoreCase(idioma) || "fr".equalsIgnoreCase(idioma) || "pt".equalsIgnoreCase(idioma)) {
            libroService.listarLibrosPorIdioma(idioma).forEach(libro -> {
                System.out.println("\n---LIBRO---");
                System.out.println("Título: " + libro.getTitulo());
                System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
                System.out.println("Idioma: " + libro.getIdioma());
                System.out.println("Número de descargas: " + libro.getNumeroDescargas() + "\n");
            });
        } else {
            System.out.println("\nIdioma no válido. Intente de nuevo. \n");
        }
    }

    private void mostrarDetallesLibro(LibroDTO libroDTO) {
        System.out.println("\n--LIBRO---");
        System.out.println("Título: " + libroDTO.getTitulo());
        System.out.println("Autor: " + (libroDTO.getAutores().isEmpty() ? "Desconocido" : libroDTO.getAutores().getFirst()));
        System.out.println("Idioma: " + libroDTO.getIdiomas().getFirst());
        System.out.println("Número de descargas: " + libroDTO.getNumeroDescargas() +"\n");
    }
}


