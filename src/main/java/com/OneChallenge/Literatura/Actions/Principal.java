package com.OneChallenge.Literatura.Actions;
import com.OneChallenge.Literatura.models.*;
import com.OneChallenge.Literatura.repository.*;
import com.OneChallenge.Literatura.service.*;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private List<DatosLibros> datosLibros = new ArrayList<>();
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    private List<Libro> libros;
    private Optional<Libro> libroBuscado;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void loadMenu() {
        var opcion = -1;


        while (opcion != 0) {
            var menu = """
                    ***************************
                     CATALOGO DE LITERATURA
                    ***************************
                    Bienvenido al catálogo de literatura. Aquí puedes buscar y gestionar libros y autores.
                    Selecciona una opción del menú:
                    1 - Buscar Libro en la plataforma
                    2 - Historial de los Libros buscados
                    3 - Buscar Libro por titulo 
                    4 - Lista los Libros Registrados
                    5 - Lista los Autores Registrados
                    6 - Los 10 Libros mas descargados
                    7 - Buscar un Libros por su Idioma
                    8 - Lista de Autores vivos en un año determinado
                    9 - Los 5 Autores con mas Años de muerto
                    10 - Buscar autor por su Nombre6
                    0 - Salir
                    --------------------------------------------------------------
                    Elija una opción: """;

            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> mostrarLibrosBuscados();
                case 3 -> DBSearchBook();
                case 4 -> DBListbooks();
                case 5 -> DBListAutors();
                case 6 -> BooksDownloaded();
                case 7 ->  searchBooksLanguage();
                case 8 -> AutoresbyLiveYear();
                case 9 -> AutorsTopDead();
                case 10 -> AutorByName();
                case 0 -> System.out.println("Gracias por usar nuestro sistema. ¡Hasta luego!");
                default -> System.out.println("Opción inválida. Intente nuevamente.");
            }
        }
    }


    private DatosLibros getDatosLibros() {
        System.out.println("Escribe el título del libro a buscar:");
        String tituloNombre = teclado.nextLine();

        String url = URL_BASE + "?search=" + tituloNombre.replace(" ", "+");
        String json = consumoAPI.obtenerDatos(url);

        Datos datos = conversor.obtenerDatos(json, Datos.class);

        if (datos.resultados().isEmpty()) {
            System.out.println("No se encontró ningún libro con ese título.");
            return null;
        }

        return datos.resultados().get(0); // Devolver solo el primer libro encontrado
    }

    private void buscarLibroPorTitulo() {
        DatosLibros datos = getDatosLibros();
        if (datos == null) return;
        Optional<Libro> libroExistente = libroRepository.findByTituloContainsIgnoreCase(datos.titulo());
        if (libroExistente.isPresent()) {
            System.out.println("El libro  " + datos.titulo() + "  ya está almacenado en la base de datos.");
            System.out.println(libroExistente.get());
        } else {
            Libro libro = new Libro(datos);
            libroRepository.save(libro);
            if (datos.idiomas() != null) {
                List<String> idiomasNormalizados = datos.idiomas().stream()
                        .map(String::toLowerCase)
                        .distinct()
                        .collect(Collectors.toList());
                libro.setIdiomas(idiomasNormalizados);
            }
            if(datos.autor()!= null ){
                List<Autor> autores = datos.autor().stream()
                        .map(datoAutor -> autorRepository
                                .findByNombre(datoAutor.nombre())
                                .orElseGet(() -> autorRepository.save(new Autor(datoAutor)))
                        )
                        .collect(Collectors.toList());
                libro.setAutores(autores);
                autores.forEach(a -> a.setLibros(List.of(libro)));
            }
            System.out.println("Libro Almacenado con éxito:");
            System.out.println(libro);
        }
    }

    private void mostrarLibrosBuscados() {
        libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay registros de libros en el sistema.");
            return;
        }
        libros.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .forEach(System.out::println);
    }

    private void DBListbooks() {
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println(" No existen Libros registros en el Sistema.");
            return;
        }
        System.out.println(libros.size() + " libros, los cuales son:");
        libros.stream()
                .map(Libro::getTitulo)
                .forEach(titulo -> System.out.println("El libro:  " + titulo));
    }

    private void DBSearchBook() {
        System.out.println("Escribe el título del libro :");
        String nombreTitulo = teclado.nextLine();
        libroBuscado = libroRepository.findByTituloContainsIgnoreCase(nombreTitulo);
        if (libroBuscado.isPresent()) {
            System.out.println("Libro encontrado :" + libroBuscado.get());
        } else {
            System.out.println("Libro no registro en el sistema.");
        }


    }

    private void DBListAutors() {
        List<Autor> autores = autorRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("📭 No hay autores guardados en la base de datos.");
            return;
        }
        System.out.println("Se encontraron " + autores.size() + " autores, los cuales son:");
        autores.stream()
                .map(Autor::getNombre)
                .forEach(nombre -> System.out.println("Autor: " + nombre));

    }

    private void BooksDownloaded(){
        List<Libro> top10 = libroRepository.topLibrosMasDescargados();
        System.out.println("Los 10 libros mas descargados:");
        top10.forEach(libro ->System.out.println(
                "Nombre " + libro.getTitulo() + " - Numero de Descargas: " + libro.getNumeroDeDescarga()
            )
        );
    }

    private void AutoresbyLiveYear() {
        System.out.println("Escribe el año para revisar autores vivos:");
        int fecha = teclado.nextInt();
        teclado.nextLine();
        List<Autor> autoresVivos = autorRepository.autoresbyLiveYear(fecha);
        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontró ningún autor vivo en ese año.");
        } else {
            System.out.println("Autores vivos en " + fecha + ":");
            autoresVivos.forEach(a -> System.out.println("Autor: " + a.getNombre()));
        }
    }

    private void AutorByName() {
        System.out.println("Escribe el autor que deseas buscar:");
        String nombreAutor = teclado.nextLine();
        Optional<Autor> autorBuscado = autorRepository.findByNombreIgnoreCase(nombreAutor);
        if (autorBuscado.isPresent()) {
            System.out.println("Autor encontrado: " + autorBuscado.get());
        } else {
            System.out.println("No se encontró ningún autor con ese nombre.");
        }
    }


    public void AutorsTopDead() {
        List<Autor> autores = autorRepository.findAll();

        int datenow = Year.now().getValue();

        List<Autor> topAutores = autores.stream()
                .filter(a -> a.getFallecimiento() != null)
                .sorted((a1, a2) -> {
                    int fechaMuerto1 = (a1.getFallecimiento() < 0)
                            ? datenow + Math.abs(a1.getFallecimiento())
                            : datenow - a1.getFallecimiento();

                    int fechaMuerto2 = (a2.getFallecimiento() < 0)
                            ? datenow + Math.abs(a2.getFallecimiento())
                            : datenow - a2.getFallecimiento();

                    return Integer.compare(fechaMuerto2, fechaMuerto1); // Orden descendente
                })
                .limit(5)
                .collect(Collectors.toList());

        System.out.println("Los 5 autores con más años de fallecidos:\n ");
        for (Autor autor : topAutores) {
            int fechaFallecimiento = autor.getFallecimiento();
            String era = (fechaFallecimiento < 0) ? "A.C." : "D.C.";
            int dateDied = (fechaFallecimiento < 0)
                    ? datenow + Math.abs(fechaFallecimiento)
                    : datenow - fechaFallecimiento;

            System.out.printf("El autor: %-30s  fallecido en: %4d %s (%d años muerto)%n",
                    autor.getNombre(), Math.abs(fechaFallecimiento), era, dateDied);
        }
    }
    private void searchBooksLanguage() {
        // Mapa de idiomas disponibles con su código y nombre
        Map<Integer, Map.Entry<String, String>> opcionesIdiomas = Map.of(
                1, Map.entry("en", "Inglés"),
                2, Map.entry("es", "Español"),
                3, Map.entry("fr", "Francés"),
                4, Map.entry("it", "Italiano"),
                5, Map.entry("zh", "Chino"),
                6, Map.entry("tl", "Talago")
        );

        System.out.println("""
        Seleccione el idioma para buscar libros:
        1 - Inglés (en)
        2 - Español (es)
        3 - Francés (fr)
        4 - Italiano (it)
        5 - Chino (zh)
        6 - Talago (tl)
        0 - Cancelar
        """);

        System.out.print("Elija una opción deseada: ");
        int opcion = teclado.nextInt();
        teclado.nextLine();

        if (opcion == 0) {
            System.out.println(" Gracias por usar nuestro sistema.");
            return;
        }

        if (!opcionesIdiomas.containsKey(opcion)) {
            System.out.println( "Opción inválida favor de verificar.");
            return;
        }

        Map.Entry<String, String> idiomaSeleccionado = opcionesIdiomas.get(opcion);
        String codigoIdioma = idiomaSeleccionado.getKey();
        String nombreIdioma = idiomaSeleccionado.getValue();

        List<Libro> librosEnIdioma = libroRepository.findByContainingIdioma(codigoIdioma);

        if (librosEnIdioma.isEmpty()) {
            System.out.println("No se encontraron libros en " + nombreIdioma + " con el codigo: " + codigoIdioma);
        } else {
            System.out.println("Libros encontrados en " + nombreIdioma + "con el codigo: " + codigoIdioma + ": ");
            librosEnIdioma.forEach(libro -> {
                System.out.println("Nombre del Libro: " + libro.getTitulo());
            });
        }
    }

}


