package ir.milad.DocVisitApp.domain.patient.service;

import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@UnitTestRequired
@Service
public class LoadPatientHistoryViewDataService {
    private final PatientRepository patientRepository;

    public LoadPatientHistoryViewDataService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientHistoryViewData load(Patient patient) {
        var res = patientRepository.findHistoryByPatient(patient);
        Collections.reverse(res);
        return PatientHistoryViewData.from(
                res.map(PatientHistoryDto::new).toList(),
                patient,
                patientRepository.isBlocked(patient)
        );
    }

    @AllArgsConstructor(staticName = "from")
    public static class PatientHistoryViewData {
        public final List<PatientHistoryDto> histories;
        public final Patient patient;
        public final Boolean isBlocked;
    }

    public static class PatientHistoryDto {
        public final String day;
        public final String month;
        public final String year;
        public final PatientHistoryStatus status;

        public PatientHistoryDto(PatientHistory history) {
            day = history.date.format(DateTimeFormatter.ofPattern("dd"));
            month = history.date.toFrenchMonth();
            year = history.date.format(DateTimeFormatter.ofPattern("yyyy"));
            this.status = history.status;
        }
    }
}
