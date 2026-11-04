package ad.taller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReservarTest extends ConBaseEnMemoria {

    final Reservar reservar = new Reservar(ds);
    final RepositorioReservasJdbc repositorio = new RepositorioReservasJdbc(ds);
    static final LocalDate DIA = LocalDate.of(2026, 11, 5);     // el día de R042

    @Test
    @DisplayName("Reservar resta las plazas del día e inserta la reserva")
    void reservaNormal() throws Exception {
        int antes = plazasLibres(DIA);
        reservar.reservar(new Reserva("R600", "Cliente de prueba", DIA, 2));
        assertEquals(antes - 2, plazasLibres(DIA));
        assertTrue(repositorio.buscar("R600").isPresent());
    }

    @Test
    @DisplayName("Sin plazas suficientes no cambia NADA: ni las plazas ni la reserva (rollback)")
    void sinPlazasNoCambiaNada() throws Exception {
        int antes = plazasLibres(DIA);
        PersistenciaException e = assertThrows(PersistenciaException.class,
                () -> reservar.reservar(new Reserva("R601", "Cliente de prueba", DIA, antes + 1)));
        assertTrue(e.getMessage().contains("plazas"), e.getMessage());
        assertEquals(antes, plazasLibres(DIA), "las plazas tienen que estar como estaban");
        assertTrue(repositorio.buscar("R601").isEmpty(), "la reserva no puede haberse colado");
    }

    @Test
    @DisplayName("Un día sin fila en plazas empieza con el aforo completo")
    void diaNuevo() throws Exception {
        LocalDate libre = LocalDate.of(2027, 1, 20);
        assertEquals(-1, plazasLibres(libre));
        reservar.reservar(new Reserva("R602", "Cliente de prueba", libre, 4));
        assertEquals(CargaInicial.AFORO_POR_DIA - 4, plazasLibres(libre));
    }
}
