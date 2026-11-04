package ad.taller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/** Las reservas en un fichero JSON, como en la UD1, pero con la interfaz de la UD2. */
public class RepositorioReservasJson implements Repositorio<Reserva> {

    private final Path fichero;
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    public RepositorioReservasJson(Path f) { this.fichero = f; }

    @Override
    public List<Reserva> cargarTodos() {
        if (!Files.exists(fichero)) return new ArrayList<>();
        try {
            Reserva[] leidas = json.readValue(fichero.toFile(), Reserva[].class);
            return new ArrayList<>(List.of(leidas));
        } catch (IOException e) {
            throw new PersistenciaException("leer " + fichero, e);
        }
    }

    @Override
    public Optional<Reserva> buscar(String id) {
        return cargarTodos().stream().filter(r -> r.id().equals(id)).findFirst();
    }

    @Override
    public void guardar(Reserva r) {
        List<Reserva> todas = cargarTodos();
        if (todas.stream().anyMatch(x -> x.id().equals(r.id())))
            throw new PersistenciaException("guardar la reserva " + r.id() + ": ya existe");
        todas.add(r);
        escribir(todas);                        // el fichero entero, otra vez
    }

    @Override
    public boolean eliminar(String id) {
        List<Reserva> todas = cargarTodos();
        boolean estaba = todas.removeIf(r -> r.id().equals(id));
        if (estaba) escribir(todas);
        return estaba;
    }

    /** La escritura atómica de la UD1: primero a un temporal y, si sale bien, se cambia por el bueno. */
    private void escribir(List<Reserva> todas) {
        try {
            Path dir = fichero.toAbsolutePath().getParent();
            Path tmp = Files.createTempFile(dir, "reservas", ".tmp");
            json.writeValue(tmp.toFile(), todas);
            Files.move(tmp, fichero, REPLACE_EXISTING, ATOMIC_MOVE);
        } catch (IOException e) {
            throw new PersistenciaException("escribir " + fichero, e);
        }
    }
}
