package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetActiveSessionService {
    private final VisitSessionRepository visitSessionRepository;

    public GetActiveSessionService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public Optional<VisitSession> forTodayAndNow() {
        return visitSessionRepository.findForTodayAndNow();
    }
}
