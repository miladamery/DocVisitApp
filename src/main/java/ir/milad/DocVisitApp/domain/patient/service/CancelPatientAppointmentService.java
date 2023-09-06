package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class CancelPatientAppointmentService {
    private final VisitSessionRepository visitSessionRepository;
    private final PatientRepository patientRepository;

    public CancelPatientAppointmentService(VisitSessionRepository visitSessionRepository, PatientRepository patientRepository) {
        this.visitSessionRepository = visitSessionRepository;
        this.patientRepository = patientRepository;
    }

    public synchronized void cancelByPatient(String appointmentId) {
        cancel(appointmentId, AppointmentStatus.CANCELED, PatientHistoryStatus.CANCELLED);
    }

    public synchronized void cancelByDoctor(String appointmentId) {
        cancel(appointmentId, AppointmentStatus.CANCELED_BY_DOCTOR, PatientHistoryStatus.CANCELED_BY_DOCTOR);
    }

    private void cancel(String appointmentId, AppointmentStatus appointmentStatus, PatientHistoryStatus historyStatus) {
        var patient = visitSessionRepository.findActiveSessionForToday()
                .orElseThrow(() -> new ApplicationException("Active session not found."))
                .cancelAppointment(appointmentId, appointmentStatus, LocalTime.now());
        patientRepository.addPatientHistory(patient, new PatientHistory(LocalDate.now(), historyStatus));
        visitSessionRepository.updateActiveVisitSession();
    }
}
