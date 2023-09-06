package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

@UnitTestRequired
@Service
public class PatientAppointmentOnHoldService {

    private VisitSessionRepository visitSessionRepository;

    public PatientAppointmentOnHoldService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public void onHold(String appointmentId) {
        visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .onHold(appointmentId);
        visitSessionRepository.updateActiveVisitSession();
    }
}
