package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoadTurnService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadTurnService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public Optional<Appointment> loadTurn(String id) {
        return visitSessionRepository
                .findForTodayAndNow()
                .flatMap(visitSession -> visitSession.findTurnById(id));
    }
}
