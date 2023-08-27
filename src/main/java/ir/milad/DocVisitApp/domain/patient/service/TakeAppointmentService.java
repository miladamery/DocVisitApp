package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class TakeAppointmentService {

    private final VisitSessionRepository visitSessionRepository;

    public TakeAppointmentService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public synchronized Appointment takeAppointment(Patient patient, LocalTime entryTime) {
        var appointment = visitSessionRepository.getActiveSession(LocalDateTime.of(LocalDate.now(), entryTime))
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .giveAppointment(patient, entryTime);
        visitSessionRepository.updateActiveVisitSession();
        return appointment;
    }
}
