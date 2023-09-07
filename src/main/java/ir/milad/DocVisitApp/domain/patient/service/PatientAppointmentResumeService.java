package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@UnitTestRequired
@Service
public class PatientAppointmentResumeService {

    private final VisitSessionRepository visitSessionRepository;

    public PatientAppointmentResumeService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public void resume(String appointmentId) {
        visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .resume(appointmentId, LocalTime.now().withNano(0));
        visitSessionRepository.updateActiveVisitSession();
    }
}
