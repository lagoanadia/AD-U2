package ad.taller;

import java.time.LocalDate;

/**
 * Una reserva del taller. Es EL MISMO record de la unidad 1: el dato no ha cambiado.
 * Lo que cambia en esta unidad es dónde se guarda: antes en reservas.json, ahora en PostgreSQL.
 *
 * @param id       la referencia que ve el cliente: R001 … R180
 * @param cliente  el nombre del cliente, tal como venía en el JSON
 * @param fecha    el día de la cita
 * @param personas cuántas personas vienen (siempre más de 0)
 */
public record Reserva(String id, String cliente, LocalDate fecha, int personas) { }
