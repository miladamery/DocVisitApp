package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class CancelAppointmentService {
    private final VisitSessionRepository visitSessionRepository;

    public CancelAppointmentService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public synchronized void cancel(String turnId) {
        visitSessionRepository.findActiveSessionForTodayAndNow()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .cancelAppointment(turnId);
        visitSessionRepository.updateActiveVisitSession();
    }
}
