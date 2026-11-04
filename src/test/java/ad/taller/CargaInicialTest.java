package ad.taller;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CargaInicialTest extends ConBaseEnMemoria {

    @Test
    @DisplayName("Entran las 180 reservas y los 26 clientes distintos")
    void recuentos() throws Exception {
        try (var con = ds.getConnection()) {
            assertEquals(180, CargaInicial.contar(con, "reserva"));
            assertEquals(26, CargaInicial.contar(con, "cliente"));
        }
    }

    @Test
    @DisplayName("R042 conserva las comillas y el & del nombre: los datos no se tocan")
    void r042Intacta() {
        Reserva r = new RepositorioReservasJdbc(ds).buscar("R042").orElseThrow();
        assertEquals("Talleres \"O Rápido\" & Cía", r.cliente());
        assertEquals(LocalDate.of(2026, 11, 5), r.fecha());
        assertEquals(3, r.personas());
    }

    @Test
    @DisplayName("Si una fila falla, no entra NINGUNA: la carga es una transacción")
    void cargaAMediasNoQueda() throws Exception {
        JdbcDataSource otra = new JdbcDataSource();           // una base aparte, vacía
        otra.setURL("jdbc:h2:mem:cargaRota;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        otra.setUser("sa");
        Esquema.migrar(otra);
        List<Reserva> conUnaRota = new ArrayList<>(delJson);
        conUnaRota.add(new Reserva("R999", "Cliente roto", LocalDate.of(2026, 12, 1), 0));   // CHECK (personas > 0)

        assertThrows(PersistenciaException.class, () -> CargaInicial.cargar(otra, conUnaRota));
        try (var con = otra.getConnection()) {
            assertEquals(0, CargaInicial.contar(con, "reserva"), "la tabla tiene que quedar vacía, no a medias");
            assertEquals(0, CargaInicial.contar(con, "cliente"));
        }
    }
}
