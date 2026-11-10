# control-presencia

## Modelo de dominio
- `Person`: id y nombre.
- `ClockEvent`: fichaje inmutable (persona, instante, tipo CLOCK_IN/CLOCK_OUT, origen).
- `TimeSlot`: franja horaria; si el fin es anterior al inicio cruza la medianoche.
- `Schedule` (interfaz) con tres implementaciones:
    - `ContinuousSchedule`: un tramo en ciertos días (oficina).
    - `SplitSchedule`: varios tramos sin solapes (tienda/restaurante).
    - `ShiftSchedule`: ciclo rotatorio de `Shift`; una guardia es un turno que cruza medianoche (hospital).
- `ClockEventValidator`: rechaza salida sin entrada, fichaje duplicado e incoherente.
- `PresenceEvaluator`: calcula estado, minutos trabajados, desviación e incidencias.

## Reglas de cómputo
- Tolerancia configurable en minutos para entrada y salida.
- Retraso: primera entrada posterior al inicio + tolerancia.
- Salida temprana: última salida anterior al fin - tolerancia.
- Horas trabajadas: suma de pares CLOCK_IN/CLOCK_OUT; una entrada abierta cuenta hasta "ahora".