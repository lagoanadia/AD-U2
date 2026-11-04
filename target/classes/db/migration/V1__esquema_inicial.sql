-- V1 · El esquema del taller.
-- En reservas.json cada reserva repetía el NOMBRE del cliente. Aquí el cliente
-- sale a su propia tabla y la reserva lo señala con una clave ajena.

CREATE TABLE cliente (
    id      BIGSERIAL    PRIMARY KEY,          -- la clave la pone la base
    nombre  VARCHAR(80)  NOT NULL UNIQUE       -- no puede haber dos clientes con el mismo nombre
);

CREATE TABLE reserva (
    id          BIGSERIAL  PRIMARY KEY,
    referencia  CHAR(4)    NOT NULL UNIQUE,                    -- R001 … R180: el id del JSON
    cliente_id  BIGINT     NOT NULL REFERENCES cliente(id),    -- clave ajena
    fecha       DATE       NOT NULL,
    personas    INT        NOT NULL CHECK (personas > 0)       -- la base rechaza un 0 o un negativo
);

CREATE INDEX idx_reserva_fecha ON reserva(fecha);
