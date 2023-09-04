package ir.milad.DocVisitApp.domain.visit_session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.tsid.TsidCreator;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.Patient;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class Appointment {
    final String id;
    int turnNumber;
    int turnsToAwait;
    LocalDateTime visitTime;
    @JsonIgnore
    Patient patient;
    AppointmentStatus status;
    Integer numOfPersons;

    public Appointment(int turnNumber, int turnsToAwait, LocalDateTime visitTime, Patient patient, Integer numOfPersons) {
        this.id = TsidCreator.getTsid().toString();
        this.turnNumber = turnNumber;
        this.turnsToAwait = turnsToAwait;
        this.visitTime = visitTime;
        this.patient = patient;
        status = AppointmentStatus.WAITING;
        this.numOfPersons = numOfPersons;
    }

    public boolean isSamePatient(Patient patient) {
        return this.patient.equals(patient);
    }

    @UnitTestRequired
    public Long waitingTimeFromNow() {
        var now = LocalDateTime.now();
        if (visitTime.isAfter(now) || visitTime.equals(now))
            return Duration.between(now, visitTime).toMinutes();
        return 0L;
    }
}
