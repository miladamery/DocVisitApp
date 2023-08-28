package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoadPatientAppointmentService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadPatientAppointmentService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public Optional<Appointment> loadPatientAppointment(String id) {
        return visitSessionRepository
                .getActiveSession(LocalDateTime.now())
                .flatMap(visitSession -> visitSession.findAppointmentById(id));
    }
}
