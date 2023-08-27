package ir.milad.DocVisitApp.domain.patient;

import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
public class PatientHistoryRow {
    public LocalDate date;
    public AppointmentStatus status;
}
