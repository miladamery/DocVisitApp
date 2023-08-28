package ir.milad.DocVisitApp.domain.patient;

import java.util.List;

public interface PatientRepository {
    void addPatientHistory(Patient patient, PatientHistory history);

    List<PatientHistory> findHistoryByPatient(Patient patient);

    void addToBlocked(Patient patient);

    boolean isBlocked(Patient patient);
}
