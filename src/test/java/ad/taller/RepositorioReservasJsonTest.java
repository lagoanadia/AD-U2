package ad.taller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** El contrato, sobre una COPIA de reservas.json en una carpeta temporal: el fichero de datos/ no se toca. */
class RepositorioReservasJsonTest implements ContratoRepositorio {

    @TempDir Path carpeta;
    RepositorioReservasJson repositorio;

    @BeforeEach
    void copia() throws Exception {
        Path fichero = carpeta.resolve("reservas.json");
        Files.copy(Path.of("datos", "reservas.json"), fichero);
        repositorio = new RepositorioReservasJson(fichero);
    }

    @Override
    public Repositorio<Reserva> repositorio() { return repositorio; }

    @Test
    @DisplayName("Lee las 180 reservas de la unidad 1")
    void leeElJson() {
        assertEquals(180, repositorio.cargarTodos().size());
    }

    @Test
    @DisplayName("Si el fichero no existe, lista vacía; al guardar, se crea")
    void sinFichero() {
        var nuevo = new RepositorioReservasJson(carpeta.resolve("no-existe.json"));
        assertTrue(nuevo.cargarTodos().isEmpty());
        nuevo.guardar(NUEVA);
        assertTrue(Files.exists(carpeta.resolve("no-existe.json")));
        assertEquals(1, nuevo.cargarTodos().size());
    }
}
