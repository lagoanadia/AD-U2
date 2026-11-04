# El taller de reservas · paso 2 · los datos en PostgreSQL

Código de referencia de AD-UD2. **Es el que se explica en clase y el que recibe el alumnado
entero**: las actividades consisten en leerlo, tocarlo y dirigir a un agente sobre él
(`MODELO-DE-TRABAJO.md`). Probado el 02/10/2026 con Java 21, Maven, PostgreSQL 16 en Docker y H2.

## Ponerlo en marcha

```bash
docker compose up -d           # PostgreSQL 16 en localhost:5432 (DB_PORT=5433 si está ocupado)
mvn -q compile exec:java       # crea el esquema, carga las 180 reservas y recorre la capa de datos
mvn test                       # 32 pruebas con H2 en memoria, sin Docker: 28 pasan y 4 esperan a las actividades
```

Si cambias el puerto: `export DB_URL=jdbc:postgresql://localhost:5433/taller` antes de ejecutar.

## Qué hay, y en qué orden leerlo

| Fichero | Qué es | Bloque |
|---|---|---|
| `datos/reservas.json` | Las 180 reservas de la unidad 1, sin tocar | — |
| `db/migration/V1__esquema_inicial.sql` | Las tablas `cliente` y `reserva`; el cliente sale a su tabla | T2 |
| `db/migration/V2__plazas_por_dia.sql` | La tabla `plazas`; llegó después y por eso es otra migración | T2 |
| `Reserva.java` | El mismo record de la UD1: el dato no cambia, cambia dónde se guarda | T1 |
| `Repositorio.java` | La frontera de la UD1, con `guardar` y `eliminar` de una sola fila | T1 · T9 |
| `Conexion.java` | Credenciales del entorno y pool (HikariCP); qué conector es y por qué | T1 · T2 · T8 |
| `Esquema.java` | Flyway: aplicar las migraciones que falten | T2 |
| `CargaInicial.java` | JSON → tablas, en lote y en una transacción; el recuento manda | T3 · T5 · T7 |
| `RepositorioReservasJdbc.java` | La capa de datos: `SELECT` con `JOIN`, `PreparedStatement`, claves generadas, `fila()` | T3 · T4 · T9 |
| `RepositorioReservasMemoria.java` | La misma interfaz sobre un mapa: sin fichero ni base | T9 |
| `RepositorioReservasJson.java` | La misma interfaz sobre el fichero JSON de la UD1, con escritura atómica | T9 |
| `PersistenciaException.java` | El error de la capa de datos, venga de SQL, de un fichero o de una reserva repetida | T2 · T9 |
| `Buscador.java` | La inyección de SQL, con el método inseguro al lado del seguro | T3 |
| `Reservar.java` | La transacción de las plazas, con `FOR UPDATE` contra la carrera | T5 · T6 |
| `Main.java` | El recorrido completo, ejecutable | — |
| `Fuentes.java` | El mismo programa con las tres fuentes: se elige con `FUENTE` | T9 |
| `src/test/…` | 32 pruebas; `ContratoRepositorio` es la misma prueba para las tres fuentes; `Actividad2Test` y `Actividad3Test` están en `@Disabled` hasta que toque | — |

Dos métodos lanzan `UnsupportedOperationException` a propósito: `deFecha` (actividad 2, tocar) y
`anular` (actividad 3, dirigir). Sus pruebas ya están escritas. Las soluciones del profesor están
en `../soluciones/`, fuera de esta carpeta, porque esta carpeta entera va al alumnado.

## Tres fuentes, una interfaz (T9, añadido el 02/11/2026)

```bash
FUENTE=memoria mvn -q compile exec:java -Dexec.mainClass=ad.taller.Fuentes   # empieza vacía
FUENTE=json    mvn -q compile exec:java -Dexec.mainClass=ad.taller.Fuentes   # datos/reservas.json
mvn -q compile exec:java -Dexec.mainClass=ad.taller.Fuentes                  # PostgreSQL (docker compose up -d antes)
```

En PowerShell: `$env:FUENTE="json"; mvn -q compile exec:java "-Dexec.mainClass=ad.taller.Fuentes"`.

Ojo: con `FUENTE=json` se escribe de verdad en `datos/reservas.json` (guarda R900 y la vuelve a borrar).
Las pruebas del JSON trabajan sobre una copia en una carpeta temporal.

## Lo que no prueba H2

Las pruebas corren sobre H2 en modo PostgreSQL para que no hagan falta ni Docker ni red. Lo que
H2 no reproduce igual es el comportamiento de PostgreSQL con bloqueos y dos conexiones a la vez:
eso se enseña en clase contra el contenedor, con dos terminales.
