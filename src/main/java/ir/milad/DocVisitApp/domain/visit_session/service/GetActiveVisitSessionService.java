package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@UnitTestRequired
@Service
public class GetActiveVisitSessionService {
    private final VisitSessionRepository visitSessionRepository;

    public GetActiveVisitSessionService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public Optional<VisitSession> findActiveSessionForTodayAndNow() {
        return visitSessionRepository.findActiveSessionForTodayAndNow();
    }

    public Optional<VisitSession> findActiveVisitSessionForToday() {
        return visitSessionRepository.findActiveSessionForToday();
    }
}
