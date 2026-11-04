package ad.taller;

import org.flywaydb.core.Flyway;
import javax.sql.DataSource;

/**
 * Crea o actualiza el esquema con Flyway. Las migraciones viven en
 * src/main/resources/db/migration/ y se aplican en orden: V1, V2…
 *
 * Flyway apunta en la tabla flyway_schema_history cuáles ya se aplicaron, así que llamar a
 * migrar() dos veces no hace nada la segunda. Y si alguien EDITA una migración ya aplicada,
 * Flyway se niega a arrancar: la suma de comprobación no coincide. Eso es lo que queremos.
 */
public final class Esquema {

    private Esquema() { }

    public static void migrar(DataSource ds) {
        Flyway.configure()
              .dataSource(ds)
              .load()
              .migrate();
    }
}
