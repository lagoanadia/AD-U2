package ad.taller;

/** El contrato, en memoria: un repositorio nuevo y vacío para cada prueba. */
class RepositorioReservasMemoriaTest implements ContratoRepositorio {

    final RepositorioReservasMemoria repositorio = new RepositorioReservasMemoria();

    @Override
    public Repositorio<Reserva> repositorio() { return repositorio; }
}
