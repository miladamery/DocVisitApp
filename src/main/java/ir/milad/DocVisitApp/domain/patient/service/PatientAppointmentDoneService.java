package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PatientAppointmentDoneService {

    private final VisitSessionRepository visitSessionRepository;
    private final PatientRepository patientRepository;
    public PatientAppointmentDoneService(VisitSessionRepository visitSessionRepository, PatientRepository patientRepository) {
        this.visitSessionRepository = visitSessionRepository;
        this.patientRepository = patientRepository;
    }

    public void done(String id) {
        var appointment = visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .findAppointmentById(id)
                .orElseThrow(() -> new ApplicationException("Appointment didnt found:" + id));
        patientRepository.addPatientHistory(appointment.getPatient(), new PatientHistory(LocalDate.now(), PatientHistoryStatus.VISITED));
        appointment.setStatus(AppointmentStatus.VISITED);
        visitSessionRepository.updateActiveVisitSession();
    }
}
