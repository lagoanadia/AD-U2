package ad.taller;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * El Repositorio de la unidad 1, cumplido con JDBC. El resto del programa sigue viendo
 * Reserva(id, cliente, fecha, personas); aquí dentro eso se convierte en DOS tablas
 * (cliente y reserva) unidas por una clave ajena. Ese salto es el desfase objeto-relacional.
 *
 * Reglas que se repiten en todos los métodos:
 *  - la conexión se PIDE al pool y se DEVUELVE al salir del try-with-resources;
 *  - todo parámetro va con PreparedStatement y «?», nunca pegado al texto del SQL;
 *  - la SQLException se traduce a PersistenciaException con la operación que fallaba.
 */
public class RepositorioReservasJdbc implements Repositorio<Reserva> {

    /** Las columnas que forman una Reserva, con el nombre del cliente traído por el JOIN. */
    private static final String SELECT =
            "SELECT r.referencia, c.nombre, r.fecha, r.personas "
          + "FROM reserva r JOIN cliente c ON c.id = r.cliente_id ";

    private final DataSource ds;

    public RepositorioReservasJdbc(DataSource ds) { this.ds = ds; }

    @Override
    public List<Reserva> cargarTodos() {
        try (Connection con = ds.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT + "ORDER BY r.referencia")) {
            List<Reserva> lista = new ArrayList<>();
            while (rs.next()) lista.add(fila(rs));
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("cargar las reservas", e);
        }
    }

    @Override
    public Optional<Reserva> buscar(String id) {
        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE r.referencia = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(fila(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("buscar la reserva " + id, e);
        }
    }

    /** Las reservas de un cliente, por fecha. No está en la interfaz: es una pregunta del taller. */
    public List<Reserva> deCliente(String nombre) {
        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE c.nombre = ? ORDER BY r.fecha")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reserva> lista = new ArrayList<>();
                while (rs.next()) lista.add(fila(rs));
                return lista;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("buscar las reservas de " + nombre, e);
        }
    }

    /** ACTIVIDAD 2 · Las reservas de un día. Se implementa en la actividad; la prueba ya existe. */
    public List<Reserva> deFecha(LocalDate dia) {
        throw new UnsupportedOperationException("pendiente: actividad 2");
    }

    @Override
    public void guardar(Reserva r) {
        try (Connection con = ds.getConnection()) {
            long clienteId = idDeCliente(con, r.cliente());      // existe o se crea
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO reserva (referencia, cliente_id, fecha, personas) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, r.id());
                ps.setLong(2, clienteId);
                ps.setObject(3, r.fecha());          // LocalDate -> DATE, sin convertir a mano
                ps.setInt(4, r.personas());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("guardar la reserva " + r.id(), e);
        }
    }

    @Override
    public boolean eliminar(String id) {
        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM reserva WHERE referencia = ?")) {
            ps.setString(1, id);
            return ps.executeUpdate() == 1;      // filas afectadas: 0 si no existía
        } catch (SQLException e) {
            throw new PersistenciaException("eliminar la reserva " + id, e);
        }
    }

    /**
     * Devuelve el id del cliente con ese nombre, creándolo si no existe. La clave la genera la
     * base (BIGSERIAL): hay que pedírsela con RETURN_GENERATED_KEYS, no inventarla.
     */
    static long idDeCliente(Connection con, String nombre) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM cliente WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO cliente (nombre) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getLong(1);
            }
        }
    }

    /** Una fila del ResultSet -> una Reserva. Es el único sitio donde se nombran las columnas. */
    static Reserva fila(ResultSet rs) throws SQLException {
        return new Reserva(
                rs.getString("referencia").trim(),         // CHAR(4) rellena con espacios
                rs.getString("nombre"),
                rs.getObject("fecha", LocalDate.class),
                rs.getInt("personas"));
    }
}
