package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class PatientAppointmentCheckInService {

    private final VisitSessionRepository visitSessionRepository;

    public PatientAppointmentCheckInService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public synchronized void checkIn(String appointmentId) {
        visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .checkIn(appointmentId);
        visitSessionRepository.updateActiveVisitSession();
    }
}