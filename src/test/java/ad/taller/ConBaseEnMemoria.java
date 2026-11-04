package ad.taller;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.List;

/**
 * Lo común a todas las pruebas: una base H2 EMBEBIDA, en memoria, nueva para cada clase de
 * pruebas, con el esquema de Flyway y las 180 reservas cargadas.
 *
 * Por qué H2 y no el PostgreSQL de Docker: las pruebas tienen que poder correr en cualquier
 * equipo, en un segundo, sin arrancar nada. H2 en «modo PostgreSQL» entiende el mismo SQL de
 * las migraciones. Lo que NO prueba H2 es el comportamiento exacto de PostgreSQL con bloqueos
 * y concurrencia: eso se mira en clase contra el contenedor.
 */
abstract class ConBaseEnMemoria {

    static DataSource ds;
    static List<Reserva> delJson;

    @BeforeAll
    static void baseNueva(org.junit.jupiter.api.TestInfo info) throws Exception {
        JdbcDataSource h2 = new JdbcDataSource();
        String nombre = info.getTestClass().map(Class::getSimpleName).orElse("pruebas");
        h2.setURL("jdbc:h2:mem:" + nombre + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        ds = h2;
        Esquema.migrar(ds);
        delJson = CargaInicial.leerJson(Path.of("datos", "reservas.json"));
        CargaInicial.cargar(ds, delJson);
    }

    /** Cuántas plazas libres quedan un día, leído directamente de la tabla. */
    static int plazasLibres(java.time.LocalDate dia) throws Exception {
        try (var con = ds.getConnection();
             var ps = con.prepareStatement("SELECT libres FROM plazas WHERE fecha = ?")) {
            ps.setObject(1, dia);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
}
