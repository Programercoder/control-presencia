import { API_BASE_URL } from "../frontend-shared/config.js";
import { createHttpApi, todayIso, STATUS_LABELS, INCIDENT_LABELS } from "../frontend-shared/apiClient.js";


const api = createHttpApi(API_BASE_URL);

const $ = (id) => document.getElementById(id);

function showMessage(text, kind) {
  const box = $("message");
  box.textContent = text;
  box.className = "message " + kind;
}

function clearMessage() {
  $("message").className = "message hidden";
}

function fmtMinutes(m) {
  const sign = m < 0 ? "-" : "";
  const abs = Math.abs(m);
  return sign + Math.floor(abs / 60) + "h " + String(abs % 60).padStart(2, "0") + "m";
}

function renderPresence(p) {
  $("status").textContent = STATUS_LABELS[p.status] || p.status;
  $("status").className = "badge " + p.status;
  $("expected").textContent = fmtMinutes(p.expectedMinutes);
  $("worked").textContent = fmtMinutes(p.workedMinutes);
  $("deviation").textContent = fmtMinutes(p.deviationMinutes);
  const list = $("incidents");
  list.innerHTML = "";
  p.incidents.forEach((i) => {
    const li = document.createElement("li");
    li.textContent = (INCIDENT_LABELS[i.type] || i.type) + ": " + i.minutes + " min";
    list.appendChild(li);
  });
}

function renderEmptyPresence(text) {
  $("status").textContent = text;
  $("status").className = "badge";
  ["expected", "worked", "deviation"].forEach((id) => ($(id).textContent = "-"));
  $("incidents").innerHTML = "";
}

function renderEvents(events) {
  const body = $("events");
  body.innerHTML = "";
  if (events.length === 0) {
    body.innerHTML = "<tr><td colspan='3'>Sin fichajes hoy</td></tr>";
    return;
  }
  events.forEach((e) => {
    const tr = document.createElement("tr");
    const time = e.timestamp.substring(11, 19);
    const type = e.type === "CLOCK_IN" ? "Entrada" : "Salida";
    tr.innerHTML = "<td>" + time + "</td><td>" + type + "</td><td>" + e.source + "</td>";
    body.appendChild(tr);
  });
}

async function refresh() {
  const personId = $("person").value;
  if (!personId) return;
  const today = todayIso();
  try {
    renderEvents(await api.listClockEvents(personId, today, today));
  } catch (e) {
    showMessage(e.message, "error");
    return;
  }
  try {
    renderPresence(await api.getPresence(personId, today));
  } catch (e) {
    if (e.code === "SCHEDULE_NOT_ASSIGNED") {
      renderEmptyPresence("Sin horario asignado");
    } else {
      renderEmptyPresence("-");
      showMessage(e.message, "error");
    }
  }
}

async function clock(type) {
  clearMessage();
  const personId = $("person").value;
  try {
    const event = await api.registerClockEvent(personId, type, "WEB");
    const label = type === "CLOCK_IN" ? "Entrada" : "Salida";
    showMessage(label + " registrada a las " + event.timestamp.substring(11, 19), "ok");
  } catch (e) {
    showMessage(e.message, "error");
  }
  await refresh();
}

async function init() {
  try {
    const people = await api.listPeople();
    $("person").innerHTML = people.map((p) => "<option value='" + p.id + "'>" + p.name + "</option>").join("");
  } catch (e) {
    showMessage(e.message, "error");
    return;
  }
  $("person").addEventListener("change", () => { clearMessage(); refresh(); });
  $("clockIn").addEventListener("click", () => clock("CLOCK_IN"));
  $("clockOut").addEventListener("click", () => clock("CLOCK_OUT"));
  await refresh();
  setInterval(refresh, 10000);
}

init();