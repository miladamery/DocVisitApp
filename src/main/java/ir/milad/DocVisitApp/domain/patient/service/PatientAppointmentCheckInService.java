package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@UnitTestRequired
@Service
public class PatientAppointmentCheckInService {

    private final VisitSessionRepository visitSessionRepository;

    public PatientAppointmentCheckInService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public synchronized void checkIn(String appointmentId) {
        var vs = visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."));
        vs.checkIn(appointmentId, LocalTime.now().withSecond(0).withNano(0));
        visitSessionRepository.updateActiveVisitSession(vs);
    }
}