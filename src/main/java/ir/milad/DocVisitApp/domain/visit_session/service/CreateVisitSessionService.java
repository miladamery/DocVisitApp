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
        if (this.visitSessionRepository.findByDateAndFromTimeAndToTime(date, fromTime, toTime).isPresent())
            throw new ApplicationException("Visit session for given time exists.");
        // TODO: 8/26/2023 Check duplicate or overlapping session do not enter
        visitSessionRepository.save(new VisitSession(date, fromTime, toTime, sessionLength));
    }
}
