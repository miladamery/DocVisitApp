package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class LoadPatientHistoryService {
    private final PatientRepository patientRepository;

    public LoadPatientHistoryService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientHistory> load(Patient patient) {
        var res = patientRepository.findHistoryByPatient(patient);
        Collections.reverse(res);
        return res;
    }
}
