package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@UnitTestRequired
@Service
public class BlockPatientService {
    private final PatientRepository patientRepository;

    public BlockPatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public void block(Patient patient) {
        patientRepository.addToBlocked(patient);
        patientRepository.addPatientHistory(patient, new PatientHistory(LocalDate.now(), PatientHistoryStatus.BLOCKED));
    }
}
