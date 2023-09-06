package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@UnitTestRequired
@Service
public class LoadPatientAppointmentService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadPatientAppointmentService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public Optional<AppointmentData> loadPatientAppointment(String id) {
        var vs = visitSessionRepository.getActiveSession(LocalDateTime.now())
                .orElseThrow(() -> new ApplicationException("Visit Session Not Found."));
        return vs.findAppointmentById(id).map(ap -> new AppointmentData(ap, vs.appointmentTurnsToAwait(ap.getId())));
    }
}
