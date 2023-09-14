package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@UnitTestRequired
@Service
public class PatientAppointmentDoneService {

    private final VisitSessionRepository visitSessionRepository;
    private final PatientRepository patientRepository;
    public PatientAppointmentDoneService(VisitSessionRepository visitSessionRepository, PatientRepository patientRepository) {
        this.visitSessionRepository = visitSessionRepository;
        this.patientRepository = patientRepository;
    }

    public void done(String id) {
        var visitSession = visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."));
        var appointment = visitSession.findAppointmentById(id)
                .orElseThrow(() -> new ApplicationException("Appointment didnt found:" + id));
        visitSession.done(appointment.getId(), LocalTime.now().withSecond(0).withNano(0));
        patientRepository.addPatientHistory(appointment.getPatient(), new PatientHistory(LocalDate.now(), PatientHistoryStatus.VISITED));
        visitSessionRepository.updateActiveVisitSession(visitSession);
    }
}
