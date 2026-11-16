import { API_BASE_URL } from "../frontend-shared/config.js";
import { createHttpApi, todayIso } from "../frontend-shared/apiClient.js";

const api = createHttpApi(API_BASE_URL);
const $ = (id) => document.getElementById(id);

const DAYS = [
  ["MONDAY", "Lun"], ["TUESDAY", "Mar"], ["WEDNESDAY", "Mié"], ["THURSDAY", "Jue"],
  ["FRIDAY", "Vie"], ["SATURDAY", "Sáb"], ["SUNDAY", "Dom"],
];
const TYPE_LABELS = { CONTINUOUS: "Corrido", SPLIT: "Partido", SHIFTS: "Turnos y guardias" };

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

// ---------- list ----------

function slotsText(slots) {
  return (slots || []).map((s) => s.start + "-" + s.end).join(" y ");
}

function detail(s) {
  if (s.type === "SHIFTS") {
    const cycle = (s.cycle || []).map((sh) => sh.name + (sh.slots.length ? " " + slotsText(sh.slots) : "")).join(" | ");
    return "Desde " + s.cycleStart + ": " + cycle;
  }
  const days = (s.days || []).map((d) => DAYS.find((x) => x[0] === d)[1]).join(", ");
  return days + " · " + slotsText(s.slots);
}

async function loadSchedules() {
  const schedules = await api.listSchedules();
  $("schedules").innerHTML = schedules.map((s) =>
    "<tr><td>" + esc(s.id) + "</td><td>" + esc(s.name) + "</td><td>" + TYPE_LABELS[s.type] +
    "</td><td>" + esc(detail(s)) + "</td></tr>").join("");
  $("schedule").innerHTML = schedules.map((s) =>
    "<option value='" + esc(s.id) + "'>" + esc(s.name) + " (" + esc(s.id) + ")</option>").join("");
}

// ---------- form ----------

function addSlotRow(container, start = "", end = "") {
  const row = document.createElement("div");
  row.className = "slot-row";
  row.innerHTML = "<input type='time' class='start' value='" + start + "'><input type='time' class='end' value='" + end + "'>" +
    "<button type='button' class='small'>Quitar</button>";
  row.querySelector("button").addEventListener("click", () => row.remove());
  container.appendChild(row);
}

function addShiftRow(name = "", start = "", end = "") {
  const row = document.createElement("div");
  row.className = "slot-row";
  row.innerHTML = "<input class='shift-name' placeholder='Nombre (MORNING)' value='" + esc(name) + "'>" +
    "<input type='time' class='start' value='" + start + "'><input type='time' class='end' value='" + end + "'>" +
    "<button type='button' class='small'>Quitar</button>";
  row.querySelector("button").addEventListener("click", () => row.remove());
  $("shifts").appendChild(row);
}

function readSlots(container) {
  return [...container.querySelectorAll(".slot-row")]
    .map((r) => ({ start: r.querySelector(".start").value, end: r.querySelector(".end").value }))
    .filter((s) => s.start || s.end);
}

function readShifts() {
  return [...$("shifts").querySelectorAll(".slot-row")].map((r) => {
    const start = r.querySelector(".start").value;
    const end = r.querySelector(".end").value;
    return {
      name: r.querySelector(".shift-name").value.trim(),
      slots: start || end ? [{ start, end }] : [],
    };
  });
}

function updateType() {
  const shifts = $("type").value === "SHIFTS";
  $("daysBox").classList.toggle("hidden", shifts);
  $("slotsBox").classList.toggle("hidden", shifts);
  $("shiftsBox").classList.toggle("hidden", !shifts);
}

function resetForm() {
  $("id").value = "";
  $("name").value = "";
  $("slots").innerHTML = "";
  $("shifts").innerHTML = "";
  addSlotRow($("slots"), "08:00", "15:00");
  document.querySelectorAll("#days input").forEach((c) => (c.checked = false));
}

async function save() {
  clearMessage();
  const type = $("type").value;
  const schedule = { id: $("id").value.trim(), type, name: $("name").value.trim() };
  if (type === "SHIFTS") {
    schedule.cycleStart = $("cycleStart").value || null;
    schedule.cycle = readShifts();
  } else {
    schedule.days = [...document.querySelectorAll("#days input:checked")].map((c) => c.value);
    schedule.slots = readSlots($("slots"));
  }
  try {
    await api.defineSchedule(schedule);
    showMessage("Horario " + schedule.id + " guardado", "ok");
    resetForm();
    await loadSchedules();
  } catch (e) {
    showMessage(e.message, "error");
  }
}

async function assign() {
  clearMessage();
  const assignment = {
    personId: $("person").value,
    scheduleId: $("schedule").value,
    from: $("from").value || null,
    to: $("to").value || null,
  };
  if (!assignment.from) { showMessage("Indica la fecha de inicio", "error"); return; }
  try {
    await api.assignSchedule(assignment);
    showMessage("Horario asignado a " + assignment.personId, "ok");
  } catch (e) {
    showMessage(e.message, "error");
  }
}

async function init() {
  $("days").innerHTML = DAYS.map((d) => "<label><input type='checkbox' value='" + d[0] + "'>" + d[1] + "</label>").join("");
  $("type").addEventListener("change", () => {
    updateType();
    if ($("type").value === "SPLIT" && $("slots").children.length < 2) addSlotRow($("slots"), "17:00", "21:00");
    if ($("type").value === "SHIFTS" && $("shifts").children.length === 0) {
      addShiftRow("MORNING", "08:00", "15:00");
      addShiftRow("ON_CALL", "20:00", "08:00");
      addShiftRow("OFF");
    }
  });
  $("addSlot").addEventListener("click", () => addSlotRow($("slots")));
  $("addShift").addEventListener("click", () => addShiftRow());
  $("save").addEventListener("click", save);
  $("assign").addEventListener("click", assign);
  $("from").value = todayIso();
  $("cycleStart").value = todayIso();
  resetForm();
  updateType();
  try {
    const people = await api.listPeople();
    $("person").innerHTML = people.map((p) => "<option value='" + esc(p.id) + "'>" + esc(p.name) + "</option>").join("");
    await loadSchedules();
  } catch (e) {
    showMessage(e.message, "error");
  }
}

init();