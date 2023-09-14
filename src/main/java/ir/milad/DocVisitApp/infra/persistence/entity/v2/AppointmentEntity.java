package ir.milad.DocVisitApp.infra.persistence.entity.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AppointmentEntity {
    private final String id;
    private int turnNumber;
    private int turnsToAwait;
    private LocalDateTime visitTime;
    @JsonIgnore
    private PatientEntity patient;
    private AppointmentStatus status;
    private Integer numOfPersons;

    public static AppointmentEntity from(Appointment appointment) {
        return new AppointmentEntity(
                appointment.getId(),
                appointment.getTurnNumber(),
                appointment.getTurnsToAwait(),
                appointment.getVisitTime(),
                PatientEntity.from(appointment.getPatient()),
                appointment.getStatus(),
                appointment.getNumOfPersons()
        );
    }
}
