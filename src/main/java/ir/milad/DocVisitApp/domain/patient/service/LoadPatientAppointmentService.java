package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoadPatientAppointmentService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadPatientAppointmentService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public Optional<AppointmentData> loadPatientAppointment(String id) {
        var vs = visitSessionRepository.getActiveSession(LocalDateTime.now())
                .orElseThrow(() -> new ApplicationException("Visit Session Not Found."));
        var appointment = vs.findAppointmentById(id);
        var visited = vs.numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.VISITED));
        return appointment.map(ap -> new AppointmentData(ap, ap.getTurnsToAwait() - visited));
    }
}
