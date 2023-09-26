package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import lombok.ToString;

import java.time.format.DateTimeFormatter;

@ToString
public class AppointmentData {
    public String id;
    public int turnNumber;
    public String time;
    public Long turnsToAwait;
    public AppointmentStatus status;
    public String firstName;
    public String lastName;

    public AppointmentData(Appointment appointment, Long turnsToAwait) {
        this.id = appointment.getId();
        this.turnNumber = appointment.getTurnNumber();
        this.time = appointment.getVisitTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.turnsToAwait = turnsToAwait;
        this.status = appointment.getStatus();
        this.firstName = appointment.getPatient().getFirstName();
        this.lastName = appointment.getPatient().getLastName();
    }
}
