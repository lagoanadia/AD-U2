package ad.taller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Las reservas en un mapa: sin fichero ni base. Se pierden al cerrar el programa. */
public class RepositorioReservasMemoria implements Repositorio<Reserva> {

    private final Map<String, Reserva> datos = new TreeMap<>();   // ordenadas por id

    @Override
    public List<Reserva> cargarTodos() {
        return new ArrayList<>(datos.values());     // una copia: nadie toca el mapa desde fuera
    }

    @Override
    public Optional<Reserva> buscar(String id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public void guardar(Reserva r) {
        if (datos.putIfAbsent(r.id(), r) != null)
            throw new PersistenciaException("guardar la reserva " + r.id() + ": ya existe");
    }

    @Override
    public boolean eliminar(String id) {
        return datos.remove(id) != null;
    }
}
