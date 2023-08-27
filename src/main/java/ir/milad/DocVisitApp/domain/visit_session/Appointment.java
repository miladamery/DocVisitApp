package ir.milad.DocVisitApp.domain.visit_session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.tsid.TsidCreator;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import lombok.Data;

import java.time.Duration;
import java.time.LocalTime;

@Data
public class Appointment {
    final String id;
    int turnNumber;
    int turnsToAwait;
    LocalTime visitTime;
    @JsonIgnore
    Patient patient;
    AppointmentStatus status;

    public Appointment(int turnNumber, int turnsToAwait, LocalTime visitTime, Patient patient) {
        this.id = TsidCreator.getTsid().toString();
        this.turnNumber = turnNumber;
        this.turnsToAwait = turnsToAwait;
        this.visitTime = visitTime;
        this.patient = patient;
        status = AppointmentStatus.WAITING;
    }

    public boolean isSamePatient(Patient patient) {
        return this.patient.equals(patient);
    }

    @UnitTestRequired
    public Long waitingTimeFromNow() {
        var now = LocalTime.now();
        if (visitTime.isAfter(now) || visitTime.equals(now))
            return Duration.between(now, visitTime).toMinutes();
        return 0L;
    }
}
