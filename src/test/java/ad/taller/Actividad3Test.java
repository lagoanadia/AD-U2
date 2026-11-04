package ad.taller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ACTIVIDAD 3 · DIRIGIR. El cambio lo pide cada uno a un agente: «anular una reserva por su
 * referencia devolviendo sus plazas al día, todo o nada». Esta prueba es el juez: lo que el
 * agente devuelva tiene que pasarla. Y la lista de comprobación de la actividad dice qué más
 * hay que mirar aunque pase.
 */
@Disabled("actividad 3: quita esta línea para empezar")
class Actividad3Test extends ConBaseEnMemoria {

    final Reservar reservar = new Reservar(ds);
    final RepositorioReservasJdbc repositorio = new RepositorioReservasJdbc(ds);
    static final LocalDate DIA = LocalDate.of(2026, 11, 5);

    @Test
    @DisplayName("Anular devuelve las plazas y borra la reserva")
    void anularDevuelvePlazas() throws Exception {
        int antes = plazasLibres(DIA);
        reservar.reservar(new Reserva("R700", "Cliente de prueba", DIA, 3));
        assertEquals(antes - 3, plazasLibres(DIA));

        assertTrue(reservar.anular("R700"));
        assertEquals(antes, plazasLibres(DIA), "las 3 plazas tienen que volver");
        assertTrue(repositorio.buscar("R700").isEmpty());
    }

    @Test
    @DisplayName("Anular una referencia que no existe devuelve false y no toca las plazas")
    void anularInexistente() throws Exception {
        int antes = plazasLibres(DIA);
        assertFalse(reservar.anular("R999"));
        assertEquals(antes, plazasLibres(DIA));
    }
}
