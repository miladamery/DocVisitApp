package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;

public class AppointmentData {
    public String id;
    public int turnNumber;
    public Long waitingTime;
    public Long turnsToAwait;
    public AppointmentStatus status;
    public String firstName;
    public String lastName;

    public AppointmentData(Appointment appointment, Long turnsToAwait) {
        this.id = appointment.getId();
        this.turnNumber = appointment.getTurnNumber();
        this.waitingTime = appointment.waitingTimeFromNow();
        this.turnsToAwait = turnsToAwait;
        this.status = appointment.getStatus();
        this.firstName = appointment.getPatient().getFirstName();
        this.lastName = appointment.getPatient().getLastName();
    }
}
