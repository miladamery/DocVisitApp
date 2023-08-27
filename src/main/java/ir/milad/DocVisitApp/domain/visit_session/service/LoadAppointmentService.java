package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoadAppointmentService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadAppointmentService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public Optional<Appointment> loadTurn(String id) {
        return visitSessionRepository
                .getActiveSession(LocalDateTime.now())
                .flatMap(visitSession -> visitSession.findTurnById(id));
    }
}
