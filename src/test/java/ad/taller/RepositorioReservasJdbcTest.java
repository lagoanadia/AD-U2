package ad.taller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioReservasJdbcTest extends ConBaseEnMemoria {

    final RepositorioReservasJdbc repositorio = new RepositorioReservasJdbc(ds);

    @Test
    @DisplayName("cargarTodos devuelve las mismas reservas que había en el JSON, en el mismo orden")
    void idaYVuelta() {
        assertEquals(delJson, repositorio.cargarTodos());
    }

    @Test
    @DisplayName("buscar: la que existe viene entera; la que no, Optional vacío (no null, no excepción)")
    void buscar() {
        assertEquals(new Reserva("R001", "David Rey", LocalDate.of(2026, 10, 11), 5), repositorio.buscar("R001").orElseThrow());
        assertTrue(repositorio.buscar("R999").isEmpty());
    }

    @Test
    @DisplayName("guardar y eliminar: una fila, no el fichero entero")
    void guardarYEliminar() {
        Reserva nueva = new Reserva("R500", "Cliente nuevo", LocalDate.of(2026, 12, 2), 2);
        repositorio.guardar(nueva);
        assertEquals(nueva, repositorio.buscar("R500").orElseThrow());
        assertTrue(repositorio.eliminar("R500"));
        assertFalse(repositorio.eliminar("R500"), "la segunda vez ya no existe: 0 filas afectadas");
    }

    @Test
    @DisplayName("Una referencia repetida la rechaza la BASE (UNIQUE), no el programa: estado 23505")
    void referenciaDuplicada() {
        PersistenciaException e = assertThrows(PersistenciaException.class,
                () -> repositorio.guardar(new Reserva("R001", "Otro", LocalDate.of(2026, 12, 3), 1)));
        assertTrue(e.getMessage().contains("23505"), e.getMessage());
    }

    @Test
    @DisplayName("deCliente trae solo las suyas, ordenadas por fecha")
    void deCliente() {
        var suyas = repositorio.deCliente("Xoán Bello");
        long esperadas = delJson.stream().filter(r -> r.cliente().equals("Xoán Bello")).count();
        assertEquals(esperadas, suyas.size());
        for (int i = 1; i < suyas.size(); i++) {
            assertFalse(suyas.get(i).fecha().isBefore(suyas.get(i - 1).fecha()));
        }
    }
}
