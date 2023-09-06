package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.*;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@UnitTestRequired
@Service
public class TakeAppointmentService {

    private final VisitSessionRepository visitSessionRepository;
    private final PatientRepository patientRepository;

    public TakeAppointmentService(VisitSessionRepository visitSessionRepository, PatientRepository patientRepository) {
        this.visitSessionRepository = visitSessionRepository;
        this.patientRepository = patientRepository;
    }

    public synchronized AppointmentData takeAppointment(Patient patient, LocalTime entryTime, int numOfPersons) {
        var vs = visitSessionRepository.getActiveSession(LocalDateTime.of(LocalDate.now(), entryTime))
                .orElseThrow(() -> new ApplicationException("Active session not found."));

        if (patientRepository.isBlocked(patient)) {
            patientRepository.addPatientHistory(patient, new PatientHistory(LocalDate.now(), PatientHistoryStatus.BLOCKED));
            throw new PatientIsBlockedException("Patient is blocked");
        }

        var appointment = vs.giveAppointment(patient, entryTime, numOfPersons);
        visitSessionRepository.updateActiveVisitSession();
        return new AppointmentData(appointment, vs.appointmentTurnsToAwait(appointment.getId()));
    }
}
