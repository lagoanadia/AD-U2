package ad.taller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ACTIVIDAD 2 · TOCAR. La prueba ya está escrita: quita la línea @Disabled, verás que falla,
 * e implementa RepositorioReservasJdbc.deFecha hasta que pase. Son unas diez líneas y ya
 * tienes un método casi igual en la misma clase.
 */
@Disabled("actividad 2: quita esta línea para empezar")
class Actividad2Test extends ConBaseEnMemoria {

    final RepositorioReservasJdbc repositorio = new RepositorioReservasJdbc(ds);

    @Test
    @DisplayName("deFecha trae las reservas de ese día y solo las de ese día")
    void lasDeUnDia() {
        LocalDate dia = LocalDate.of(2026, 11, 5);
        var delDia = repositorio.deFecha(dia);
        long esperadas = delJson.stream().filter(r -> r.fecha().equals(dia)).count();
        assertEquals(esperadas, delDia.size());
        assertTrue(delDia.stream().allMatch(r -> r.fecha().equals(dia)));
        assertTrue(delDia.stream().anyMatch(r -> r.id().equals("R042")));
    }

    @Test
    @DisplayName("Un día sin reservas devuelve la lista vacía, no null")
    void diaVacio() {
        assertEquals(0, repositorio.deFecha(LocalDate.of(2030, 1, 1)).size());
    }
}
