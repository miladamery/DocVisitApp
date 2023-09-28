package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class PatientAppointmentUrgentService {
    private final VisitSessionRepository visitSessionRepository;

    public PatientAppointmentUrgentService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public void urgent(String appointmentId) {
        var vs = visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."));
        vs.urgent(appointmentId, LocalTime.nowHM());
        visitSessionRepository.updateActiveVisitSession(vs);
    }
}
