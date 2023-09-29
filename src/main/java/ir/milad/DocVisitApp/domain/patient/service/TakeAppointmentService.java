package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.*;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
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

    public AppointmentData takeAppointment(Patient patient, LocalTime entryTime, int numOfPersons) {
        var et = entryTime.withSecond(0).withNano(0);
        var vs = visitSessionRepository.getActiveSession(LocalDateTime.of(LocalDate.now(), et))
                .orElseThrow(() -> new ApplicationException("Active session not found."));

        if (patientRepository.isBlocked(patient)) {
            synchronized (this) {
                patientRepository.addPatientHistory(patient, new PatientHistory(LocalDate.now(), PatientHistoryStatus.BLOCKED));
                vs.addBlockedAppointment(patient, et);
                visitSessionRepository.updateActiveVisitSession(vs);
            }
            throw new PatientIsBlockedException("Patient is blocked");
        }

        Appointment appointment;
        synchronized (this) {
            appointment = vs.giveAppointment(patient, et, numOfPersons);
            visitSessionRepository.updateActiveVisitSession(vs);
        }
        return new AppointmentData(appointment, vs.appointmentTurnsToAwait(appointment.getId()));
    }
}
