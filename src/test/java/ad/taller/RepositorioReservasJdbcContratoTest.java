package ad.taller;

/** El contrato, contra la base H2 en memoria con las 180 reservas cargadas. */
class RepositorioReservasJdbcContratoTest extends ConBaseEnMemoria implements ContratoRepositorio {

    @Override
    public Repositorio<Reserva> repositorio() { return new RepositorioReservasJdbc(ds); }
}
