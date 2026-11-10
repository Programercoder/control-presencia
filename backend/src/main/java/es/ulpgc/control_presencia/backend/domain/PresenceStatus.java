package es.ulpgc.control_presencia.backend.domain;

public enum PresenceStatus {
    NO_SHIFT,           // nothing scheduled today
    PENDING,            // clock-in time has not arrived yet
    PRESENT,            // clocked in and still inside
    LATE,               // should be here but has not clocked in
    ABSENT,             // shift ended with no clock events
    CLOCK_OUT_PENDING,  // still "inside" after the end of the shift
    COMPLETED,          // shift closed without incidents
    WITH_INCIDENT       // shift closed with late arrival or early departure
}
