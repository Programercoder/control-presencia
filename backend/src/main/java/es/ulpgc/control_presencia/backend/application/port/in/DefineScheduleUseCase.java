package es.ulpgc.control_presencia.backend.application.port.in;

public interface DefineScheduleUseCase {
    void define(String scheduleId, ScheduleDefinition definition);
}
