package ad.taller;

import java.sql.SQLException;

/**
 * Lo que lanza la capa de datos cuando algo falla. Envuelve la SQLException (que es comprobada)
 * en una excepción que no lo es, con la operación y el código de estado SQL en el mensaje:
 * «23503» es clave ajena rota, «23505» es duplicado, «23514» es un CHECK que no se cumple.
 */
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String operacion, SQLException causa) {
        super("No se pudo " + operacion + " (estado " + causa.getSQLState() + "): " + causa.getMessage(), causa);
    }

    /** Para el fichero JSON: la causa es una IOException, sin estado SQL. */
    public PersistenciaException(String operacion, Exception causa) {
        super("No se pudo " + operacion + ": " + causa.getMessage(), causa);
    }

    /** Para lo que no viene de ninguna excepción, como una reserva repetida en memoria o en el JSON. */
    public PersistenciaException(String operacion) {
        super("No se pudo " + operacion);
    }
}
