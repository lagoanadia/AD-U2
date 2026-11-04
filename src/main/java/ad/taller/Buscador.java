package ad.taller;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Dos formas de contar las reservas de un cliente. Una está MAL a propósito.
 *
 * contarInseguro pega el nombre al texto del SQL. Con un nombre normal funciona. Con el texto
 *   x' OR '1'='1   el SQL que llega a la base es   … WHERE c.nombre = 'x' OR '1'='1'
 * y cuenta TODAS las reservas. Con   x'; DROP TABLE reserva; --   la cosa es peor.
 * Eso es la inyección de SQL, y no es un fallo raro: es el fallo más explotado del mundo.
 *
 * contarSeguro usa «?»: el nombre viaja como DATO, nunca como parte de la orden. Lo que escriba
 * el usuario se busca tal cual, comillas incluidas.
 */
public class Buscador {

    private final DataSource ds;

    public Buscador(DataSource ds) { this.ds = ds; }

    /** NO HACER ESTO. Está aquí para que se vea fallar. */
    public int contarInseguro(String nombre) {
        String sql = "SELECT count(*) FROM reserva r JOIN cliente c ON c.id = r.cliente_id "
                   + "WHERE c.nombre = '" + nombre + "'";
        try (Connection con = ds.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new PersistenciaException("contar (inseguro) las reservas de " + nombre, e);
        }
    }

    public int contarSeguro(String nombre) {
        String sql = "SELECT count(*) FROM reserva r JOIN cliente c ON c.id = r.cliente_id "
                   + "WHERE c.nombre = ?";
        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("contar las reservas de " + nombre, e);
        }
    }
}
