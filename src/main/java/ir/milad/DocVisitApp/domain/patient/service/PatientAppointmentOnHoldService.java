package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@UnitTestRequired
@Service
public class PatientAppointmentOnHoldService {

    private final VisitSessionRepository visitSessionRepository;

    public PatientAppointmentOnHoldService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public void onHold(String appointmentId) {
        var vs = visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."));
        vs.onHold(appointmentId, LocalTime.now().withSecond(0).withNano(0));
        visitSessionRepository.updateActiveVisitSession(vs);
    }
}
