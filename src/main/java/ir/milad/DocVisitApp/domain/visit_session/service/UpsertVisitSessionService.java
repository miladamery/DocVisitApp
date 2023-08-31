package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class UpsertVisitSessionService {

    private final VisitSessionRepository visitSessionRepository;

    public UpsertVisitSessionService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public void upsert(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        var vs = visitSessionRepository.findActiveSessionForToday();
        if (vs.isPresent()) {
            vs.get().update(date, fromTime, toTime, sessionLength);
            this.visitSessionRepository.updateActiveVisitSession();
        } else {
            visitSessionRepository.setActiveVisitSession(new VisitSession(date, fromTime, toTime, sessionLength));
        }
    }
}
