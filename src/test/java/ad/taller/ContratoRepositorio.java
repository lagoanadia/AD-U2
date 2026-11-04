package ad.taller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * El contrato de la interfaz Repositorio, escrito UNA vez. Cada implementación (memoria, JSON y
 * PostgreSQL) lo hereda y solo dice cómo se crea su repositorio. Si una fuente se comporta distinto
 * que las otras, falla aquí: así se sabe que cambiar de fuente no cambia nada por fuera.
 *
 * Cada prueba deja el repositorio como lo encontró, porque la de PostgreSQL comparte la base.
 */
interface ContratoRepositorio {

    Repositorio<Reserva> repositorio();

    Reserva NUEVA = new Reserva("R500", "Cliente nuevo", LocalDate.of(2026, 12, 2), 2);

    @Test
    @DisplayName("Contrato · lo guardado se encuentra entero y cuenta en cargarTodos")
    default void guardarYBuscar() {
        Repositorio<Reserva> repo = repositorio();
        int antes = repo.cargarTodos().size();
        repo.guardar(NUEVA);
        try {
            assertEquals(NUEVA, repo.buscar("R500").orElseThrow());
            assertEquals(antes + 1, repo.cargarTodos().size());
        } finally {
            repo.eliminar("R500");
        }
    }

    @Test
    @DisplayName("Contrato · una reserva repetida da PersistenciaException, venga de donde venga")
    default void repetida() {
        Repositorio<Reserva> repo = repositorio();
        repo.guardar(NUEVA);
        try {
            assertThrows(PersistenciaException.class, () -> repo.guardar(NUEVA));
        } finally {
            repo.eliminar("R500");
        }
    }

    @Test
    @DisplayName("Contrato · eliminar dice true si estaba y false si ya no estaba")
    default void eliminar() {
        Repositorio<Reserva> repo = repositorio();
        repo.guardar(NUEVA);
        assertTrue(repo.eliminar("R500"));
        assertFalse(repo.eliminar("R500"));
        assertTrue(repo.buscar("R500").isEmpty());
    }

    @Test
    @DisplayName("Contrato · buscar una que no existe da Optional vacío (no null, no excepción)")
    default void noExiste() {
        assertTrue(repositorio().buscar("R999").isEmpty());
    }
}
