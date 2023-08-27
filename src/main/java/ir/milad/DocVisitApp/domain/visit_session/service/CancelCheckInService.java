package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class CancelCheckInService {
    private final VisitSessionRepository visitSessionRepository;

    public CancelCheckInService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public synchronized void cancel(String turnId) {
        visitSessionRepository.findForTodayAndNow()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .cancelAppointment(turnId);
        visitSessionRepository.update();
    }
}
