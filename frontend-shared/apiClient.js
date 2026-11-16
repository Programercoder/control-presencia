export class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

async function request(baseUrl, path, options = {}) {
  let response;
  try {
    response = await fetch(baseUrl + path, {
      headers: { "Content-Type": "application/json" },
      ...options,
    });
  } catch (e) {
    throw new ApiError(0, "NETWORK", "No se puede conectar con el servidor");
  }
  let body = null;
  try {
    body = await response.json();
  } catch (e) {
    body = null;
  }
  if (!response.ok) {
    throw new ApiError(
      response.status,
      body && body.code ? body.code : "ERROR",
      body && body.message ? body.message : "Error " + response.status
    );
  }
  return body;
}


export function createHttpApi(baseUrl) {
  const post = (path, data) => request(baseUrl, path, { method: "POST", body: JSON.stringify(data) });
  const get = (path) => request(baseUrl, path);
  return {
    listPeople: () => get("/people"),
    listSchedules: () => get("/schedules"),
    defineSchedule: (schedule) => post("/schedules", schedule),
    assignSchedule: (assignment) => post("/assignments", assignment),
    registerClockEvent: (personId, type, source) => post("/clock-events", { personId, type, source }),
    listClockEvents: (personId, from, to) =>
      get("/clock-events?personId=" + encodeURIComponent(personId) + "&from=" + from + "&to=" + to),
    getPresence: (personId, date) =>
      get("/presence/" + encodeURIComponent(personId) + (date ? "?date=" + date : "")),
    justifyIncident: (justification) => post("/justifications", justification),
    listJustifications: (personId, from, to) =>
      get("/justifications?personId=" + encodeURIComponent(personId) + "&from=" + from + "&to=" + to),
  };
}

export function todayIso() {
  const d = new Date();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return d.getFullYear() + "-" + month + "-" + day;
}

export const STATUS_LABELS = {
  NO_SHIFT: "Sin jornada hoy",
  PENDING: "Pendiente de entrada",
  PRESENT: "Presente",
  LATE: "Retraso (sin fichar)",
  ABSENT: "Ausente",
  CLOCK_OUT_PENDING: "Salida pendiente",
  COMPLETED: "Jornada completada",
  WITH_INCIDENT: "Completada con incidencia",
};

export const INCIDENT_LABELS = {
  LATE_ARRIVAL: "Retraso",
  EARLY_DEPARTURE: "Salida temprana",
  ABSENCE: "Ausencia",
  CLOCK_OUT_PENDING: "Salida sin fichar",
};