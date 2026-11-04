package ad.taller;

import java.util.List;
import java.util.Optional;

/**
 * La frontera de la persistencia, igual que en la unidad 1: el resto del programa habla con
 * esta interfaz y no sabe si detrás hay un fichero o una base de datos.
 *
 * Dos métodos nuevos respecto a la unidad 1: guardar y eliminar UNA reserva. Con un fichero
 * había que reescribirlo entero (guardarTodos); una base de datos cambia una sola fila.
 */
public interface Repositorio<T> {
    List<T> cargarTodos();
    Optional<T> buscar(String id);
    void guardar(T elemento);
    boolean eliminar(String id);
}
