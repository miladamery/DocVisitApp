package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class PatientAppointmentCheckInService {

    private final VisitSessionRepository visitSessionRepository;

    public PatientAppointmentCheckInService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public void checkIn(String id) {
        visitSessionRepository.findActiveSessionForTodayAndNow()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .findAppointmentById(id)
                .orElseThrow(() -> new ApplicationException("Appointment didnt found:" + id))
                .setStatus(AppointmentStatus.VISITING);
        visitSessionRepository.updateActiveVisitSession();
    }
}