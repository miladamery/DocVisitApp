package ir.milad.DocVisitApp.domain.visit_session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.tsid.TsidCreator;
import ir.milad.DocVisitApp.domain.patient.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Appointment {
    final String id;
    int turnNumber;
    int turnsToAwait;
    // TODO: 9/14/2023 This is A VALUE OBJECT. convert to one.
    LocalDateTime visitTime;
    @JsonIgnore
    Patient patient;
    AppointmentStatus status;
    Integer numOfPersons;

    public static Appointment newAppointment(int turnNumber, int turnsToAwait, LocalDateTime visitTime, Patient patient, Integer numOfPersons) {
        return new Appointment(
                TsidCreator.getTsid().toString(),
                turnNumber,
                turnsToAwait,
                visitTime,
                patient,
                AppointmentStatus.WAITING,
                numOfPersons
        );
    }

    public boolean isSamePatient(Patient patient) {
        return this.patient.equals(patient);
    }

    public void decrementTurnsToAwait() {
        this.turnsToAwait--;
    }

    public void incrementTurnsToAwait() {
        this.turnsToAwait++;
    }

    public void increaseVisitTime(Integer minutes) {
        visitTime = visitTime.plusMinutes(minutes);
    }

    public void increaseVisitTime(Long minutes) {
        visitTime = visitTime.plusMinutes(minutes);
    }

    public void decreaseVisitTime(Integer minutes) {
        visitTime = visitTime.minusMinutes(minutes);
    }

    public void decreaseVisitTime(Long minutes) {
        visitTime = visitTime.minusMinutes(minutes);
    }

    public LocalDateTime calculatedEndTime(int sessionLength){
        return visitTime.plusMinutes((long) numOfPersons * sessionLength);
    }

}
