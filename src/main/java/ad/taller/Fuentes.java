package ad.taller;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * La misma interfaz con tres fuentes de datos (AD-UD2-T9). La fuente se elige al arrancar con la
 * variable de entorno FUENTE; de la línea del switch para abajo, el programa no sabe cuál le ha tocado.
 *
 *   FUENTE=memoria  mvn -q compile exec:java -Dexec.mainClass=ad.taller.Fuentes   (empieza vacía)
 *   FUENTE=json     mvn -q compile exec:java -Dexec.mainClass=ad.taller.Fuentes   (datos/reservas.json)
 *   FUENTE=postgres mvn -q compile exec:java -Dexec.mainClass=ad.taller.Fuentes   (docker compose up -d antes)
 *
 * En Windows (PowerShell): $env:FUENTE="json"; mvn -q compile exec:java "-Dexec.mainClass=ad.taller.Fuentes"
 */
public class Fuentes {

    public static void main(String[] args) {
        Repositorio<Reserva> repo = switch (System.getenv().getOrDefault("FUENTE", "postgres")) {
            case "memoria" -> new RepositorioReservasMemoria();
            case "json"    -> new RepositorioReservasJson(Path.of("datos", "reservas.json"));
            default        -> {
                DataSource ds = Conexion.origen();
                Esquema.migrar(ds);
                yield new RepositorioReservasJdbc(ds);
            }
        };

        // De aquí para abajo, el programa no sabe qué fuente le ha tocado
        System.out.println(repo.cargarTodos().size() + " reservas");
        repo.guardar(new Reserva("R900", "Prueba", LocalDate.of(2026, 11, 2), 2));
        System.out.println(repo.buscar("R900").orElseThrow());
        repo.eliminar("R900");
    }
}
