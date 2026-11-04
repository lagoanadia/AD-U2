package ad.taller;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

/**
 * De dónde salen las conexiones. Dos decisiones que se ven aquí:
 *
 *  1. Las credenciales vienen del ENTORNO (DB_URL, DB_USER, DB_PASSWORD). Los valores de respaldo
 *     solo sirven para el contenedor de desarrollo de docker-compose.yml. En un .java no va nunca
 *     una contraseña real.
 *  2. No se abre una conexión cada vez: un POOL (HikariCP) abre unas pocas al arrancar y las
 *     presta. Abrir una conexión a PostgreSQL cuesta decenas de milisegundos; prestarla, nada.
 *
 * El conector es el driver JDBC de PostgreSQL (org.postgresql:postgresql, licencia BSD). Es un
 * driver de tipo 4: Java puro, habla el protocolo de PostgreSQL directamente por la red, sin
 * nada instalado en el sistema operativo.
 */
public final class Conexion {

    private Conexion() { }

    /** El origen de datos de la aplicación: un pool sobre el PostgreSQL del entorno. */
    public static DataSource origen() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(env("DB_URL", "jdbc:postgresql://localhost:5432/taller"));
        ds.setUsername(env("DB_USER", "taller"));
        ds.setPassword(env("DB_PASSWORD", "taller"));
        ds.setMaximumPoolSize(5);          // para un taller sobra; un servidor web pediría más
        return ds;
    }

    private static String env(String clave, String porDefecto) {
        String v = System.getenv(clave);
        return v == null || v.isBlank() ? porDefecto : v;
    }
}
