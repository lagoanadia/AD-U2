# 🗄️ AD · UD2 — Taller de reservas con JDBC y PostgreSQL

Reference code for **Unit 2 of *Acceso a Datos*** (DAM): the reservations workshop from Unit 1, now storing its data in **PostgreSQL** through JDBC. It's the code explained in class and handed to students, who read it, modify it and direct an AI agent on it.

> Detailed walkthrough in Spanish: see [`LEEME.md`](LEEME.md).

## 📁 Project Structure

```
├── datos/reservas.json                 → The 180 reservations from Unit 1
├── docker-compose.yml                  → PostgreSQL 16 on localhost:5432
├── pom.xml                             → Maven: PostgreSQL driver, HikariCP, Flyway, Jackson, H2, JUnit 5
└── src/
    ├── main/java/ad/taller/
    │   ├── Main.java                       → Full runnable tour of the data layer
    │   ├── Conexion.java                   → Credentials from env + HikariCP pool
    │   ├── Esquema.java                    → Applies Flyway migrations
    │   ├── CargaInicial.java               → JSON → tables, batched in one transaction
    │   ├── Reserva.java                    → The reservation record (same as Unit 1)
    │   ├── Repositorio.java                → Repository interface
    │   ├── RepositorioReservasJdbc.java    → JDBC implementation (JOIN, PreparedStatement, generated keys)
    │   ├── RepositorioReservasMemoria.java → Same interface over a Map
    │   ├── RepositorioReservasJson.java    → Same interface over the JSON file (atomic writes)
    │   ├── Buscador.java                   → SQL injection: unsafe vs safe search side by side
    │   ├── Reservar.java                   → Booking transaction with SELECT … FOR UPDATE
    │   ├── Fuentes.java                    → Same program with 3 sources, chosen by FUENTE
    │   └── PersistenciaException.java      → Data-layer error type
    ├── main/resources/db/migration/
    │   ├── V1__esquema_inicial.sql         → cliente and reserva tables
    │   └── V2__plazas_por_dia.sql          → plazas table
    └── test/java/ad/taller/                → 32 tests on H2 (in-memory, PostgreSQL mode)
```

## 🚀 How It Works

`Main` runs the whole data layer in order:

1. Gets a pooled `DataSource` (HikariCP) with credentials from the environment
2. Runs **Flyway** migrations V1 and V2 if they're missing
3. If the `reserva` table is empty, loads the 180 reservations from JSON in a single batched transaction
4. Lists, searches by id and by client, shows the unsafe vs safe search, and makes a new booking inside a transaction

### One interface, three storages

`Repositorio` is implemented over PostgreSQL, an in-memory map and the JSON file. The same contract test (`ContratoRepositorio`) runs against all three, and `Fuentes` lets you switch with an env variable:

```bash
FUENTE=memoria mvn -q compile exec:java -Dexec.mainClass=ad.taller.Fuentes
FUENTE=json    mvn -q compile exec:java -Dexec.mainClass=ad.taller.Fuentes
```

### SQL injection, shown on purpose

`Buscador` has an unsafe method that concatenates user input into SQL next to a safe one using `PreparedStatement`, so the difference can be demonstrated live.

### Avoiding race conditions

`Reservar` locks the day's seats row with `SELECT … FOR UPDATE` inside a transaction, so two concurrent bookings can't overbook.

### Exercises built in

`deFecha` (activity 2) and `anular` (activity 3) throw `UnsupportedOperationException` on purpose; their tests are already written and marked `@Disabled`.

## 🧠 Key Concepts

- **JDBC** — `Connection`, `PreparedStatement`, `ResultSet`, generated keys
- **Connection pooling** — HikariCP
- **Schema migrations** — Flyway versioned scripts
- **Transactions** — commit/rollback, batch inserts, `FOR UPDATE` locking
- **SQL injection** — why parameters beat string concatenation
- **Repository pattern** — one interface, several storage back-ends
- **Testing** — JUnit 5 contract tests on H2 in PostgreSQL mode

## 🛠️ How To Run

Requires Java 21, Maven and Docker.

```bash
docker compose up -d
```

```bash
mvn -q compile exec:java
```

Tests don't need Docker (they use H2):

```bash
mvn test
```

If port 5432 is busy, start the container with `DB_PORT=5433` and `export DB_URL=jdbc:postgresql://localhost:5433/taller`.
