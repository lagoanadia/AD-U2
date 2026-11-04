package ad.taller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Lleva las 180 reservas de reservas.json (unidad 1) a PostgreSQL. Una sola vez.
 *
 * Es una TRANSACCIÓN: o entran todas o no entra ninguna. Si a mitad falla una fila, se hace
 * rollback y las tablas se quedan como estaban (vacías). Una carga a medias es peor que ninguna.
 *
 * El orden importa: primero los clientes, porque la reserva lleva una clave ajena que apunta a
 * ellos. Al revés, PostgreSQL responde con el estado 23503.
 */
public final class CargaInicial {

    /** Plazas que ofrece el taller cada día. Lo que no está reservado queda libre. */
    public static final int AFORO_POR_DIA = 30;

    private CargaInicial() { }

    public static List<Reserva> leerJson(Path fichero) throws IOException {
        ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
        return List.of(json.readValue(fichero.toFile(), Reserva[].class));
    }

    /** Carga las reservas y devuelve cuántas filas quedaron en la tabla reserva. */
    public static int cargar(DataSource ds, List<Reserva> reservas) {
        try (Connection con = ds.getConnection()) {
            con.setAutoCommit(false);                        // aquí empieza la transacción
            try {
                Map<String, Long> ids = insertarClientes(con, reservas);
                insertarReservas(con, reservas, ids);
                insertarPlazas(con, reservas);
                int filas = contar(con, "reserva");
                if (filas != reservas.size()) {              // el recuento manda
                    throw new SQLException("Leídas " + reservas.size() + " reservas y guardadas " + filas);
                }
                con.commit();                                // ahora sí: todas a la vez
                return filas;
            } catch (SQLException e) {
                con.rollback();                              // nada de lo anterior queda
                throw new PersistenciaException("cargar las reservas iniciales", e);
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("abrir la conexión para la carga", e);
        }
    }

    /** Un cliente por cada nombre DISTINTO, y el id que le da la base para usarlo después. */
    private static Map<String, Long> insertarClientes(Connection con, List<Reserva> reservas) throws SQLException {
        Map<String, Long> ids = new HashMap<>();
        TreeSet<String> nombres = new TreeSet<>();
        for (Reserva r : reservas) nombres.add(r.cliente());
        for (String nombre : nombres) {
            ids.put(nombre, RepositorioReservasJdbc.idDeCliente(con, nombre));
        }
        return ids;
    }

    /** Las 180 reservas en UN viaje a la base (lote), no en 180. */
    private static void insertarReservas(Connection con, List<Reserva> reservas, Map<String, Long> ids) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO reserva (referencia, cliente_id, fecha, personas) VALUES (?, ?, ?, ?)")) {
            for (Reserva r : reservas) {
                ps.setString(1, r.id());
                ps.setLong(2, ids.get(r.cliente()));
                ps.setObject(3, r.fecha());
                ps.setInt(4, r.personas());
                ps.addBatch();                               // se acumula…
            }
            ps.executeBatch();                               // …y se envía todo junto
        }
    }

    /** Para cada día con reservas, las plazas que quedan: el aforo menos lo ya reservado. */
    private static void insertarPlazas(Connection con, List<Reserva> reservas) throws SQLException {
        Map<LocalDate, Integer> ocupadas = new HashMap<>();
        for (Reserva r : reservas) ocupadas.merge(r.fecha(), r.personas(), Integer::sum);
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO plazas (fecha, libres) VALUES (?, ?)")) {
            for (Map.Entry<LocalDate, Integer> dia : ocupadas.entrySet()) {
                ps.setObject(1, dia.getKey());
                ps.setInt(2, AFORO_POR_DIA - dia.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    static int contar(Connection con, String tabla) throws SQLException {
        // El nombre de la tabla no puede ir con «?»: solo se admiten los dos nombres conocidos.
        if (!tabla.equals("reserva") && !tabla.equals("cliente")) throw new IllegalArgumentException(tabla);
        try (Statement st = con.createStatement();
             var rs = st.executeQuery("SELECT count(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
