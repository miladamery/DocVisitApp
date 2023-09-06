package ir.milad.DocVisitApp.domain.visit_session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.tsid.TsidCreator;
import ir.milad.DocVisitApp.domain.patient.Patient;
import lombok.Data;

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
