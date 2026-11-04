package ad.taller;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Hacer una reserva nueva son DOS cambios en la base: restar las plazas del día e insertar
 * la reserva. Tienen que pasar los dos o ninguno: eso es una transacción.
 *
 * Y hay un segundo problema: dos personas reservando el mismo día a la vez. Las dos leen
 * «quedan 3 plazas», las dos restan 2 y el taller acaba con 4 personas para 3 plazas.
 * SELECT … FOR UPDATE bloquea la fila del día hasta que la transacción termina: la segunda
 * espera a que la primera acabe y entonces ve las plazas que quedan de verdad.
 */
public class Reservar {

    private final DataSource ds;

    public Reservar(DataSource ds) { this.ds = ds; }

    /** Descuenta las plazas e inserta la reserva. Si no hay plazas, no cambia nada. */
    public void reservar(Reserva r) {
        try (Connection con = ds.getConnection()) {
            con.setAutoCommit(false);
            try {
                int libres = plazasLibresBloqueando(con, r);
                if (libres < r.personas()) {
                    throw new SQLException("Solo quedan " + libres + " plazas el " + r.fecha());
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE plazas SET libres = libres - ? WHERE fecha = ?")) {
                    ps.setInt(1, r.personas());
                    ps.setObject(2, r.fecha());
                    ps.executeUpdate();
                }
                long clienteId = RepositorioReservasJdbc.idDeCliente(con, r.cliente());
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO reserva (referencia, cliente_id, fecha, personas) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, r.id());
                    ps.setLong(2, clienteId);
                    ps.setObject(3, r.fecha());
                    ps.setInt(4, r.personas());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();                 // las plazas vuelven a estar como estaban
                throw new PersistenciaException("reservar " + r.id(), e);
            } finally {
                con.setAutoCommit(true);        // la conexión vuelve limpia al pool
            }
        } catch (SQLException e) {
            throw new PersistenciaException("abrir la conexión para reservar", e);
        }
    }

    /** ACTIVIDAD 3 · Anular una reserva devolviendo sus plazas, todo o nada. Se dirige a un agente. */
    public boolean anular(String id) {
        throw new UnsupportedOperationException("pendiente: actividad 3");
    }

    /**
     * Lee las plazas del día y deja la fila BLOQUEADA hasta el commit o el rollback.
     * Si el día no tiene fila todavía, se crea con el aforo completo (y queda bloqueada igual).
     */
    private static int plazasLibresBloqueando(Connection con, Reserva r) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT libres FROM plazas WHERE fecha = ? FOR UPDATE")) {
            ps.setObject(1, r.fecha());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO plazas (fecha, libres) VALUES (?, ?)")) {
            ps.setObject(1, r.fecha());
            ps.setInt(2, CargaInicial.AFORO_POR_DIA);
            ps.executeUpdate();
        }
        return CargaInicial.AFORO_POR_DIA;
    }
}
