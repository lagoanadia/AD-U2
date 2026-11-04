package ad.taller;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * El taller de reservas, paso 2: los datos en PostgreSQL.
 *
 * Antes de ejecutar:   docker compose up -d        (PostgreSQL 16 en localhost:5432)
 * Ejecutar:            mvn -q compile exec:java
 * Probar sin Docker:   mvn test                    (las pruebas usan H2, una base en memoria)
 *
 * Qué hace, en orden: crea el esquema con Flyway (V1, V2), carga las 180 reservas de la
 * unidad 1 si la tabla está vacía, y después enseña cada pieza de la capa de datos
 * funcionando: listar, buscar, el buscador inseguro frente al seguro, y una reserva nueva
 * dentro de una transacción.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        DataSource ds = Conexion.origen();
        Esquema.migrar(ds);
        System.out.println("Esquema al día (flyway_schema_history).");

        List<Reserva> delJson = CargaInicial.leerJson(Path.of("datos", "reservas.json"));
        if (tablaVacia(ds)) {
            int filas = CargaInicial.cargar(ds, delJson);
            System.out.println("Cargadas " + filas + " reservas de " + delJson.size() + " leídas del JSON.");
        } else {
            System.out.println("La tabla reserva ya tenía datos: no se vuelve a cargar.");
        }

        RepositorioReservasJdbc repositorio = new RepositorioReservasJdbc(ds);
        System.out.println("En la base hay " + repositorio.cargarTodos().size() + " reservas.");
        System.out.println("R042 = " + repositorio.buscar("R042").orElseThrow());
        System.out.println("Las de Xoán Bello: " + repositorio.deCliente("Xoán Bello").size());

        Buscador buscador = new Buscador(ds);
        String ataque = "x' OR '1'='1";
        System.out.println("Inseguro con «" + ataque + "»: " + buscador.contarInseguro(ataque) + " reservas");
        System.out.println("Seguro   con «" + ataque + "»: " + buscador.contarSeguro(ataque) + " reservas");

        Reservar reservar = new Reservar(ds);
        // La referencia es CHAR(4): R9 y dos cifras. Con cinco caracteres la base la rechaza (22001).
        Reserva nueva = new Reserva("R9" + String.format("%02d", System.currentTimeMillis() % 100),
                "Prueba de clase", LocalDate.of(2026, 10, 14), 2);
        reservar.reservar(nueva);
        System.out.println("Reservada " + nueva.id() + ". Ahora hay " + repositorio.cargarTodos().size() + ".");
        repositorio.eliminar(nueva.id());
        System.out.println("Y eliminada otra vez. Ahora hay " + repositorio.cargarTodos().size() + ".");
    }

    private static boolean tablaVacia(DataSource ds) throws Exception {
        try (Connection con = ds.getConnection()) {
            return CargaInicial.contar(con, "reserva") == 0;
        }
    }
}
