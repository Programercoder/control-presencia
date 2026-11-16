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
      "</td><td>" + (e.type === "CLOCK_IN" ? "Entrada" : "Salida") + "</td><td>" + e.source + "</td>";
    body.appendChild(tr);
  });
}

function renderDaily(rows) {
  const body = $("daily");
  body.innerHTML = "";
  let days = 0, expected = 0, worked = 0, deviation = 0, incidents = 0;
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
      const list = p.incidents.map((i) => (INCIDENT_LABELS[i.type] || i.type) + " (" + i.minutes + " min)").join(", ");
      tr.innerHTML = "<td>" + r.date + "</td><td><span class='badge " + p.status + "'>" +
        (STATUS_LABELS[p.status] || p.status) + "</span></td><td>" + fmtMinutes(p.workedMinutes) +
        "</td><td>" + fmtMinutes(p.deviationMinutes) + "</td><td>" + (list || "-") + "</td>";
    } else {
      tr.innerHTML = "<td>" + r.date + "</td><td colspan='4'>" + r.note + "</td>";
    }
    body.appendChild(tr);
  });
  $("days").textContent = days;
  $("expected").textContent = fmtMinutes(expected);
  $("worked").textContent = fmtMinutes(worked);
  $("deviation").textContent = fmtMinutes(deviation);
  $("incidentCount").textContent = incidents;
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

async function init() {
  try {
    const people = await api.listPeople();
    $("person").innerHTML = people.map((p) => "<option value='" + p.id + "'>" + p.name + "</option>").join("");
  } catch (e) {
    showMessage(e.message, "error");
    return;
  }
  const today = todayIso();
  $("from").value = today.substring(0, 8) + "01";
  $("to").value = today;
  $("search").addEventListener("click", search);
  await search();
}

init();