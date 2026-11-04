package ad.taller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuscadorTest extends ConBaseEnMemoria {

    final Buscador buscador = new Buscador(ds);
    static final String ATAQUE = "x' OR '1'='1";

    @Test
    @DisplayName("Con un nombre normal, los dos cuentan lo mismo")
    void nombreNormal() {
        long esperadas = delJson.stream().filter(r -> r.cliente().equals("Xoán Bello")).count();
        assertEquals(esperadas, buscador.contarSeguro("Xoán Bello"));
        assertEquals(esperadas, buscador.contarInseguro("Xoán Bello"));
    }

    @Test
    @DisplayName("Con el ataque, el inseguro devuelve TODAS las reservas: eso es la inyección")
    void inseguroInyectado() {
        assertEquals(180, buscador.contarInseguro(ATAQUE));
    }

    @Test
    @DisplayName("Con el ataque, el seguro busca un cliente que se llame así, literalmente: 0")
    void seguroNoSeInyecta() {
        assertEquals(0, buscador.contarSeguro(ATAQUE));
    }
}
