-- V2 · Cuántas plazas quedan libres cada día.
-- Es una migración aparte porque llegó después: V1 ya estaba aplicada y no se toca.

CREATE TABLE plazas (
    fecha   DATE  PRIMARY KEY,
    libres  INT   NOT NULL CHECK (libres >= 0)   -- nunca puede quedar en negativo
);
