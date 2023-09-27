package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UnblockPatientService {
    private final PatientRepository patientRepository;

    public UnblockPatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public void unblock(Patient patient) {
        patientRepository.removeFromBlocked(patient);
        patientRepository.addPatientHistory(patient, new PatientHistory(LocalDate.now(), PatientHistoryStatus.UNBLOCKED));
    }
}
