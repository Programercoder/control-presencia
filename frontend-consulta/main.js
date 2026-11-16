import { API_BASE_URL } from "../frontend-shared/config.js";
import { createHttpApi, todayIso, STATUS_LABELS, INCIDENT_LABELS } from "../frontend-shared/apiClient.js";

const api = createHttpApi(API_BASE_URL);
const $ = (id) => document.getElementById(id);
const MAX_DAYS = 62;

function showMessage(text, kind) {
  $("message").textContent = text;
  $("message").className = "message " + kind;
}

function clearMessage() {
  $("message").className = "message hidden";
}

function esc(text) {
  const d = document.createElement("div");
  d.textContent = text == null ? "" : String(text);
  return d.innerHTML;
}

function fmtMinutes(m) {
  const sign = m < 0 ? "-" : "";
  const abs = Math.abs(m);
  return sign + Math.floor(abs / 60) + "h " + String(abs % 60).padStart(2, "0") + "m";
}

function datesBetween(from, to) {
  const result = [];
  const d = new Date(from + "T00:00:00");
  const end = new Date(to + "T00:00:00");
  while (d <= end) {
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    result.push(d.getFullYear() + "-" + m + "-" + day);
    d.setDate(d.getDate() + 1);
  }
  return result;
}

function renderEvents(events) {
  const body = $("events");
  body.innerHTML = "";
  if (events.length === 0) {
    body.innerHTML = "<tr><td colspan='4'>Sin fichajes en el periodo</td></tr>";
    return;
  }
  events.forEach((e) => {
    const tr = document.createElement("tr");
    tr.innerHTML = "<td>" + e.timestamp.substring(0, 10) + "</td><td>" + e.timestamp.substring(11, 19) +
      "</td><td>" + (e.type === "CLOCK_IN" ? "Entrada" : "Salida") + "</td><td>" + esc(e.source) + "</td>";
    body.appendChild(tr);
  });
}

function incidentHtml(date, i) {
  const label = INCIDENT_LABELS[i.type] || i.type;
  if (i.justification) {
    return "<div class='incident justified'>" + esc(label) + " (" + i.minutes + " min) &#10003; Justificada: " +
      esc(i.justification) + "</div>";
  }
  return "<div class='incident'>" + esc(label) + " (" + i.minutes + " min)" +
    "<button class='small' data-date='" + date + "' data-type='" + i.type + "'>Justificar</button></div>";
}

function renderDaily(rows) {
  const body = $("daily");
  body.innerHTML = "";
  let days = 0, expected = 0, worked = 0, deviation = 0, incidents = 0, justified = 0;
  rows.forEach((r) => {
    const tr = document.createElement("tr");
    if (r.presence) {
      const p = r.presence;
      if (p.status !== "NO_SHIFT") {
        days++;
        expected += p.expectedMinutes;
        worked += p.workedMinutes;
        deviation += p.deviationMinutes;
      }
      incidents += p.incidents.length;
      justified += p.incidents.filter((i) => i.justification).length;
      const list = p.incidents.map((i) => incidentHtml(r.date, i)).join("");
      tr.innerHTML = "<td>" + r.date + "</td><td><span class='badge " + p.status + "'>" +
        (STATUS_LABELS[p.status] || p.status) + "</span></td><td>" + fmtMinutes(p.workedMinutes) +
        "</td><td>" + fmtMinutes(p.deviationMinutes) + "</td><td>" + (list || "-") + "</td>";
    } else {
      tr.innerHTML = "<td>" + r.date + "</td><td colspan='4'>" + esc(r.note) + "</td>";
    }
    body.appendChild(tr);
  });
  $("days").textContent = days;
  $("expected").textContent = fmtMinutes(expected);
  $("worked").textContent = fmtMinutes(worked);
  $("deviation").textContent = fmtMinutes(deviation);
  $("incidentCount").textContent = incidents;
  $("justifiedCount").textContent = justified;
}

function renderJustifications(list) {
  const body = $("justifications");
  if (list.length === 0) {
    body.innerHTML = "<tr><td colspan='5'>Sin justificaciones en el periodo</td></tr>";
    return;
  }
  body.innerHTML = list.map((j) =>
    "<tr><td>" + j.date + "</td><td>" + esc(INCIDENT_LABELS[j.incidentType] || j.incidentType) +
    "</td><td>" + esc(j.reason) + "</td><td>" + esc(j.author) + "</td><td>" +
    esc(j.recordedAt.substring(0, 19).replace("T", " ")) + "</td></tr>").join("");
}

async function search() {
  clearMessage();
  const personId = $("person").value;
  const from = $("from").value;
  const to = $("to").value;
  if (!personId || !from || !to) { showMessage("Indica persona y fechas", "error"); return; }
  if (to < from) { showMessage("La fecha final es anterior a la inicial", "error"); return; }
  const dates = datesBetween(from, to);
  if (dates.length > MAX_DAYS) { showMessage("El periodo máximo es de " + MAX_DAYS + " días", "error"); return; }

  try {
    renderEvents(await api.listClockEvents(personId, from, to));
    renderJustifications(await api.listJustifications(personId, from, to));
  } catch (e) {
    showMessage(e.message, "error");
    return;
  }

  const rows = await Promise.all(dates.map(async (date) => {
    try {
      return { date, presence: await api.getPresence(personId, date) };
    } catch (e) {
      return { date, note: e.code === "SCHEDULE_NOT_ASSIGNED" ? "Sin horario asignado" : e.message };
    }
  }));
  renderDaily(rows);
}

async function justify(date, type) {
  const reason = window.prompt("Motivo de la justificación (" + date + "):");
  if (reason === null) return;
  const author = $("author").value.trim();
  try {
    await api.justifyIncident({
      personId: $("person").value,
      date,
      incidentType: type,
      reason,
      author,
    });
    showMessage("Incidencia justificada", "ok");
    const message = $("message").textContent;
    await search();
    showMessage(message, "ok");
  } catch (e) {
    showMessage(e.message, "error");
  }
}

async function init() {
  try {
    const people = await api.listPeople();
    $("person").innerHTML = people.map((p) => "<option value='" + esc(p.id) + "'>" + esc(p.name) + "</option>").join("");
  } catch (e) {
    showMessage(e.message, "error");
    return;
  }
  const today = todayIso();
  $("from").value = today.substring(0, 8) + "01";
  $("to").value = today;
  $("search").addEventListener("click", search);
  $("daily").addEventListener("click", (event) => {
    const button = event.target.closest("button[data-type]");
    if (button) justify(button.dataset.date, button.dataset.type);
  });
  await search();
}

init();