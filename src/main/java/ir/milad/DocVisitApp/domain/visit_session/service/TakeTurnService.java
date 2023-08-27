package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.Patient;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class TakeTurnService {

    private final VisitSessionRepository visitSessionRepository;

    public TakeTurnService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public synchronized Appointment getAppointment(Patient patient, LocalTime entryTime) {
        var turn = visitSessionRepository.findForTodayAndNow()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .giveAppointment(patient, entryTime);
        visitSessionRepository.update();
        return turn;
    }
}
