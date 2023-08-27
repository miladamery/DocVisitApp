package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class CreateVisitSessionService {

    private final VisitSessionRepository visitSessionRepository;

    public CreateVisitSessionService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public void create(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        if (this.visitSessionRepository.exists(date))
            throw new ApplicationException("Visit session for " + date.toString() + " exists.");
        visitSessionRepository.setActiveVisitSession(new VisitSession(date, fromTime, toTime, sessionLength));
    }
}
