package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class LoadPatientHistoryService {
    private final PatientRepository patientRepository;

    public LoadPatientHistoryService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientHistoryViewData> load(Patient patient) {
        var res = patientRepository.findHistoryByPatient(patient);
        Collections.reverse(res);
        return res.stream().map(PatientHistoryViewData::new).toList();
    }

    public static class PatientHistoryViewData {
        public final String day;
        public final String month;
        public final String year;
        public final PatientHistoryStatus status;

        public PatientHistoryViewData(PatientHistory history) {
            day = history.date.format(DateTimeFormatter.ofPattern("dd"));
            month = history.date.format(DateTimeFormatter.ofPattern("MMMM"));
            year = history.date.format(DateTimeFormatter.ofPattern("yyyy"));
            this.status = history.status;
        }
    }
}
